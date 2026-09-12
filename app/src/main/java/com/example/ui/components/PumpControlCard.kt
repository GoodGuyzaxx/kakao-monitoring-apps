package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ControlsDto
import com.example.model.PumpDataDto
import com.example.ui.theme.PumpActiveBg
import com.example.ui.theme.PumpActiveBorder
import com.example.ui.theme.PumpActiveColor
import com.example.ui.theme.PumpInactiveBg
import com.example.ui.theme.PumpInactiveBorder
import com.example.ui.theme.PumpInactiveColor

fun formatPumpReason(reason: String?): String {
  return when (reason?.lowercase()?.trim()) {
    "startup" -> "Sistem siap: Inisialisasi awal (ESP32 baru menyala)."
    "auto_soil_dry" -> "Otomatis: Tanah Kering (≤ 35%), pompa diaktifkan."
    "auto_soil_moist" -> "Otomatis: Target kelembapan tanah (≥ 55%) terpenuhi."
    "manual" -> "Perintah Manual: Dikontrol oleh pengguna melalui aplikasi."
    "safety_timeout" -> "Pengaman: Pompa dimatikan otomatis setelah 3 menit."
    else -> "Sistem siap dan memantau status kelembapan."
  }
}

@Composable
fun PumpControlCard(
  pumpData: PumpDataDto,
  controls: ControlsDto,
  isUpdating: Boolean,
  currentMoisture: Double = 42.0,
  onModeChange: (String) -> Unit,
  onManualPumpToggle: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val isPumpOn = pumpData.isOn
  val currentMode = controls.mode

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by
    infiniteTransition.animateFloat(
      initialValue = 1f,
      targetValue = if (isPumpOn) 1.25f else 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(900, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "pulseScale",
    )

  Card(
    modifier = modifier.fillMaxWidth().testTag("pump_control_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .border(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            RoundedCornerShape(20.dp),
          )
          .padding(20.dp)
    ) {
      // Header: Title and Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Pompa Irigasi",
            style =
              MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              ),
          )
          Text(
            text = "Relay 12V • GPIO26",
            style =
              MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
              ),
          )
        }

        // Clean Status Pill
        val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        val pillBg = if (isPumpOn) {
          if (isDark) Color(0xFF0F382A) else PumpActiveBg
        } else {
          if (isDark) Color(0xFF1F2923) else PumpInactiveBg
        }
        val pillBorder = if (isPumpOn) {
          if (isDark) Color(0xFF1B634B) else PumpActiveBorder
        } else {
          if (isDark) Color(0xFF2E3D35) else PumpInactiveBorder
        }
        val pillColor = if (isPumpOn) {
          if (isDark) Color(0xFF6EE7B7) else PumpActiveColor
        } else {
          if (isDark) Color(0xFF94A3B8) else PumpInactiveColor
        }

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = pillBg,
          border = androidx.compose.foundation.BorderStroke(1.dp, pillBorder),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
              modifier =
                Modifier.size(7.dp)
                  .scale(if (isPumpOn) pulseScale else 1f)
                  .clip(CircleShape)
                  .background(pillColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isPumpOn) "Aktif Menyiram" else "Mati (Standby)",
              style =
                MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = pillColor,
                ),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Clean Minimal Mode Segmented Switch
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          MinimalSegmentButton(
            title = "Mode Otomatis",
            isSelected = currentMode == "auto",
            icon = Icons.Default.AutoMode,
            onClick = { onModeChange("auto") },
            modifier = Modifier.weight(1f).testTag("mode_auto_button"),
          )

          MinimalSegmentButton(
            title = "Mode Manual",
            isSelected = currentMode == "manual",
            icon = Icons.Default.TouchApp,
            onClick = { onModeChange("manual") },
            modifier = Modifier.weight(1f).testTag("mode_manual_button"),
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Mode Specific Section
      if (currentMode == "auto") {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.background,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text(
                  text = "Ambang Nyala",
                  style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp),
                )
                Text(
                  text = "≤ 35% (Kering)",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "Ambang Mati",
                  style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp),
                )
                Text(
                  text = "≥ 55% (Lembap)",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text =
                if (isPumpOn) "Pompa aktif menyiram hingga target 55% tercapai."
                else "Pompa siap menyala otomatis jika kelembapan turun ke ≤ 35%.",
              style =
                MaterialTheme.typography.bodySmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 12.sp,
                ),
            )
          }
        }
      } else {
        // Manual Action
        Column(modifier = Modifier.fillMaxWidth()) {
          Button(
            onClick = { onManualPumpToggle(!isPumpOn) },
            enabled = !isUpdating,
            shape = RoundedCornerShape(12.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor =
                  if (isPumpOn) Color(0xFFDC2626) // Clean red
                  else Color(0xFF059669), // Clean emerald
                contentColor = Color.White,
              ),
            modifier =
              Modifier.fillMaxWidth()
                .height(48.dp)
                .testTag("manual_pump_toggle_button"),
          ) {
            if (isUpdating) {
              CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Mengirim Perintah...", style = MaterialTheme.typography.labelLarge)
            } else {
              Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isPumpOn) "Matikan Pompa" else "Nyalakan Pompa",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "membaca perubahan manual tiap ~5 detik.",
            style =
              MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              ),
            modifier = Modifier.align(Alignment.CenterHorizontally),
          )
        }
      }

      // Safety timer (Only shown when pump is actively running)
      if (isPumpOn) {
        Spacer(modifier = Modifier.height(12.dp))
        val maxSeconds = 120f
        val elapsed =
          if (pumpData.elapsedSeconds > 0) pumpData.elapsedSeconds
          else if (pumpData.changedAtEpoch > 0) {
            val nowSec = System.currentTimeMillis() / 1000
            (nowSec - pumpData.changedAtEpoch).coerceAtLeast(0)
          } else 0L
        val progress = (elapsed / maxSeconds).coerceIn(0f, 1f)
        val minutes = elapsed / 60
        val seconds = elapsed % 60
        val timeStr = String.format("%02d:%02d", minutes, seconds)

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.background,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = PumpActiveColor,
                modifier = Modifier.size(15.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Durasi Aktif: $timeStr / 03:00 (Batas Otomatis)",
                style =
                  MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  ),
              )
            }

            Box(modifier = Modifier.width(60.dp)) {
              LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = PumpActiveColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Reason footnote
      Text(
        text = formatPumpReason(pumpData.reason),
        style =
          MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
          ),
      )
    }
  }
}

@Composable
private fun MinimalSegmentButton(
  title: String,
  isSelected: Boolean,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val bgColor by
    animateColorAsState(
      targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
      label = "btnBg",
    )
  val textColor by
    animateColorAsState(
      targetValue =
        if (isSelected) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurfaceVariant,
      label = "btnText",
    )

  Surface(
    shape = RoundedCornerShape(9.dp),
    color = bgColor,
    shadowElevation = if (isSelected) 1.dp else 0.dp,
    modifier = modifier.clickable { onClick() },
  ) {
    Row(
      modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(16.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = title,
        style =
          MaterialTheme.typography.labelMedium.copy(
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
          ),
      )
    }
  }
}
