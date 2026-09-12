package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppThemeMode
import com.example.model.ConnectionStatus
import com.example.model.ControlsDto
import com.example.model.DeviceSettings
import com.example.model.PumpDataDto
import com.example.ui.CacaoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacaoDashboardScreen(
  uiState: CacaoUiState,
  isDarkTheme: Boolean = false,
  onToggleTheme: () -> Unit = {},
  onThemeModeSelect: (AppThemeMode) -> Unit = {},
  onModeChange: (String) -> Unit,
  onManualPumpToggle: (Boolean) -> Unit,
  onRefresh: () -> Unit,
  onSaveSettings: (DeviceSettings) -> Unit,
  onDismissNotice: () -> Unit,
  onSimulateMoistureChange: (Double) -> Unit = {},
  onSimulateDryTrigger: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  var showSettingsDialog by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(uiState.userNotice) {
    uiState.userNotice?.let {
      snackbarHostState.showSnackbar(it)
      onDismissNotice()
    }
  }

  val soilMoisture = uiState.deviceData.sensors?.soilMoisturePct ?: 42.0

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      CacaoTopBar(
        deviceId = uiState.settings.deviceId,
        connectionStatus = uiState.connectionStatus,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        onRefreshClick = onRefresh,
        onSettingsClick = { showSettingsDialog = true },
      )
    },
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    containerColor = MaterialTheme.colorScheme.background,
  ) { innerPadding ->
    LazyColumn(
      modifier =
        Modifier.fillMaxSize()
          .padding(innerPadding)
          .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item { Spacer(modifier = Modifier.height(2.dp)) }

      // 1. Primary Metric: Soil Moisture
      item {
        SoilMoistureCard(sensorData = uiState.deviceData.sensors)
      }

      // 2. Secondary: Pump & Relay Control
      item {
        PumpControlCard(
          pumpData = uiState.deviceData.pump ?: PumpDataDto(),
          controls = uiState.deviceData.controls ?: ControlsDto(),
          isUpdating = uiState.isUpdatingControls,
          currentMoisture = soilMoisture,
          onModeChange = onModeChange,
          onManualPumpToggle = onManualPumpToggle,
        )
      }

      // 3. Atmosphere Sensors (Temperature & Humidity)
      item {
        AtmosphereSensorsRow(sensorData = uiState.deviceData.sensors)
      }

      // 4. Historical Trend Chart
      item {
        SoilTrendChart(historyPoints = uiState.historyPoints)
      }

      // 5. Clean Demo Test Bar (Shown only in Demo Mode)
      if (uiState.settings.isDemoMode) {
        item {
          CleanDemoControls(
            onAddWater = { onSimulateMoistureChange(10.0) },
            onDrySoil = { onSimulateMoistureChange(-10.0) },
            onTriggerDryThreshold = onSimulateDryTrigger,
          )
        }
      }
      item { Spacer(modifier = Modifier.height(16.dp)) }
    }
  }

  if (showSettingsDialog) {
    SettingsDialog(
      currentSettings = uiState.settings,
      currentThemeMode = uiState.themeMode,
      onDismiss = { showSettingsDialog = false },
      onSave = onSaveSettings,
      onThemeModeChange = onThemeModeSelect,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CacaoTopBar(
  deviceId: String,
  connectionStatus: ConnectionStatus,
  isDarkTheme: Boolean,
  onToggleTheme: () -> Unit,
  onRefreshClick: () -> Unit,
  onSettingsClick: () -> Unit,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "spin")
  val rotationAngle by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(1200, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "spinAngle",
    )

  val isSyncing = connectionStatus is ConnectionStatus.Syncing

  val (statusText, statusColor) =
    when (connectionStatus) {
      is ConnectionStatus.Connected -> "Online" to Color(0xFF16A34A)
      is ConnectionStatus.DemoMode -> "Simulasi Demo" to Color(0xFF7C3AED)
      is ConnectionStatus.Syncing -> "Menyinkronkan" to Color(0xFF0284C7)
      is ConnectionStatus.Connecting -> "Menghubungkan" to Color(0xFFD97706)
      is ConnectionStatus.Error -> "Offline" to Color(0xFFDC2626)
    }

  TopAppBar(
    title = {
      Column {
        Text(
          text = "Monitoring Kakao",
          style =
            MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            ),
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 2.dp),
        ) {
          Box(
            modifier =
              Modifier.size(6.dp)
                .clip(CircleShape)
                .background(statusColor)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "$statusText • ${deviceId.ifBlank { "kakao-01" }}",
            style =
              MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              ),
          )
        }
      }
    },
    actions = {
      IconButton(
        onClick = onToggleTheme,
        modifier = Modifier.testTag("theme_toggle_button"),
      ) {
        Icon(
          imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
          contentDescription = if (isDarkTheme) "Ganti ke Mode Terang" else "Ganti ke Mode Gelap",
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }

      IconButton(
        onClick = onRefreshClick,
        modifier = Modifier.testTag("refresh_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Muat Ulang Data",
          tint = MaterialTheme.colorScheme.onSurface,
          modifier = if (isSyncing) Modifier.rotate(rotationAngle) else Modifier,
        )
      }

      IconButton(
        onClick = onSettingsClick,
        modifier = Modifier.testTag("settings_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Pengaturan",
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
    },
    colors =
      TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
      ),
  )
}

@Composable
private fun CleanDemoControls(
  onAddWater: () -> Unit,
  onDrySoil: () -> Unit,
  onTriggerDryThreshold: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    modifier =
      Modifier.fillMaxWidth()
        .border(
          1.dp,
          MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
          RoundedCornerShape(16.dp),
        ),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Uji Coba Data (Mode Demo)",
          style =
            MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            ),
        )
        Text(
          text = "Simulasi",
          style =
            MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              color = Color(0xFF7C3AED),
            ),
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedButton(
          onClick = onAddWater,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f).height(38.dp),
        ) {
          Icon(
            imageVector = Icons.Default.WaterDrop,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("+10%", fontSize = 11.sp)
        }

        OutlinedButton(
          onClick = onDrySoil,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f).height(38.dp),
        ) {
          Icon(
            imageVector = Icons.Default.WbSunny,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("-10%", fontSize = 11.sp)
        }

        OutlinedButton(
          onClick = onTriggerDryThreshold,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f).height(38.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Picu 35%", fontSize = 11.sp)
        }
      }
    }
  }
}
