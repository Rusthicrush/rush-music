package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCardBackground

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    borderStroke: BorderStroke? = BorderStroke(1.dp, GlassBorder),
    glowColor: Color = Color.Transparent,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    val shadowModifier = if (glowColor != Color.Transparent) {
        modifier.drawBehind {
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = glowColor.copy(alpha = 0.25f)
                    asFrameworkPaint().apply {
                        maskFilter = android.graphics.BlurMaskFilter(
                            18f,
                            android.graphics.BlurMaskFilter.Blur.OUTER
                        )
                    }
                }
                canvas.drawRoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    radiusX = cornerRadius.toPx(),
                    radiusY = cornerRadius.toPx(),
                    paint = paint
                )
            }
        }
    } else {
        modifier
    }

    Surface(
        modifier = shadowModifier
            .clip(shape)
            .let { m ->
                if (onClick != null) m.clickable { onClick() } else m
            },
        color = GlassCardBackground,
        shape = shape,
        border = borderStroke,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        content = content
    )
}
