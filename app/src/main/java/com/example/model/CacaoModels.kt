package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviceDataDto(
  @param:Json(name = "sensors") val sensors: SensorDataDto? = null,
  @param:Json(name = "pump") val pump: PumpDataDto? = null,
  @param:Json(name = "controls") val controls: ControlsDto? = null,
)

@JsonClass(generateAdapter = true)
data class SensorDataDto(
  @param:Json(name = "temperatureC") val temperatureC: Double? = null,
  @param:Json(name = "humidityAirPct") val humidityAirPct: Double? = null,
  @param:Json(name = "soilMoisturePct") val soilMoisturePct: Double? = null,
  @param:Json(name = "soilRaw") val soilRaw: Int? = null,
  @param:Json(name = "dhtValid") val dhtValid: Boolean? = null,
  @param:Json(name = "updatedAtEpoch") val updatedAtEpoch: Long? = null,
)

@JsonClass(generateAdapter = true)
data class PumpDataDto(
  @param:Json(name = "isOn") val isOn: Boolean = false,
  @param:Json(name = "reason") val reason: String? = null,
  @param:Json(name = "elapsedSeconds") val elapsedSeconds: Long = 0,
  @param:Json(name = "changedAtEpoch") val changedAtEpoch: Long = 0,
)

@JsonClass(generateAdapter = true)
data class ControlsDto(
  @param:Json(name = "mode") val mode: String = "auto", // "auto" or "manual"
  @param:Json(name = "manualPump") val manualPump: Boolean = false,
)

enum class SoilMoistureLevel {
  DRY, // <= 35%
  OPTIMAL, // 36% - 54%
  WET; // >= 55%

  companion object {
    fun fromMoisture(pct: Double?): SoilMoistureLevel {
      if (pct == null) return OPTIMAL
      return when {
        pct <= 35.0 -> DRY
        pct >= 55.0 -> WET
        else -> OPTIMAL
      }
    }
  }
}

data class SoilHistoryPoint(
  val timestampEpoch: Long,
  val soilMoisturePct: Double,
  val temperatureC: Double,
  val pumpActive: Boolean,
)

data class DeviceSettings(
  val databaseUrl: String = "",
  val deviceId: String = "kakao-01",
  val authToken: String = "",
  val pollIntervalSeconds: Int = 4,
  val isDemoMode: Boolean = true, // Defaults to demo if URL not configured yet
)

sealed interface ConnectionStatus {
  object Connecting : ConnectionStatus
  object Connected : ConnectionStatus
  object Syncing : ConnectionStatus
  data class Error(val message: String) : ConnectionStatus
  object DemoMode : ConnectionStatus
}

enum class AppThemeMode {
  SYSTEM,
  LIGHT,
  DARK;

  fun label(): String = when (this) {
    SYSTEM -> "Ikuti Sistem"
    LIGHT -> "Mode Terang"
    DARK -> "Mode Gelap"
  }
}
