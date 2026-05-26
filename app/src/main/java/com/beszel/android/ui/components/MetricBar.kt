package com.beszel.android.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.beszel.android.ui.theme.warning

@Composable
fun LinearMeter(
    value: Float,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
    color: Color? = null,
    track: Color? = null,
) {
    val colors = MaterialTheme.colorScheme
    val fg = color ?: when {
        value > 90f -> colors.error
        value > 75f -> colors.warning
        else        -> colors.primary
    }
    val bg = track ?: colors.surfaceContainerHighest
    val clamped = (value / 100f).coerceIn(0f, 1f)
    val animated by animateFloatAsState(targetValue = clamped, animationSpec = tween(600), label = "meter")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height))
            .background(bg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(RoundedCornerShape(height))
                .background(fg)
        )
    }
}
