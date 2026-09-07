package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SoilHistoryPoint
import com.example.ui.theme.SoilDryRed
import com.example.ui.theme.SoilOptimalGreen

@Composable
fun SoilTrendChart(
  historyPoints: List<SoilHistoryPoint>,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth().testTag("soil_trend_chart"),
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
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ShowChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Tren Kelembapan",
            style =
              MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              ),
          )
        }

        Text(
          text = "${historyPoints.size} titik data",
          style =
            MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Minimal Legend Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        LegendDot(color = SoilDryRed.copy(alpha = 0.8f), label = "35% Ambang Nyala")
        LegendDot(color = SoilOptimalGreen.copy(alpha = 0.8f), label = "55% Ambang Mati")
      }

      Spacer(modifier = Modifier.height(14.dp))

      val chartLineColor = MaterialTheme.colorScheme.primary

      if (historyPoints.size < 2) {
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .height(130.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "Mengumpulkan riwayat telemetri...",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
          )
        }
      } else {
        Canvas(
          modifier =
            Modifier.fillMaxWidth()
              .height(130.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
              .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
          val width = size.width
          val height = size.height
          val minY = 10f
          val maxY = 85f

          fun getY(value: Double): Float {
            val normalized = (value.toFloat() - minY) / (maxY - minY)
            return height - (normalized.coerceIn(0f, 1f) * height)
          }

          val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

          // 35% line
          val y35 = getY(35.0)
          drawLine(
            color = SoilDryRed.copy(alpha = 0.35f),
            start = Offset(0f, y35),
            end = Offset(width, y35),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dash,
          )

          // 55% line
          val y55 = getY(55.0)
          drawLine(
            color = SoilOptimalGreen.copy(alpha = 0.35f),
            start = Offset(0f, y55),
            end = Offset(width, y55),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dash,
          )

          val stepX = width / (historyPoints.size - 1).coerceAtLeast(1)
          val points =
            historyPoints.mapIndexed { index, pt ->
              Offset(x = index * stepX, y = getY(pt.soilMoisturePct))
            }

          val lineColor = chartLineColor

          // Smooth gradient area under curve
          if (points.isNotEmpty()) {
            val fillPath = Path()
            fillPath.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
              val p0 = points[i - 1]
              val p1 = points[i]
              val cx = (p0.x + p1.x) / 2
              fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }
            fillPath.lineTo(points.last().x, height)
            fillPath.lineTo(points.first().x, height)
            fillPath.close()

            drawPath(
              path = fillPath,
              brush =
                Brush.verticalGradient(
                  colors = listOf(lineColor.copy(alpha = 0.15f), Color.Transparent),
                  startY = 0f,
                  endY = height,
                ),
            )
          }

          // Line stroke
          if (points.isNotEmpty()) {
            val strokePath = Path()
            strokePath.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
              val p0 = points[i - 1]
              val p1 = points[i]
              val cx = (p0.x + p1.x) / 2
              strokePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }

            drawPath(
              path = strokePath,
              color = lineColor,
              style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
            )
          }

          // Minimal Dots
          points.forEachIndexed { index, pt ->
            val isLast = index == points.lastIndex
            if (isLast) {
              drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
              drawCircle(color = lineColor, radius = 3.5.dp.toPx(), center = pt)
            } else {
              drawCircle(color = lineColor, radius = 2.dp.toPx(), center = pt)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun LegendDot(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      style =
        MaterialTheme.typography.bodySmall.copy(
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
  }
}
