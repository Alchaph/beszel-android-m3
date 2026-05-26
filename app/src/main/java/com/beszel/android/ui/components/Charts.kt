package com.beszel.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

@Composable
fun Sparkline(
    data: List<Float>,
    modifier: Modifier = Modifier,
    color: Color? = null,
    height: Float = 36f,
    fill: Boolean = true,
) {
    val strokeColor = color ?: MaterialTheme.colorScheme.primary
    if (data.size < 2) return
    Canvas(modifier = modifier.fillMaxWidth().height(height.dp)) {
        val w = size.width
        val h = size.height
        val maxV = data.max().coerceAtLeast(1f)
        val minV = data.min()
        val range = (maxV - minV).coerceAtLeast(1f)

        val pts = data.mapIndexed { i, v ->
            Offset(
                x = i / (data.size - 1f) * w,
                y = h - ((v - minV) / range * h),
            )
        }
        if (fill) {
            val path = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                pts.drop(1).forEach { lineTo(it.x, it.y) }
                lineTo(w, h); lineTo(0f, h); close()
            }
            drawPath(path, Brush.verticalGradient(
                listOf(strokeColor.copy(alpha = 0.35f), strokeColor.copy(alpha = 0f)),
                startY = 0f, endY = h,
            ))
        }
        val linePath = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(linePath, strokeColor, style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round,
        ))
    }
}

@Composable
fun BigChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    color: Color? = null,
    maxOverride: Float? = null,
    fillStrong: Boolean = false,
) {
    val chartColor = color ?: MaterialTheme.colorScheme.primary
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    if (data.size < 2) {
        Spacer(modifier = modifier.fillMaxWidth().height(140.dp))
        return
    }

    Canvas(modifier = modifier.fillMaxWidth().height(140.dp)) {
        val w = size.width
        val h = size.height
        val pl = 36f; val pr = 8f; val pt = 8f; val pb = 20f
        val cw = w - pl - pr
        val ch = h - pt - pb
        val maxV = maxOverride ?: 100f

        val gridYs = listOf(0f, 0.25f, 0.5f, 0.75f, 1f).map { pt + it * ch }
        gridYs.forEachIndexed { i, y ->
            drawLine(
                outlineVariant,
                Offset(pl, y), Offset(pl + cw, y),
                strokeWidth = 0.8f,
                pathEffect = if (i < 4) PathEffect.dashPathEffect(floatArrayOf(4f, 6f)) else null,
            )
        }

        val pts = data.mapIndexed { i, v ->
            Offset(
                x = pl + i / (data.size - 1f) * cw,
                y = pt + (1f - (v / maxV).coerceIn(0f, 1f)) * ch,
            )
        }

        val fillPath = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
            lineTo(pl + cw, pt + ch); lineTo(pl, pt + ch); close()
        }
        drawPath(fillPath, Brush.verticalGradient(
            listOf(
                chartColor.copy(alpha = if (fillStrong) 0.5f else 0.3f),
                chartColor.copy(alpha = 0f),
            ),
            startY = pt, endY = pt + ch,
        ))

        val linePath = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(linePath, chartColor, style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round,
        ))

        // End dot
        val last = pts.last()
        drawCircle(chartColor.copy(alpha = 0.2f), radius = 8f, center = last)
        drawCircle(chartColor, radius = 3.5f, center = last)
    }
}
