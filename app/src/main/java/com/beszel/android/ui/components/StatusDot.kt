package com.beszel.android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.beszel.android.data.model.SystemStatus
import com.beszel.android.ui.theme.success

@Composable
fun StatusDot(status: SystemStatus, size: Dp = 10.dp, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val color = when (status) {
        SystemStatus.Up      -> colors.success
        SystemStatus.Down    -> colors.error
        SystemStatus.Unknown -> colors.tertiary  // warn
        SystemStatus.Paused  -> colors.outline
        SystemStatus.Pending -> colors.outline
    }

    Box(modifier = modifier.size(size + 4.dp), contentAlignment = Alignment.Center) {
        if (status == SystemStatus.Up) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseOut),
                    repeatMode = RepeatMode.Restart,
                ), label = "scale"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.35f, targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseOut),
                    repeatMode = RepeatMode.Restart,
                ), label = "alpha"
            )
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha))
            )
        }
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}
