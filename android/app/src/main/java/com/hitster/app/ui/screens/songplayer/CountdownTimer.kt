package com.hitster.app.ui.screens.songplayer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

@Composable
fun CountdownTimer(
    remainingMillis: Long,
    totalDurationMillis: Long,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val progress = if (totalDurationMillis > 0) {
        remainingMillis.toFloat() / totalDurationMillis.toFloat()
    } else {
        0f
    }
    
    val secondsRemaining = ceil(remainingMillis / 1000f).toInt()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(100.dp)
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            // Draw background track
            drawCircle(
                color = trackColor,
                style = Stroke(width = 8.dp.toPx())
            )
            
            // Draw progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        Text(
            text = secondsRemaining.toString(),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
