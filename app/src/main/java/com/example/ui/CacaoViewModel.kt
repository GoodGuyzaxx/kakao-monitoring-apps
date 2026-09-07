package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppPreferences
import com.example.data.FirebaseRepository
import com.example.model.AppThemeMode
import com.example.model.ConnectionStatus
import com.example.model.ControlsDto
import com.example.model.DeviceDataDto
import com.example.model.DeviceSettings
import com.example.model.PumpDataDto
import com.example.model.SensorDataDto
import com.example.model.SoilHistoryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class CacaoUiState(
  val deviceData: DeviceDataDto =
    DeviceDataDto(
      sensors =
        SensorDataDto(
          temperatureC = 28.5,
          humidityAirPct = 76.0,
          soilMoisturePct = 42.0,
          soilRaw = 2350,
          dhtValid = true,
          updatedAtEpoch = System.currentTimeMillis() / 1000,
        ),
      pump = PumpDataDto(isOn = false, reason = "auto_soil_moist"),
      controls = ControlsDto(mode = "auto", manualPump = false),
    ),
  val settings: DeviceSettings = DeviceSettings(),
  val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
  val connectionStatus: ConnectionStatus = ConnectionStatus.Connecting,
  val isUpdatingControls: Boolean = false,
  val userNotice: String? = null,
  val historyPoints: List<SoilHistoryPoint> = emptyList(),
  val lastSyncTime: Long = System.currentTimeMillis(),
)

class CacaoViewModel(application: Application) : AndroidViewModel(application) {

  private val appPreferences = AppPreferences(application)
  private val repository = FirebaseRepository()

  private val _uiState = MutableStateFlow(CacaoUiState())
  val uiState: StateFlow<CacaoUiState> = _uiState.asStateFlow()

  private var pollingJob: Job? = null

  init {
    val initialSettings = appPreferences.getSettings()
    val initialTheme = appPreferences.getThemeMode()
    _uiState.update {
      it.copy(
        settings = initialSettings,
        themeMode = initialTheme,
        connectionStatus =
          if (initialSettings.isDemoMode || initialSettings.databaseUrl.isBlank()) {
            ConnectionStatus.DemoMode
          } else {
            ConnectionStatus.Connecting
          },
      )
    }
    startPollingLoop()
  }

  fun startPollingLoop() {
    pollingJob?.cancel()
    pollingJob =
      viewModelScope.launch {
        while (isActive) {
          fetchDataInternal()
          val interval = (_uiState.value.settings.pollIntervalSeconds.coerceIn(2, 60)) * 1000L
          delay(interval)
        }
      }
  }

  fun refreshNow() {
    viewModelScope.launch {
      _uiState.update { it.copy(connectionStatus = ConnectionStatus.Syncing) }
      fetchDataInternal()
    }
  }

  private suspend fun fetchDataInternal() {
    val currentSettings = _uiState.value.settings
    val result = repository.fetchDeviceData(currentSettings)

    result.fold(
      onSuccess = { dto ->
        val timestamp = System.currentTimeMillis()
        val moisture = dto.sensors?.soilMoisturePct ?: 0.0
        val temp = dto.sensors?.temperatureC ?: 0.0
        val pumpIsOn = dto.pump?.isOn ?: false

        _uiState.update { state ->
          val newHistory = (state.historyPoints + SoilHistoryPoint(
            timestampEpoch = timestamp,
            soilMoisturePct = moisture,
            temperatureC = temp,
            pumpActive = pumpIsOn,
          )).takeLast(25)

          state.copy(
            deviceData = dto,
            connectionStatus =
              if (currentSettings.isDemoMode || currentSettings.databaseUrl.isBlank()) {
                ConnectionStatus.DemoMode
              } else {
                ConnectionStatus.Connected
              },
            historyPoints = newHistory,
            lastSyncTime = timestamp,
          )
        }
      },
      onFailure = { error ->
        _uiState.update { state ->
          state.copy(
            connectionStatus = ConnectionStatus.Error(error.message ?: "Gagal terhubung ke Firebase")
          )
        }
      },
    )
  }

  fun setControlMode(mode: String) {
    val currentControls = _uiState.value.deviceData.controls ?: ControlsDto()
    if (currentControls.mode == mode) return

    val targetManualPump = if (mode == "manual") currentControls.manualPump else false
    executeControlUpdate(newMode = mode, newManualPump = targetManualPump)
  }

  fun setManualPump(turnOn: Boolean) {
    executeControlUpdate(newMode = "manual", newManualPump = turnOn)
  }

  private fun executeControlUpdate(newMode: String, newManualPump: Boolean) {
    viewModelScope.launch {
      _uiState.update { it.copy(isUpdatingControls = true) }
      val settings = _uiState.value.settings
      val result = repository.updateControls(settings, newMode, newManualPump)

      result.fold(
        onSuccess = { updatedControls ->
          val noticeText =
            if (newMode == "manual") {
              if (newManualPump) "Pompa dinyalakan (Manual)" else "Pompa dimatikan (Manual)"
            } else {
              "Mode Otomatis Aktif (ambang 35% - 55%)"
            }

          _uiState.update { state ->
            val updatedDeviceData =
              state.deviceData.copy(
                controls = updatedControls,
                pump =
                  if (newMode == "manual") {
                    state.deviceData.pump?.copy(isOn = newManualPump, reason = "manual")
                      ?: PumpDataDto(isOn = newManualPump, reason = "manual")
                  } else {
                    state.deviceData.pump
                  },
              )
            state.copy(
              isUpdatingControls = false,
              deviceData = updatedDeviceData,
              userNotice = noticeText,
            )
          }
          // Fetch immediate state update
          delay(400)
          fetchDataInternal()
        },
        onFailure = { error ->
          _uiState.update { state ->
            state.copy(
              isUpdatingControls = false,
              userNotice = "Gagal mengubah kontrol: ${error.localizedMessage}",
            )
          }
        },
      )
    }
  }

  fun saveSettings(newSettings: DeviceSettings) {
    appPreferences.saveSettings(newSettings)
    _uiState.update {
      it.copy(
        settings = newSettings,
        connectionStatus =
          if (newSettings.isDemoMode || newSettings.databaseUrl.isBlank()) {
            ConnectionStatus.DemoMode
          } else {
            ConnectionStatus.Connecting
          },
        userNotice = "Pengaturan berhasil disimpan",
      )
    }
    startPollingLoop()
  }

  fun dismissNotice() {
    _uiState.update { it.copy(userNotice = null) }
  }

  fun simulateMoistureChange(delta: Double) {
    repository.adjustDemoMoisture(delta)
    viewModelScope.launch {
      fetchDataInternal()
      _uiState.update {
        it.copy(
          userNotice = if (delta > 0) "Simulasi penyiraman (+${delta.toInt()}%)" else "Simulasi tanah mengering (${delta.toInt()}%)"
        )
      }
    }
  }

  fun simulateDryTrigger() {
    repository.triggerThresholdDryDemo()
    viewModelScope.launch {
      fetchDataInternal()
      _uiState.update {
        it.copy(userNotice = "Simulasi tanah kering (≤ 35%) dipicu!")
      }
    }
  }

  fun setThemeMode(mode: AppThemeMode) {
    appPreferences.saveThemeMode(mode)
    _uiState.update {
      it.copy(
        themeMode = mode,
        userNotice = "Tema diubah ke ${mode.label()}",
      )
    }
  }

  fun toggleThemeMode() {
    val current = _uiState.value.themeMode
    val next = when (current) {
      AppThemeMode.LIGHT -> AppThemeMode.DARK
      AppThemeMode.DARK -> AppThemeMode.LIGHT
      AppThemeMode.SYSTEM -> AppThemeMode.DARK
    }
    setThemeMode(next)
  }
}
