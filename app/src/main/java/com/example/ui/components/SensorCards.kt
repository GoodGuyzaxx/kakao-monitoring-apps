package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SensorDataDto
import com.example.model.SoilMoistureLevel
import com.example.ui.theme.SoilDryRed
import com.example.ui.theme.SoilDryRedBg
import com.example.ui.theme.SoilDryRedBorder
import com.example.ui.theme.SoilOptimalGreen
import com.example.ui.theme.SoilOptimalGreenBg
import com.example.ui.theme.SoilOptimalGreenBorder
import com.example.ui.theme.SoilWetBlue
import com.example.ui.theme.SoilWetBlueBg
import com.example.ui.theme.SoilWetBlueBorder

@Composable
fun SoilMoistureCard(
  sensorData: SensorDataDto?,
  modifier: Modifier = Modifier,
) {
  val moisture = sensorData?.soilMoisturePct ?: 0.0
  val soilRaw = sensorData?.soilRaw ?: 0
  val level = SoilMoistureLevel.fromMoisture(moisture)

  val animatedProgress by
    animateFloatAsState(
      targetValue = (moisture / 100.0).toFloat().coerceIn(0f, 1f),
      animationSpec = tween(durationMillis = 600),
      label = "soilProgress",
    )

  val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

  val (statusText, statusColor, statusBg, statusBorder) =
    when (level) {
      SoilMoistureLevel.DRY ->
        Tuple4(
          "Kering (≤ 35%)",
          if (isDark) Color(0xFFF87171) else SoilDryRed,
          if (isDark) Color(0xFF381212) else SoilDryRedBg,
          if (isDark) Color(0xFF651B1B) else SoilDryRedBorder,
        )
      SoilMoistureLevel.OPTIMAL ->
        Tuple4(
          "Optimal (36% - 54%)",
          if (isDark) Color(0xFF4ADE80) else SoilOptimalGreen,
          if (isDark) Color(0xFF0F361F) else SoilOptimalGreenBg,
          if (isDark) Color(0xFF165C32) else SoilOptimalGreenBorder,
        )
      SoilMoistureLevel.WET ->
        Tuple4(
          "Lembap (≥ 55%)",
          if (isDark) Color(0xFF38BDF8) else SoilWetBlue,
          if (isDark) Color(0xFF0F2E44) else SoilWetBlueBg,
          if (isDark) Color(0xFF154C72) else SoilWetBlueBorder,
        )
    }

  Card(
    modifier = modifier.fillMaxWidth().testTag("soil_moisture_card"),
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
      // Top Label & Status Pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Grass,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Kelembapan Tanah",
            style =
              MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              ),
          )
        }

        // Status Pill
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = statusBg,
          border = androidx.compose.foundation.BorderStroke(1.dp, statusBorder),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = statusText,
              style =
                MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = statusColor,
                ),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Clean Big Number
      Row(
        verticalAlignment = Alignment.Bottom,
      ) {
        Text(
          text = String.format("%.1f", moisture),
          style =
            MaterialTheme.typography.displayMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            ),
        )
        Text(
          text = "%",
          style =
            MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
          modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Minimal Progress Track
      Box(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
          progress = { animatedProgress },
          modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
          color = statusColor,
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Footnote
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Ambang otomatis: 35% ON • 55% OFF",
          style =
            MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Text(
          text = "$soilRaw ADC",
          style =
            MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Medium,
            ),
        )
      }
    }
  }
}

@Composable
fun AtmosphereSensorsRow(
  sensorData: SensorDataDto?,
  modifier: Modifier = Modifier,
) {
  val isDhtValid = sensorData?.dhtValid != false && sensorData?.temperatureC != null
  val temp = sensorData?.temperatureC
  val humidity = sensorData?.humidityAirPct

  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Temperature Card
    Card(
      modifier = Modifier.weight(1f).testTag("temperature_card"),
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
            .padding(16.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.DeviceThermostat,
            contentDescription = null,
            tint = Color(0xFFE07A5F),
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Suhu Udara",
            style =
              MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              ),
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = if (temp != null) String.format("%.1f", temp) else "--",
            style =
              MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color =
                  if (temp != null) MaterialTheme.colorScheme.onSurface
                  else MaterialTheme.colorScheme.onSurfaceVariant,
              ),
          )
          Text(
            text = "°C",
            style =
              MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              ),
            modifier = Modifier.padding(bottom = 3.dp, start = 2.dp),
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text =
            when {
              temp == null || !isDhtValid -> "Sensor offline (Cek GPIO4)"
              temp in 24.0..32.0 -> "Optimal (24-32°C)"
              else -> "Di luar optimal"
            },
          style =
            MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              color =
                if (temp != null && isDhtValid && temp in 24.0..32.0) SoilOptimalGreen
                else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
      }
    }

    // Humidity Card
    Card(
      modifier = Modifier.weight(1f).testTag("humidity_card"),
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
            .padding(16.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Air,
            contentDescription = null,
            tint = Color(0xFF3D5A80),
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Kelembapan Udara",
            style =
              MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              ),
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = if (humidity != null) String.format("%.1f", humidity) else "--",
            style =
              MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color =
                  if (humidity != null) MaterialTheme.colorScheme.onSurface
                  else MaterialTheme.colorScheme.onSurfaceVariant,
              ),
          )
          Text(
            text = "%",
            style =
              MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              ),
            modifier = Modifier.padding(bottom = 3.dp, start = 2.dp),
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text =
            when {
              humidity == null || !isDhtValid -> "Sensor offline (Cek DHT22)"
              humidity >= 70.0 -> "Lembap (Ideal)"
              else -> "Sedang"
            },
          style =
            MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              color =
                if (humidity != null && isDhtValid && humidity >= 70.0) SoilWetBlue
                else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
      }
    }
  }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
