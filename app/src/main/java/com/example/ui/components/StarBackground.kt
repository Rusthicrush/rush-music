package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.AppThemePreset
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random

// Represents a star in our serene custom sky
private data class UniverseParticle(
    val xPercent: Float,
    val yPercent: Float,
    val size: Float,
    val offsetPhase: Float,
    val pulseSpeed: Float
)

@Composable
fun StarBackground(
    theme: AppThemePreset,
    content: @Composable () -> Unit
) {
    // Generate static particles once
    val particles = remember {
        List(90) {
            UniverseParticle(
                xPercent = Random.nextFloat(),
                yPercent = Random.nextFloat(),
                size = Random.nextFloat() * 1.8f + 1.2f,
                offsetPhase = Random.nextFloat() * Math.PI.toFloat(),
                pulseSpeed = Random.nextFloat() * 1.2f + 0.6f
            )
        }
    }

    // A simple infinite timer to drive star twinkling, grid scanning, or aurora movement
    val infiniteTransition = rememberInfiniteTransition(label = "stellar_clock")
    val animFactor by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stellar_phase"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw central background gradient
            val gradientBrush = Brush.verticalGradient(
                colors = listOf(theme.bgStart, theme.bgEnd),
                startY = 0f,
                endY = h
            )
            drawRect(brush = gradientBrush, size = size)

            // 2. Draw Theme-specific Celestial Atmospheric Artwork
            when (theme) {
                AppThemePreset.MIDNIGHT_SKY -> {
                    // Classic crescent moon
                    drawMoon(this, theme.bgStart)
                }
                AppThemePreset.NEON_DUSK -> {
                    // Synthwave cyberpunk grid perspective lines
                    drawCyberGrid(this, theme, animFactor)
                }
                AppThemePreset.AURORA_FOREST -> {
                    // Floating emerald aurora curtains
                    drawAuroraBands(this, theme, animFactor)
                }
                AppThemePreset.ROSE_QUARTZ -> {
                    // Floating soft nebula radial spots
                    drawWarmNebulas(this, theme)
                }
                AppThemePreset.SOLAR_ECLIPSE -> {
                    // Giant black sun with blazing golden corona flares
                    drawSolarEclipse(this, theme, animFactor)
                }
                AppThemePreset.CYAN_GLACIER -> {
                    // Sharp iceberg crystalline structures
                    drawGlacialCrystals(this, theme, animFactor)
                }
                AppThemePreset.AMETHYST_FALLS -> {
                    // Vertical fluid lavender waterfall streams
                    drawAmethystFalls(this, theme, animFactor)
                }
                AppThemePreset.DESERT_MONSOON -> {
                    // Sandstone canyon dunes and vertical desert rainfall
                    drawDesertMonsoon(this, theme, animFactor)
                }
                AppThemePreset.COSMIC_NEBULA -> {
                    // Expanding deep multi-colored stellar nebulae
                    drawCosmicNebula(this, theme, animFactor)
                }
            }

            // 3. Draw Universe custom glowing particles
            for (p in particles) {
                val alpha = (sin(animFactor * p.pulseSpeed + p.offsetPhase) + 1f) / 2f
                val glows = p.size * (0.3f + 0.7f * alpha)
                
                // Outer glow representation
                drawCircle(
                    color = theme.starColor.copy(alpha = alpha * 0.35f),
                    radius = glows * 2.6f,
                    center = Offset(p.xPercent * w, p.yPercent * h)
                )
                // Core particle center
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.9f),
                    radius = glows * 0.85f,
                    center = Offset(p.xPercent * w, p.yPercent * h)
                )
            }
        }
        
        // Render content over the generated graphics
        content()
    }
}

private fun drawMoon(drawScope: DrawScope, backgroundClearColor: Color) {
    val paddingX = drawScope.size.width * 0.15f
    val paddingY = drawScope.size.height * 0.10f
    val moonCenter = Offset(drawScope.size.width - paddingX, paddingY)
    val moonRadius = 38f

    // Soft yellow moon eclipse glow
    drawScope.drawCircle(
        color = Color(0xFFFFF3B0).copy(alpha = 0.08f),
        radius = moonRadius * 1.8f,
        center = moonCenter
    )

    // Main moon surface circle
    drawScope.drawCircle(
        color = Color(0xFFFFFDF0).copy(alpha = 0.85f),
        radius = moonRadius,
        center = moonCenter
    )

    // Subtraction offset overlap masking
    drawScope.drawCircle(
        color = backgroundClearColor,
        radius = moonRadius * 0.95f,
        center = Offset(moonCenter.x - 14f, moonCenter.y - 4f)
    )
}

private fun drawCyberGrid(drawScope: DrawScope, theme: AppThemePreset, phase: Float) {
    val w = drawScope.size.width
    val h = drawScope.size.height
    val gridTop = h * 0.55f // Grid covers the bottom portion of screen

    // Perspective vanishing center
    val vanishingX = w / 2f
    val vanishingY = gridTop

    // Draw horizontal grid lines with relative spacing compressing towards vanishing point
    val lineCount = 12
    for (i in 0..lineCount) {
        val normalized = i.toFloat() / lineCount.toFloat()
        // Exponential scaling for perspective depth
        val relativeY = gridTop + (h - gridTop) * (normalized * normalized)
        
        drawScope.drawLine(
            color = theme.accentColor.copy(alpha = 0.12f * normalized),
            start = Offset(0f, relativeY),
            end = Offset(w, relativeY),
            strokeWidth = 1.5f
        )
    }

    // Draw vanishing perspective lines stretching outwards
    val diagonalLines = 10
    for (i in 0..diagonalLines) {
        val fraction = i.toFloat() / diagonalLines.toFloat()
        val bottomTargetX = w * fraction
        
        drawScope.drawLine(
            color = theme.primaryColor.copy(alpha = 0.10f),
            start = Offset(vanishingX, vanishingY),
            end = Offset(bottomTargetX, h),
            strokeWidth = 1.2f
        )
    }
}

private fun drawAuroraBands(drawScope: DrawScope, theme: AppThemePreset, phase: Float) {
    val w = drawScope.size.width
    val h = drawScope.size.height
    val numWaves = 3

    for (step in 0 until numWaves) {
        val path = Path()
        val verticalOffset = h * (0.15f + step * 0.12f)
        val amplitude = 40f + step * 15f
        val phaseShift = step * (Math.PI / 3f).toFloat()

        path.reset()
        // Draw wavy banner path across the sky
        for (x in 0..w.toInt() step 20) {
            val angle = (x.toFloat() / w) * (2f * Math.PI.toFloat()) + phase + phaseShift
            val y = verticalOffset + sin(angle) * amplitude
            if (x == 0) {
                path.moveTo(x.toFloat(), y)
            } else {
                path.lineTo(x.toFloat(), y)
            }
        }

        // Draw soft aurora stroke line
        drawScope.drawPath(
            path = path,
            color = if (step % 2 == 0) theme.primaryColor.copy(alpha = 0.14f) else theme.accentColor.copy(alpha = 0.08f),
            style = Stroke(width = 120f / (step + 1f))
        )
    }
}

private fun drawWarmNebulas(drawScope: DrawScope, theme: AppThemePreset) {
    val w = drawScope.size.width
    val h = drawScope.size.height

    // Radial amber aura top left
    drawScope.drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(theme.accentColor.copy(alpha = 0.12f), Color.Transparent),
            center = Offset(w * 0.2f, h * 0.25f),
            radius = w * 0.5f
        ),
        radius = w * 0.5f,
        center = Offset(w * 0.2f, h * 0.25f)
    )

    // Radial gold aura bottom right
    drawScope.drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(theme.primaryColor.copy(alpha = 0.08f), Color.Transparent),
            center = Offset(w * 0.8f, h * 0.7f),
            radius = w * 0.6f
        ),
        radius = w * 0.6f,
        center = Offset(w * 0.8f, h * 0.7f)
    )
}

private fun drawSolarEclipse(drawScope: DrawScope, theme: AppThemePreset, phase: Float) {
    val w = drawScope.size.width
    val h = drawScope.size.height
    // Positioning near middle-top for an authoritative celestial presence
    val center = Offset(w * 0.5f, h * 0.25f)
    val eclipseRadius = w * 0.22f

    // 1. Blazing Outer Corona Glow
    drawScope.drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(theme.primaryColor.copy(alpha = 0.45f), theme.accentColor.copy(alpha = 0.15f), Color.Transparent),
            center = center,
            radius = eclipseRadius * (1.8f + 0.15f * sin(phase))
        ),
        radius = eclipseRadius * (1.8f + 0.15f * sin(phase)),
        center = center
    )

    // 2. Solar Flare Ray Petals
    val numFlares = 16
    for (i in 0 until numFlares) {
        val angle = (i.toFloat() / numFlares.toFloat()) * 2f * Math.PI.toFloat() + (phase * 0.15f)
        val flareLength = eclipseRadius * (1.2f + 0.25f * sin(phase * 2f + i))
        val flareTarget = Offset(
            x = center.x + cos(angle) * flareLength,
            y = center.y + sin(angle) * flareLength
        )
        drawScope.drawLine(
            color = theme.accentColor.copy(alpha = 0.35f),
            start = center,
            end = flareTarget,
            strokeWidth = 6f
        )
    }

    // 3. True Obsidian Subtraction Moon Mask (creates the Eclipse ring effect!)
    drawScope.drawCircle(
        color = theme.bgEnd,
        radius = eclipseRadius * 0.96f,
        center = center
    )
}

private fun drawGlacialCrystals(drawScope: DrawScope, theme: AppThemePreset, phase: Float) {
    val w = drawScope.size.width
    val h = drawScope.size.height
    
    // Low opacity glowing geometric crystalline glaciers at the bottom
    val icePath1 = Path().apply {
        moveTo(0f, h)
        lineTo(w * 0.25f, h * 0.82f + 15f * sin(phase))
        lineTo(w * 0.5f, h)
        close()
    }
    val icePath2 = Path().apply {
        moveTo(w * 0.35f, h)
        lineTo(w * 0.70f, h * 0.76f + 20f * cos(phase + 1f))
        lineTo(w * 1.05f, h)
        close()
    }
    
    drawScope.drawPath(
        path = icePath1,
        color = theme.primaryColor.copy(alpha = 0.08f)
    )
    drawScope.drawPath(
        path = icePath1,
        color = theme.accentColor.copy(alpha = 0.14f),
        style = Stroke(width = 2f)
    )

    drawScope.drawPath(
        path = icePath2,
        color = theme.accentColor.copy(alpha = 0.06f)
    )
    drawScope.drawPath(
        path = icePath2,
        color = theme.primaryColor.copy(alpha = 0.18f),
        style = Stroke(width = 2f)
    )
}

private fun drawAmethystFalls(drawScope: DrawScope, theme: AppThemePreset, phase: Float) {
    val w = drawScope.size.width
    val h = drawScope.size.height
    val numStreams = 4

    for (i in 0 until numStreams) {
        val path = Path()
        val startX = w * (0.2f + 0.2f * i)
        val amplitude = 25f
        val streamWidth = 35f - (i * 4f)

        path.reset()
        path.moveTo(startX, 0f)
        
        for (y in 0..h.toInt() step 30) {
            val angle = (y.toFloat() / h) * (4f * Math.PI.toFloat()) + phase + (i * 1.2f)
            val posX = startX + sin(angle) * amplitude
            path.lineTo(posX, y.toFloat())
        }

        drawScope.drawPath(
            path = path,
            color = if (i % 2 == 0) theme.primaryColor.copy(alpha = 0.12f) else theme.accentColor.copy(alpha = 0.08f),
            style = Stroke(width = streamWidth)
        )
    }
}

private fun drawDesertMonsoon(drawScope: DrawScope, theme: AppThemePreset, phase: Float) {
    val w = drawScope.size.width
    val h = drawScope.size.height

    // 1. Copper Sandstone Dunes at bottom
    val dunePath = Path().apply {
        moveTo(0f, h)
        lineTo(0f, h * 0.88f)
        cubicTo(w * 0.35f, h * 0.84f, w * 0.65f, h * 0.94f, w, h * 0.90f)
        lineTo(w, h)
        close()
    }
    drawScope.drawPath(
        path = dunePath,
        color = theme.primaryColor.copy(alpha = 0.12f)
    )

    // 2. Electrostatic falling rain streaks
    val rainDropCount = 18
    for (i in 0 until rainDropCount) {
        val startX = (w * (i * 0.055f + 0.05f)) % w
        val fallOffset = (phase * 180f + i * 40f) % h
        
        drawScope.drawLine(
            color = theme.accentColor.copy(alpha = 0.22f),
            start = Offset(startX, fallOffset),
            end = Offset(startX, fallOffset + 48f),
            strokeWidth = 2f
        )
    }
}

private fun drawCosmicNebula(drawScope: DrawScope, theme: AppThemePreset, phase: Float) {
    val w = drawScope.size.width
    val h = drawScope.size.height

    // Swirling nebulas - multi colored overlapping dynamic gradients
    val center1 = Offset(w * (0.3f + 0.1f * sin(phase * 0.5f)), h * (0.4f + 0.1f * cos(phase * 0.5f)))
    val center2 = Offset(w * (0.7f + 0.1f * cos(phase * 0.4f)), h * (0.6f + 0.1f * sin(phase * 0.6f)))

    // Magenta glow spot
    drawScope.drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(theme.primaryColor.copy(alpha = 0.22f), Color.Transparent),
            center = center1,
            radius = w * 0.65f
        ),
        radius = w * 0.65f,
        center = center1
    )

    // Green cyber glow spot
    drawScope.drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(theme.accentColor.copy(alpha = 0.16f), Color.Transparent),
            center = center2,
            radius = w * 0.55f
        ),
        radius = w * 0.55f,
        center = center2
    )
}

