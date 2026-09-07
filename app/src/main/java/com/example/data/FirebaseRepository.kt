package com.example.data

import com.example.model.ControlsDto
import com.example.model.DeviceDataDto
import com.example.model.DeviceSettings
import com.example.model.PumpDataDto
import com.example.model.SensorDataDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class FirebaseRepository {

  private val okHttpClient: OkHttpClient =
    OkHttpClient.Builder()
      .connectTimeout(10, TimeUnit.SECONDS)
      .readTimeout(10, TimeUnit.SECONDS)
      .writeTimeout(10, TimeUnit.SECONDS)
      .build()

  private val moshi: Moshi =
    Moshi.Builder()
      .addLast(KotlinJsonAdapterFactory())
      .build()

  private val deviceAdapter = moshi.adapter(DeviceDataDto::class.java)
  private val controlsAdapter = moshi.adapter(ControlsDto::class.java)
  private val mapAdapter =
    moshi.adapter<Map<String, Any?>>(
      Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )

  // In-memory simulation state for Demo mode
  private var demoMoisture: Double = 34.0
  private var demoSoilRaw: Int = 2950
  private var demoTemp: Double = 28.6
  private var demoHumidity: Double = 76.5
  private var demoPumpOn: Boolean = true
  private var demoReason: String = "auto_soil_dry"
  private var demoMode: String = "auto"
  private var demoManualPump: Boolean = false
  private var demoPumpStartTimeSeconds: Long = System.currentTimeMillis() / 1000

  fun cleanFirebaseUrl(rawUrl: String): String {
    var trimmed = rawUrl.trim()
    if (trimmed.isEmpty()) return ""
    if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
      trimmed = "https://$trimmed"
    }
    while (trimmed.endsWith("/")) {
      trimmed = trimmed.substring(0, trimmed.length - 1)
    }
    return trimmed
  }

  fun buildDeviceUrl(rawUrl: String, deviceId: String, authToken: String): String {
    val clean = cleanFirebaseUrl(rawUrl)
    val urlWithoutJson = if (clean.endsWith(".json")) clean.substring(0, clean.length - 5) else clean
    val devId = deviceId.ifBlank { "kakao-01" }

    val finalUrl = when {
      urlWithoutJson.endsWith("/devices/$devId") -> "$urlWithoutJson.json"
      urlWithoutJson.endsWith("/devices") -> "$urlWithoutJson/$devId.json"
      else -> "$urlWithoutJson/devices/$devId.json"
    }
    return if (authToken.isNotBlank()) "$finalUrl?auth=${authToken.trim()}" else finalUrl
  }

  fun buildControlsUrl(rawUrl: String, deviceId: String, authToken: String): String {
    val clean = cleanFirebaseUrl(rawUrl)
    val urlWithoutJson = if (clean.endsWith(".json")) clean.substring(0, clean.length - 5) else clean
    val devId = deviceId.ifBlank { "kakao-01" }

    val finalUrl = when {
      urlWithoutJson.endsWith("/devices/$devId/controls") -> "$urlWithoutJson.json"
      urlWithoutJson.endsWith("/devices/$devId") -> "$urlWithoutJson/controls.json"
      urlWithoutJson.endsWith("/devices") -> "$urlWithoutJson/$devId/controls.json"
      else -> "$urlWithoutJson/devices/$devId/controls.json"
    }
    return if (authToken.isNotBlank()) "$finalUrl?auth=${authToken.trim()}" else finalUrl
  }

  suspend fun fetchDeviceData(settings: DeviceSettings): Result<DeviceDataDto> =
    withContext(Dispatchers.IO) {
      if (settings.isDemoMode || settings.databaseUrl.isBlank()) {
        return@withContext Result.success(getSimulatedDeviceData())
      }

      val deviceId = settings.deviceId.ifBlank { "kakao-01" }
      val fullUrl = buildDeviceUrl(settings.databaseUrl, deviceId, settings.authToken)

      try {
        val request = Request.Builder().url(fullUrl).get().build()
        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
          return@withContext Result.failure(
            IOException("Firebase HTTP ${response.code}: ${response.message}")
          )
        }

        val bodyString = response.body?.string()
        if (bodyString.isNullOrBlank() || bodyString == "null") {
          return@withContext Result.failure(
            IOException("Data perangkat '$deviceId' tidak ditemukan di database")
          )
        }

        val parsed = parseDeviceDataJson(bodyString, deviceId)
        Result.success(parsed)
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  fun parseDeviceDataJson(bodyString: String, deviceId: String): DeviceDataDto {
    try {
      val map = mapAdapter.fromJson(bodyString) ?: return DeviceDataDto()

      // Support direct node or root tree with "devices": { "kakao-01": { ... } }
      val deviceMap: Map<*, *> = when {
        map.containsKey("devices") && map["devices"] is Map<*, *> -> {
          val devices = map["devices"] as Map<*, *>
          (devices[deviceId] as? Map<*, *>)
            ?: (devices.values.firstOrNull() as? Map<*, *>)
            ?: map
        }
        map.containsKey(deviceId) && map[deviceId] is Map<*, *> -> {
          map[deviceId] as Map<*, *>
        }
        else -> map
      }

      // 1. Controls
      val controlsMap = deviceMap["controls"] as? Map<*, *>
      val controls = if (controlsMap != null) {
        val mode = controlsMap["mode"]?.toString() ?: "auto"
        val manualPump = when (val mp = controlsMap["manualPump"]) {
          is Boolean -> mp
          is String -> mp.toBooleanStrictOrNull() ?: false
          else -> false
        }
        ControlsDto(mode = mode, manualPump = manualPump)
      } else ControlsDto()

      // 2. Pump
      val pumpMap = deviceMap["pump"] as? Map<*, *>
      val pump = if (pumpMap != null) {
        val isOn = when (val onVal = pumpMap["isOn"]) {
          is Boolean -> onVal
          is String -> onVal.toBooleanStrictOrNull() ?: false
          else -> false
        }
        val rawReason = pumpMap["reason"]?.toString()?.trim()
        val reason = if (!rawReason.isNullOrBlank() && !rawReason.equals("null", ignoreCase = true)) {
          rawReason
        } else if (isOn) "manual" else "startup"

        val changedAtEpoch = (pumpMap["changedAtEpoch"] as? Number)?.toLong()
          ?: pumpMap["changedAtEpoch"]?.toString()?.toLongOrNull() ?: 0L

        val explicitElapsed = (pumpMap["elapsedSeconds"] as? Number)?.toLong()
          ?: pumpMap["elapsedSeconds"]?.toString()?.toLongOrNull() ?: 0L

        val elapsed = if (explicitElapsed > 0) {
          explicitElapsed
        } else if (isOn && changedAtEpoch > 0) {
          val nowSec = System.currentTimeMillis() / 1000
          (nowSec - changedAtEpoch).coerceAtLeast(0)
        } else {
          0L
        }

        PumpDataDto(
          isOn = isOn,
          reason = reason,
          elapsedSeconds = elapsed,
          changedAtEpoch = changedAtEpoch,
        )
      } else PumpDataDto()

      // 3. Sensors - Safely handle numeric, null literal, and String "null" / "--"
      val sensorsMap = deviceMap["sensors"] as? Map<*, *>
      val sensors = if (sensorsMap != null) {
        fun parseSafeDouble(key: String): Double? {
          val raw = sensorsMap[key] ?: return null
          if (raw is Number) return raw.toDouble()
          val str = raw.toString().trim()
          if (str.equals("null", ignoreCase = true) || str.isEmpty() || str.equals("nan", ignoreCase = true)) {
            return null
          }
          return str.toDoubleOrNull()
        }

        fun parseSafeInt(key: String): Int? {
          val raw = sensorsMap[key] ?: return null
          if (raw is Number) return raw.toInt()
          val str = raw.toString().trim()
          if (str.equals("null", ignoreCase = true) || str.isEmpty()) return null
          return str.toIntOrNull()
        }

        fun parseSafeLong(key: String): Long? {
          val raw = sensorsMap[key] ?: return null
          if (raw is Number) return raw.toLong()
          val str = raw.toString().trim()
          if (str.equals("null", ignoreCase = true) || str.isEmpty()) return null
          return str.toLongOrNull()
        }

        fun parseSafeBoolean(key: String): Boolean? {
          val raw = sensorsMap[key] ?: return null
          if (raw is Boolean) return raw
          val str = raw.toString().trim()
          if (str.equals("null", ignoreCase = true) || str.isEmpty()) return null
          return str.toBooleanStrictOrNull()
        }

        SensorDataDto(
          temperatureC = parseSafeDouble("temperatureC"),
          humidityAirPct = parseSafeDouble("humidityAirPct"),
          soilMoisturePct = parseSafeDouble("soilMoisturePct"),
          soilRaw = parseSafeInt("soilRaw"),
          dhtValid = parseSafeBoolean("dhtValid"),
          updatedAtEpoch = parseSafeLong("updatedAtEpoch"),
        )
      } else null

      return DeviceDataDto(
        sensors = sensors,
        pump = pump,
        controls = controls,
      )
    } catch (e: Exception) {
      // Fallback to Moshi adapter if needed
      return deviceAdapter.fromJson(bodyString) ?: DeviceDataDto()
    }
  }

  suspend fun updateControls(
    settings: DeviceSettings,
    newMode: String,
    newManualPump: Boolean,
  ): Result<ControlsDto> = withContext(Dispatchers.IO) {
    if (settings.isDemoMode || settings.databaseUrl.isBlank()) {
      demoMode = newMode
      demoManualPump = newManualPump

      if (newMode == "manual") {
        demoPumpOn = newManualPump
        demoReason = "manual"
        if (demoPumpOn) {
          demoPumpStartTimeSeconds = System.currentTimeMillis() / 1000
        }
      } else {
        // Auto mode logic immediately responds
        if (demoMoisture <= 35.0) {
          demoPumpOn = true
          demoReason = "auto_soil_dry"
          demoPumpStartTimeSeconds = System.currentTimeMillis() / 1000
        } else if (demoMoisture >= 55.0) {
          demoPumpOn = false
          demoReason = "auto_soil_moist"
        }
      }

      delay(300) // Realistic tactile feel
      return@withContext Result.success(ControlsDto(mode = demoMode, manualPump = demoManualPump))
    }

    val deviceId = settings.deviceId.ifBlank { "kakao-01" }
    val fullUrl = buildControlsUrl(settings.databaseUrl, deviceId, settings.authToken)

    try {
      val controlsPayload = ControlsDto(mode = newMode, manualPump = newManualPump)
      val jsonString = controlsAdapter.toJson(controlsPayload)
      val body = jsonString.toRequestBody("application/json; charset=utf-8".toMediaType())

      // PATCH updates the controls node atomically
      val request = Request.Builder().url(fullUrl).patch(body).build()
      val response = okHttpClient.newCall(request).execute()

      if (!response.isSuccessful) {
        return@withContext Result.failure(
          IOException("Gagal kirim kontrol: HTTP ${response.code} ${response.message}")
        )
      }

      Result.success(controlsPayload)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun getSimulatedDeviceData(): DeviceDataDto {
    val now = System.currentTimeMillis() / 1000

    // Simulate realistic physics
    if (demoPumpOn) {
      // Pump is watering the soil: moisture rises
      demoMoisture = min(70.0, demoMoisture + 1.8 + Random.nextDouble(0.2, 0.6))
      // Raw ADC drops as capacitive soil sensor detects higher moisture
      demoSoilRaw = max(1600, (3200 - (demoMoisture * 22)).roundToInt())

      val elapsedSeconds = now - demoPumpStartTimeSeconds

      // Check safety timeout (3 minutes = 180s)
      if (elapsedSeconds >= 180) {
        demoPumpOn = false
        demoReason = "safety_timeout"
        if (demoMode == "manual") {
          demoManualPump = false
        }
      } else if (demoMode == "auto" && demoMoisture >= 55.0) {
        // Auto threshold reached
        demoPumpOn = false
        demoReason = "auto_soil_moist"
      }
    } else {
      // Pump is off: soil slowly dries
      demoMoisture = max(20.0, demoMoisture - (0.4 + Random.nextDouble(0.0, 0.3)))
      demoSoilRaw = min(3300, (3200 - (demoMoisture * 22)).roundToInt())

      if (demoMode == "auto" && demoMoisture <= 35.0) {
        demoPumpOn = true
        demoReason = "auto_soil_dry"
        demoPumpStartTimeSeconds = now
      }
    }

    // Small natural fluctuations in temperature and air humidity
    demoTemp = (demoTemp + Random.nextDouble(-0.15, 0.15)).coerceIn(26.0, 33.5)
    demoHumidity = (demoHumidity + Random.nextDouble(-0.3, 0.3)).coerceIn(60.0, 88.0)

    val roundedMoisture = (demoMoisture * 10.0).roundToInt() / 10.0
    val roundedTemp = (demoTemp * 10.0).roundToInt() / 10.0
    val roundedHumidity = (demoHumidity * 10.0).roundToInt() / 10.0

    val elapsed = if (demoPumpOn) (now - demoPumpStartTimeSeconds).coerceAtLeast(0) else 0L

    return DeviceDataDto(
      sensors =
        SensorDataDto(
          temperatureC = roundedTemp,
          humidityAirPct = roundedHumidity,
          soilMoisturePct = roundedMoisture,
          soilRaw = demoSoilRaw,
          dhtValid = true,
          updatedAtEpoch = now,
        ),
      pump =
        PumpDataDto(
          isOn = demoPumpOn,
          reason = demoReason,
          elapsedSeconds = elapsed,
        ),
      controls =
        ControlsDto(
          mode = demoMode,
          manualPump = demoManualPump,
        ),
    )
  }

  fun adjustDemoMoisture(delta: Double) {
    demoMoisture = (demoMoisture + delta).coerceIn(15.0, 85.0)
    demoSoilRaw = (3200 - (demoMoisture * 22)).roundToInt().coerceIn(1500, 3400)
    val now = System.currentTimeMillis() / 1000
    if (demoMode == "auto") {
      if (demoMoisture <= 35.0 && !demoPumpOn) {
        demoPumpOn = true
        demoReason = "auto_soil_dry"
        demoPumpStartTimeSeconds = now
      } else if (demoMoisture >= 55.0 && demoPumpOn) {
        demoPumpOn = false
        demoReason = "auto_soil_moist"
      }
    }
  }

  fun triggerThresholdDryDemo() {
    demoMoisture = 32.0
    demoSoilRaw = 2950
    val now = System.currentTimeMillis() / 1000
    if (demoMode == "auto") {
      demoPumpOn = true
      demoReason = "auto_soil_dry"
      demoPumpStartTimeSeconds = now
    }
  }
}
