package com.example.ui.components

import android.graphics.PointF
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SalimProgressRing(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    colors: List<Color>,
    strokeWidth: Dp = 14.dp,
    faceStyle: ClockFaceStyle = ClockFaceStyle.RingLike,
    shape: ClockShape = ClockShape.Circle,
    fillMode: FillMode = FillMode.Gradient,
    accessibilityText: String = "",
    innerContent: (@Composable () -> Unit)? = null
) {
    // Continuous subtle shimmer / particle pulse for organic Huawei-style fluid motion
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_phase"
    )

    val activeColors = faceStyle.getPaletteForArea(colors)
    val clampedProgress = progress.coerceIn(0.001f, 1.0f)

    Box(
        modifier = modifier
            .semantics {
                if (accessibilityText.isNotEmpty()) {
                    contentDescription = accessibilityText
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val diameter = minOf(size.width, size.height) - strokePx * 2
            val radius = diameter / 2f
            val centerOffset = Offset(size.width / 2f, size.height / 2f)

            when (shape) {
                ClockShape.Circle -> {
                    drawCircularFace(
                        clampedProgress = clampedProgress,
                        center = centerOffset,
                        radius = radius,
                        strokePx = strokePx,
                        faceStyle = faceStyle,
                        colors = activeColors,
                        fillMode = fillMode,
                        pulsePhase = pulsePhase
                    )
                }
                ClockShape.RoundedSquare -> {
                    drawSquircleFace(
                        clampedProgress = clampedProgress,
                        center = centerOffset,
                        halfSize = radius,
                        strokePx = strokePx,
                        isNotched = false,
                        faceStyle = faceStyle,
                        colors = activeColors,
                        fillMode = fillMode
                    )
                }
                ClockShape.NotchedSquircle -> {
                    drawSquircleFace(
                        clampedProgress = clampedProgress,
                        center = centerOffset,
                        halfSize = radius,
                        strokePx = strokePx,
                        isNotched = true,
                        faceStyle = faceStyle,
                        colors = activeColors,
                        fillMode = fillMode
                    )
                }
            }
        }

        if (innerContent != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(strokeWidth * 1.5f),
                contentAlignment = Alignment.Center
            ) {
                innerContent()
            }
        }
    }
}

private fun DrawScope.drawCircularFace(
    clampedProgress: Float,
    center: Offset,
    radius: Float,
    strokePx: Float,
    faceStyle: ClockFaceStyle,
    colors: List<Color>,
    fillMode: FillMode,
    pulsePhase: Float
) {
    val topLeft = Offset(center.x - radius, center.y - radius)
    val arcSize = Size(radius * 2, radius * 2)
    val trackColor = colors.first().copy(alpha = 0.15f)

    // Base background ring track
    drawArc(
        color = trackColor,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokePx, cap = StrokeCap.Round)
    )

    val activeBrush = if (fillMode == FillMode.Solid || colors.size == 1) {
        Brush.linearGradient(listOf(colors.first(), colors.first()))
    } else {
        Brush.sweepGradient(
            colors = colors + colors.first(),
            center = center
        )
    }

    when (faceStyle) {
        ClockFaceStyle.RingLike, ClockFaceStyle.SpringWarmth -> {
            // Unmodified fluid gradient ring
            drawArc(
                brush = activeBrush,
                startAngle = -90f,
                sweepAngle = clampedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Luminous glowing tip
            val tipAngleRad = ((-90f + clampedProgress * 360f) * (PI / 180f)).toFloat()
            val tipX = center.x + radius * cos(tipAngleRad)
            val tipY = center.y + radius * sin(tipAngleRad)
            drawCircle(
                color = colors.last(),
                radius = strokePx * 0.75f,
                center = Offset(tipX, tipY)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = strokePx * 0.35f,
                center = Offset(tipX, tipY)
            )
        }

        ClockFaceStyle.Fragmented -> {
            // Offset arc segments
            val segmentCount = 12
            val gapAngle = 5f
            val segmentSweep = (360f - segmentCount * gapAngle) / segmentCount

            for (i in 0 until segmentCount) {
                val segStart = -90f + i * (segmentSweep + gapAngle)
                val segFraction = (i + 1).toFloat() / segmentCount
                val isFilled = clampedProgress >= segFraction - (1f / segmentCount * 0.5f)

                if (isFilled) {
                    val colorIndex = (i % colors.size)
                    val segColor = colors[colorIndex]
                    drawArc(
                        color = segColor,
                        startAngle = segStart,
                        sweepAngle = segmentSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                } else {
                    drawArc(
                        color = trackColor.copy(alpha = 0.08f),
                        startAngle = segStart,
                        sweepAngle = segmentSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx * 0.6f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        ClockFaceStyle.NewTimes -> {
            // Geometric numerals dial indices + thin accent ring
            val dialRadius = radius - strokePx * 1.3f
            for (i in 0 until 12) {
                val angleRad = (i * 30f - 90f) * (PI / 180f).toFloat()
                val isMajor = (i % 3 == 0)
                val innerR = if (isMajor) dialRadius - 12f else dialRadius - 6f
                val start = Offset(center.x + innerR * cos(angleRad), center.y + innerR * sin(angleRad))
                val end = Offset(center.x + dialRadius * cos(angleRad), center.y + dialRadius * sin(angleRad))
                drawCircle(
                    color = if (isMajor) colors.first() else Color.White.copy(alpha = 0.4f),
                    radius = if (isMajor) 3.5f else 2f,
                    center = end
                )
            }

            // Crisp sleek accent arc
            drawArc(
                brush = activeBrush,
                startAngle = -90f,
                sweepAngle = clampedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx * 0.7f, cap = StrokeCap.Round)
            )
        }

        ClockFaceStyle.Fleeting -> {
            // Continuous base ring
            drawArc(
                brush = activeBrush,
                startAngle = -90f,
                sweepAngle = clampedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Dynamic particle trail following the leading head
            val tipAngleDeg = -90f + clampedProgress * 360f
            val particleCount = 6
            for (p in 0 until particleCount) {
                val lagDeg = tipAngleDeg - (p + 1) * 7f
                val lagRad = lagDeg * (PI / 180f).toFloat()
                val pX = center.x + radius * cos(lagRad)
                val pY = center.y + radius * sin(lagRad)
                val alpha = (1f - (p.toFloat() / particleCount)) * 0.7f
                val pSize = strokePx * (0.6f - p * 0.08f)

                drawCircle(
                    color = colors.last().copy(alpha = alpha),
                    radius = pSize,
                    center = Offset(pX, pY)
                )
            }
        }

        ClockFaceStyle.Continuity -> {
            // Designed for 24-hr day/night continuity
            drawArc(
                brush = activeBrush,
                startAngle = -90f,
                sweepAngle = clampedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Solar Noon (top) & Midnight (bottom) markers
            val noonOffset = Offset(center.x, center.y - radius)
            val midnightOffset = Offset(center.x, center.y + radius)
            drawCircle(color = Color(0xFFFFD166), radius = strokePx * 0.4f, center = noonOffset)
            drawCircle(color = Color(0xFF1A237E), radius = strokePx * 0.4f, center = midnightOffset)
        }
    }
}

private fun DrawScope.drawSquircleFace(
    clampedProgress: Float,
    center: Offset,
    halfSize: Float,
    strokePx: Float,
    isNotched: Boolean,
    faceStyle: ClockFaceStyle,
    colors: List<Color>,
    fillMode: FillMode
) {
    val cornerRadius = halfSize * 0.45f
    val trackColor = colors.first().copy(alpha = 0.15f)

    val squirclePath = Path().apply {
        val left = center.x - halfSize
        val top = center.y - halfSize
        val right = center.x + halfSize
        val bottom = center.y + halfSize

        moveTo(left + cornerRadius, top)
        if (isNotched) {
            lineTo(center.x - 12f, top)
            lineTo(center.x, top + 8f)
            lineTo(center.x + 12f, top)
        }
        lineTo(right - cornerRadius, top)
        quadraticTo(right, top, right, top + cornerRadius)
        lineTo(right, bottom - cornerRadius)
        quadraticTo(right, bottom, right - cornerRadius, bottom)
        lineTo(left + cornerRadius, bottom)
        quadraticTo(left, bottom, left, bottom - cornerRadius)
        lineTo(left, top + cornerRadius)
        quadraticTo(left, top, left + cornerRadius, top)
        close()
    }

    // Draw track
    drawPath(
        path = squirclePath,
        color = trackColor,
        style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // For squircle progress, simulate arc representation with path or colored highlight
    val brush = if (fillMode == FillMode.Solid || colors.size == 1) {
        Brush.linearGradient(listOf(colors.first(), colors.first()))
    } else {
        Brush.linearGradient(colors)
    }

    val arcTopLeft = Offset(center.x - halfSize, center.y - halfSize)
    val arcSize = Size(halfSize * 2, halfSize * 2)
    drawArc(
        brush = brush,
        startAngle = -90f,
        sweepAngle = clampedProgress * 360f,
        useCenter = false,
        topLeft = arcTopLeft,
        size = arcSize,
        style = Stroke(width = strokePx, cap = StrokeCap.Round)
    )
}
