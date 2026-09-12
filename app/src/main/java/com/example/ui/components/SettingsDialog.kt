package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppThemeMode
import com.example.model.DeviceSettings

@Composable
fun SettingsDialog(
  currentSettings: DeviceSettings,
  currentThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
  onDismiss: () -> Unit,
  onSave: (DeviceSettings) -> Unit,
  onThemeModeChange: (AppThemeMode) -> Unit = {},
) {
  var databaseUrl by remember { mutableStateOf(currentSettings.databaseUrl) }
  var deviceId by remember { mutableStateOf(currentSettings.deviceId) }
  var authToken by remember { mutableStateOf(currentSettings.authToken) }
  var pollInterval by remember { mutableStateOf(currentSettings.pollIntervalSeconds) }
  var isDemoMode by remember { mutableStateOf(currentSettings.isDemoMode) }
  var selectedTheme by remember { mutableStateOf(currentThemeMode) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier.fillMaxWidth(0.95f)
          .padding(vertical = 24.dp)
          .testTag("settings_dialog"),
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
    ) {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
      ) {
        // Top Title
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Memory,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Konfigurasi ESP32 & Firebase",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              )
              Text(
                text = "Pengaturan koneksi realtime database",
                style =
                  MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  ),
              )
            }
          }

          IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_button")) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        // Theme Selection Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth().testTag("theme_selection_card"),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Tema Tampilan",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                  text = "Pilih mode tampilan sesuai kenyamanan Anda",
                  style =
                    MaterialTheme.typography.bodySmall.copy(
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Segmented Options
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              val themeOptions =
                listOf(
                  Triple(AppThemeMode.SYSTEM, "Sistem", Icons.Default.BrightnessAuto),
                  Triple(AppThemeMode.LIGHT, "Terang", Icons.Default.LightMode),
                  Triple(AppThemeMode.DARK, "Gelap", Icons.Default.DarkMode),
                )

              themeOptions.forEach { (mode, label, icon) ->
                val isSelected = selectedTheme == mode
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color =
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface,
                  border =
                    androidx.compose.foundation.BorderStroke(
                      1.dp,
                      if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.outlineVariant,
                    ),
                  modifier =
                    Modifier.weight(1f)
                      .clip(RoundedCornerShape(12.dp))
                      .clickable {
                        selectedTheme = mode
                        onThemeModeChange(mode)
                      }
                      .testTag("theme_option_${mode.name.lowercase()}"),
                ) {
                  Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Icon(
                      imageVector = icon,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                      tint =
                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = label,
                      style =
                        MaterialTheme.typography.labelMedium.copy(
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                          color =
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                          fontSize = 12.sp,
                        ),
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Demo Mode Toggle Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors =
            CardDefaults.cardColors(
              containerColor =
                if (isDemoMode) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f),
            ) {
              Icon(
                imageVector = Icons.Default.Science,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Mode Simulasi (Demo)",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                  text = "Uji coba monitoring & kontrol pompa tanpa perlu hardware ESP32 aktif.",
                  style =
                    MaterialTheme.typography.bodySmall.copy(
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
              }
            }
            Switch(
              checked = isDemoMode,
              onCheckedChange = { isDemoMode = it },
              modifier = Modifier.testTag("demo_mode_switch"),
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Firebase URL
        OutlinedTextField(
          value = databaseUrl,
          onValueChange = { databaseUrl = it },
          label = { Text("Firebase Realtime Database URL") },
          placeholder = { Text("https://kakao-smart-default-rtdb.firebaseio.com") },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Storage, contentDescription = null)
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("firebase_url_input"),
          shape = RoundedCornerShape(14.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Device ID
        OutlinedTextField(
          value = deviceId,
          onValueChange = { deviceId = it },
          label = { Text("Device ID (Sesuai ESP32)") },
          placeholder = { Text("kakao-01") },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Devices, contentDescription = null)
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("device_id_input"),
          shape = RoundedCornerShape(14.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Auth Token
        OutlinedTextField(
          value = authToken,
          onValueChange = { authToken = it },
          label = { Text("Database Secret / Auth Token (Opsional)") },
          placeholder = { Text("Biarkan kosong jika aturan database public") },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Key, contentDescription = null)
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("auth_token_input"),
          shape = RoundedCornerShape(14.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Polling interval selector
        Text(
          text = "Interval Pembaruan Data: $pollInterval detik",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          listOf(3, 5, 10).forEach { seconds ->
            val isSelected = pollInterval == seconds
            Surface(
              shape = RoundedCornerShape(12.dp),
              color =
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
              modifier =
                Modifier.weight(1f)
                  .clickable { pollInterval = seconds }
                  .padding(vertical = 4.dp),
            ) {
              Text(
                text = "$seconds Detik",
                color =
                  if (isSelected) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.onSurfaceVariant,
                style =
                  MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                  ),
                modifier = Modifier.padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hardware Reference Summary
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Cable,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Pinout Rangkaian ESP32 (Firmware MD)",
                style =
                  MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                  ),
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text =
                "• DHT22 DATA: GPI13 (3.3V)\n• Soil Sensor AOUT: GPIO34 (3.3V Max)\n• Relay IN: GPIO26 (5V/GND bersama)\n• Pompa: Catu daya 12V tersendiri lewat COM-NO",
              style =
                MaterialTheme.typography.bodySmall.copy(
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.textButtonColors(),
          ) {
            Text("Batal")
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = {
              onSave(
                DeviceSettings(
                  databaseUrl = databaseUrl,
                  deviceId = deviceId.ifBlank { "kakao-01" },
                  authToken = authToken,
                  pollIntervalSeconds = pollInterval,
                  isDemoMode = isDemoMode,
                )
              )
              onDismiss()
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("save_settings_button"),
          ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Simpan Konfigurasi")
          }
        }
      }
    }
  }
}
