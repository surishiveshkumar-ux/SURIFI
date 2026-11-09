package com.drs.surifi.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object SurifiPalette {
    val ObsidianBg = Color(0xFF030306)
    val SpecularEdgeTop = Color(0x66FFFFFF)
    val SpecularEdgeBottom = Color(0x12FFFFFF)
    val IceWhite = Color(0xFFF5F7FA)
    val MutedSilver = Color(0xFF9AA3B2)
    val CyanGlow = Color(0xFF7CE8FF)
    val SacredGold = Color(0xFFFFDFA6)
    val EmeraldPulse = Color(0xFF64F5C8)
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    glowColor: Color = SurifiPalette.CyanGlow,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(shape)
            .drawWithCache {
                val topSheen = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.14f),
                        glowColor.copy(alpha = 0.05f),
                        Color(0xFF0A0C12).copy(alpha = 0.72f)
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
                val radialCaustic = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.25f, size.height * 0.15f),
                    radius = size.maxDimension * 0.75f
                )
                onDrawBehind {
                    drawRect(brush = topSheen)
                    drawRect(brush = radialCaustic)
                }
            }
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        SurifiPalette.SpecularEdgeTop,
                        glowColor.copy(alpha = 0.28f),
                        SurifiPalette.SpecularEdgeBottom
                    ),
                    start = Offset.Zero,
                    end = Offset(400f, 600f)
                ),
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                } else Modifier
            ),
        content = content
    )
}

@Composable
fun LiquidGlassSphere(
    modifier: Modifier = Modifier,
    accentGlow: Color,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .drawWithCache {
                val sphereGradient = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.16f),
                        accentGlow.copy(alpha = 0.08f),
                        Color(0xFF07080D).copy(alpha = 0.86f)
                    ),
                    center = Offset(size.width * 0.35f, size.height * 0.28f),
                    radius = size.width * 0.75f
                )
                val rimGlow = Brush.sweepGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        accentGlow.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.08f),
                        accentGlow.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.55f)
                    )
                )
                onDrawBehind {
                    drawCircle(brush = sphereGradient)
                    drawCircle(
                        brush = rimGlow,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6.dp.toPx())
                    )
                }
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        content = content
    )
}
