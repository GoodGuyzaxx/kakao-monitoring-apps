package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppThemeMode
import com.example.model.DeviceSettings

class AppPreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("cacao_monitor_prefs", Context.MODE_PRIVATE)

  fun getSettings(): DeviceSettings {
    val dbUrl = prefs.getString(KEY_DB_URL, "") ?: ""
    val deviceId = prefs.getString(KEY_DEVICE_ID, "kakao-01") ?: "kakao-01"
    val authToken = prefs.getString(KEY_AUTH_TOKEN, "") ?: ""
    val interval = prefs.getInt(KEY_POLL_INTERVAL, 4)
    // If dbUrl is empty, default to demo mode so the user can interact immediately
    val isDemo = prefs.getBoolean(KEY_DEMO_MODE, dbUrl.isBlank())

    return DeviceSettings(
      databaseUrl = dbUrl,
      deviceId = deviceId,
      authToken = authToken,
      pollIntervalSeconds = interval,
      isDemoMode = isDemo,
    )
  }

  fun saveSettings(settings: DeviceSettings) {
    prefs.edit()
      .putString(KEY_DB_URL, settings.databaseUrl.trim())
      .putString(KEY_DEVICE_ID, settings.deviceId.trim())
      .putString(KEY_AUTH_TOKEN, settings.authToken.trim())
      .putInt(KEY_POLL_INTERVAL, settings.pollIntervalSeconds)
      .putBoolean(KEY_DEMO_MODE, settings.isDemoMode)
      .apply()
  }

  fun getThemeMode(): AppThemeMode {
    val raw = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
    return try {
      AppThemeMode.valueOf(raw)
    } catch (e: Exception) {
      AppThemeMode.SYSTEM
    }
  }

  fun saveThemeMode(themeMode: AppThemeMode) {
    prefs.edit()
      .putString(KEY_THEME_MODE, themeMode.name)
      .apply()
  }

  companion object {
    private const val KEY_DB_URL = "db_url"
    private const val KEY_DEVICE_ID = "device_id"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_POLL_INTERVAL = "poll_interval"
    private const val KEY_DEMO_MODE = "demo_mode"
    private const val KEY_THEME_MODE = "app_theme_mode"
  }
}
