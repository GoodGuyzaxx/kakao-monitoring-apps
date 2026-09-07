package com.example

import com.example.model.SoilMoistureLevel
import com.example.ui.components.formatPumpReason
import org.junit.Assert.assertEquals
import org.junit.Test

class CacaoLogicUnitTest {

  @Test
  fun testSoilMoistureThresholds() {
    // <= 35% should be DRY (Pump triggers in auto mode)
    assertEquals(SoilMoistureLevel.DRY, SoilMoistureLevel.fromMoisture(34.9))
    assertEquals(SoilMoistureLevel.DRY, SoilMoistureLevel.fromMoisture(35.0))

    // 36% - 54% should be OPTIMAL
    assertEquals(SoilMoistureLevel.OPTIMAL, SoilMoistureLevel.fromMoisture(35.1))
    assertEquals(SoilMoistureLevel.OPTIMAL, SoilMoistureLevel.fromMoisture(45.0))
    assertEquals(SoilMoistureLevel.OPTIMAL, SoilMoistureLevel.fromMoisture(54.9))

    // >= 55% should be WET (Pump shuts off in auto mode)
    assertEquals(SoilMoistureLevel.WET, SoilMoistureLevel.fromMoisture(55.0))
    assertEquals(SoilMoistureLevel.WET, SoilMoistureLevel.fromMoisture(65.5))
  }

  @Test
  fun testPumpReasonFormatting() {
    assertEquals(
      "Otomatis: Tanah Kering (≤ 35%), pompa diaktifkan.",
      formatPumpReason("auto_soil_dry")
    )
    assertEquals(
      "Otomatis: Target kelembapan tanah (≥ 55%) terpenuhi.",
      formatPumpReason("auto_soil_moist")
    )
    assertEquals(
      "Perintah Manual: Dikontrol oleh pengguna melalui aplikasi.",
      formatPumpReason("manual")
    )
    assertEquals(
      "Pengaman: Pompa dimatikan otomatis setelah 3 menit.",
      formatPumpReason("safety_timeout")
    )
    assertEquals(
      "Sistem siap: Inisialisasi awal (ESP32 baru menyala).",
      formatPumpReason("startup")
    )
  }

  @Test
  fun testParseFirebaseJsonStructure() {
    val repository = com.example.data.FirebaseRepository()
    val jsonPayload = """
    {
      "devices": {
        "kakao-01": {
          "controls": {
            "manualPump": false,
            "mode": "auto"
          },
          "pump": {
            "changedAtEpoch": 0,
            "isOn": false,
            "reason": "startup"
          },
          "sensors": {
            "dhtValid": false,
            "humidityAirPct": "null",
            "soilMoisturePct": 100,
            "soilRaw": 237,
            "temperatureC": "null",
            "updatedAtEpoch": 1788789498
          }
        }
      }
    }
    """.trimIndent()

    val parsed = repository.parseDeviceDataJson(jsonPayload, "kakao-01")

    // Assert Controls
    assertEquals("auto", parsed.controls?.mode)
    assertEquals(false, parsed.controls?.manualPump)

    // Assert Pump
    assertEquals(false, parsed.pump?.isOn)
    assertEquals("startup", parsed.pump?.reason)
    assertEquals(0L, parsed.pump?.changedAtEpoch)

    // Assert Sensors
    assertEquals(false, parsed.sensors?.dhtValid)
    assertEquals(null, parsed.sensors?.temperatureC)
    assertEquals(null, parsed.sensors?.humidityAirPct)
    assertEquals(100.0, parsed.sensors?.soilMoisturePct ?: 0.0, 0.001)
    assertEquals(237, parsed.sensors?.soilRaw)
    assertEquals(1788789498L, parsed.sensors?.updatedAtEpoch)
  }

  @Test
  fun testThemeModeEnum() {
    assertEquals(com.example.model.AppThemeMode.SYSTEM, com.example.model.AppThemeMode.valueOf("SYSTEM"))
    assertEquals(com.example.model.AppThemeMode.LIGHT, com.example.model.AppThemeMode.valueOf("LIGHT"))
    assertEquals(com.example.model.AppThemeMode.DARK, com.example.model.AppThemeMode.valueOf("DARK"))
    assertEquals("Ikuti Sistem", com.example.model.AppThemeMode.SYSTEM.label())
    assertEquals("Mode Terang", com.example.model.AppThemeMode.LIGHT.label())
    assertEquals("Mode Gelap", com.example.model.AppThemeMode.DARK.label())
  }
}
