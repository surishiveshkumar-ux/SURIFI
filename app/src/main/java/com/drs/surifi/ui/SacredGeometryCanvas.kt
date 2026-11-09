package com.drs.surifi.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.*

private data class NormalizedCubic(
    val x0: Float, val y0: Float,
    val x1: Float, val y1: Float,
    val x2: Float, val y2: Float,
    val x3: Float, val y3: Float,
    val burstDx: Float, val burstDy: Float
)

private val InfinitySegments = listOf(
    NormalizedCubic(0.0f, 0.0f, -0.45f, -0.62f, -0.95f, -0.55f, -0.95f, 0.0f, -0.18f, -0.22f),
    NormalizedCubic(-0.95f, 0.0f, -0.95f, 0.55f, -0.45f, 0.62f, 0.0f, 0.0f, -0.20f, 0.24f),
    NormalizedCubic(0.0f, 0.0f, 0.45f, -0.62f, 0.95f, -0.55f, 0.95f, 0.0f, 0.24f, 0.14f),
    NormalizedCubic(0.95f, 0.0f, 0.95f, 0.42f, 0.58f, 0.56f, 0.22f, 0.18f, 0.16f, -0.28f),
    NormalizedCubic(0.22f, 0.18f, 0.12f, 0.08f, 0.05f, 0.03f, 0.0f, 0.0f, 0.05f, -0.35f)
)

private val SacredOmSegments = listOf(
    NormalizedCubic(-0.58f, -0.28f, -0.10f, -0.65f, 0.08f, -0.12f, -0.28f, -0.02f, 0f, 0f),
    NormalizedCubic(-0.28f, -0.02f, 0.22f, 0.08f, 0.10f, 0.78f, -0.72f, 0.34f, 0f, 0f),
    NormalizedCubic(-0.18f, 0.02f, 0.32f, -0.18f, 0.86f, 0.08f, 0.38f, 0.62f, 0f, 0f),
    NormalizedCubic(-0.12f, -0.58f, 0.12f, -0.38f, 0.42f, -0.42f, 0.56f, -0.66f, 0f, 0f),
    NormalizedCubic(0.20f, -0.78f, 0.25f, -0.83f, 0.30f, -0.78f, 0.22f, -0.75f, 0f, 0f)
)

@Composable
fun InfinityToOmCanvas(modifier: Modifier = Modifier, isBondingActive: Boolean = true) {
    val infiniteTransition = rememberInfiniteTransition(label = "InfinityOmMorph")
    val rawCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5600
                0.0f at 0 with CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                0.0f at 1100 with CubicBezierEasing(0.42f, 0.0f, 0.18f, 1.0f)
                1.0f at 2900 with CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                1.0f at 4200 with CubicBezierEasing(0.42f, 0.0f, 0.18f, 1.0f)
                0.0f at 5600
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "MorphProgress"
    )
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart),
        label = "ShimmerPhase"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val scale = size.minDimension * 0.38f
        val t = rawCycle
        val separationWave = sin(t * PI.toFloat())
        val morphColor = lerp(SurifiPalette.CyanGlow, SurifiPalette.SacredGold, t)
        val primaryBrush = Brush.linearGradient(
            colors = listOf(SurifiPalette.IceWhite, morphColor, SurifiPalette.IceWhite.copy(alpha = 0.85f)),
            start = Offset(cx - scale, cy - scale),
            end = Offset(cx + scale * cos(shimmerPhase), cy + scale * sin(shimmerPhase))
        )
        val path = Path()
        for (i in InfinitySegments.indices) {
            val inf = InfinitySegments[i]
            val om = SacredOmSegments[i]
            val burstX = inf.burstDx * separationWave * scale
            val burstY = inf.burstDy * separationWave * scale
            val x0 = cx + lerp(inf.x0, om.x0, t) * scale + burstX
            val y0 = cy + lerp(inf.y0, om.y0, t) * scale + burstY
            val x1 = cx + lerp(inf.x1, om.x1, t) * scale + burstX
            val y1 = cy + lerp(inf.y1, om.y1, t) * scale + burstY
            val x2 = cx + lerp(inf.x2, om.x2, t) * scale + burstX
            val y2 = cy + lerp(inf.y2, om.y2, t) * scale + burstY
            val x3 = cx + lerp(inf.x3, om.x3, t) * scale + burstX
            val y3 = cy + lerp(inf.y3, om.y3, t) * scale + burstY

            path.reset()
            path.moveTo(x0, y0)
            path.cubicTo(x1, y1, x2, y2, x3, y3)

            drawPath(
                path = path,
                color = morphColor.copy(alpha = if (isBondingActive) 0.26f else 0.14f),
                style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = path,
                brush = primaryBrush,
                style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            if (i == 4 && t > 0.35f) {
                val binduAlpha = ((t - 0.35f) / 0.65f).coerceIn(0f, 1f)
                drawCircle(color = SurifiPalette.SacredGold.copy(alpha = binduAlpha * 0.4f), radius = 7.dp.toPx() * binduAlpha, center = Offset(x1, y1))
                drawCircle(color = SurifiPalette.IceWhite.copy(alpha = binduAlpha), radius = 3.2.dp.toPx() * binduAlpha, center = Offset(x1, y1))
            }
        }
    }
}

@Composable
fun LocalSendRadarCanvas(modifier: Modifier = Modifier, peerCount: Int = 0) {
    val infiniteTransition = rememberInfiniteTransition(label = "LocalSendPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "RadarPulse"
    )
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension * 0.42f
        drawCircle(
            color = SurifiPalette.EmeraldPulse.copy(alpha = (1f - pulse) * 0.35f),
            radius = maxR * (0.25f + 0.75f * pulse),
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
        listOf(0.32f, 0.58f, 0.84f).forEachIndexed { idx, frac ->
            drawCircle(
                color = SurifiPalette.IceWhite.copy(alpha = 0.22f + idx * 0.12f),
                radius = maxR * frac,
                center = center,
                style = Stroke(width = 1.6.dp.toPx())
            )
        }
        drawCircle(color = SurifiPalette.EmeraldPulse, radius = 5.5.dp.toPx(), center = center)
        val activeDots = max(1, peerCount)
        for (i in 0 until activeDots) {
            val angle = (pulse * 2f * PI.toFloat()) + (i * (2f * PI.toFloat() / activeDots))
            val orbitRadius = maxR * 0.58f
            drawCircle(
                color = SurifiPalette.IceWhite,
                radius = 4.dp.toPx(),
                center = Offset(center.x + orbitRadius * cos(angle), center.y + orbitRadius * sin(angle))
            )
        }
    }
}

@Composable
fun MinimalistSLogoCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.68f, h * 0.28f)
            cubicTo(w * 0.68f, h * 0.12f, w * 0.32f, h * 0.12f, w * 0.32f, h * 0.34f)
            cubicTo(w * 0.32f, h * 0.52f, w * 0.68f, h * 0.48f, w * 0.68f, h * 0.66f)
            cubicTo(w * 0.68f, h * 0.88f, w * 0.32f, h * 0.88f, w * 0.32f, h * 0.72f)
        }
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = (size.minDimension * 0.095f), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
