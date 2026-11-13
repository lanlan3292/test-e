package com.example.expressive

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.rememberSliderState
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

private val TrackInsideCornerSize = 2.dp
private val ThumbWidth = 4.dp
private val ThumbTrackGapSize = 6.dp
private const val EnergyStartFraction = 1f / 3f
private const val EnergyEndFraction = 2f / 3f
private const val DefaultParticleCount = 22
private const val GoldenRatio = 0.6180339887498949f
private const val ParticleDurationMeanMillis = 6000f
private const val ParticleDurationSpread = 0.08f
private const val ParticleMinimumBrightness = 0.5f

private data class SliderParticle(
    val phase: Float,
    val yFraction: Float,
    val brightness: Float,
    val durationSpread: Float,
    val phaseJitter: Float
)

private data class AnimatedParticle(
    val particle: SliderParticle,
    val progress: Float,
    val visibility: Float
)

/** Material 3 Slider with a blue-to-violet track and high-intensity particles. */
@Composable
fun ExpressiveIntensitySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    contentDescription: String = "Intensity",
    onValueChangeFinished: (() -> Unit)? = null,
    trackHeight: Dp = 40.dp,
    trackCornerSize: Dp = 12.dp,
    insetIcon: Painter? = null,
    insetEndIcon: Painter? = null,
    particleCount: Int = DefaultParticleCount
) {
    require(particleCount >= 0) { "particleCount must be non-negative" }
    val state = rememberSliderState(value, steps, valueRange)
    val interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() }
    state.value = value
    val trackColors =
        if (enabled) {
            SliderDefaults.colors(activeTrackColor = Color.Transparent)
        } else {
            SliderDefaults.colors()
        }
    val inactiveIconColor = trackColors.inactiveTickColor
    val activeIconColor = trackColors.activeTickColor

    Slider(
        state = state,
        onValueChange = onValueChange,
        interactionSource = interactionSource,
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .then(
                if (enabled && insetIcon != null) {
                    Modifier.drawWithContent {
                        drawContent()
                        val iconSizePx = 20.dp.toPx()
                        val paddingPx = 10.dp.toPx()
                        val gapPx = ThumbTrackGapSize.toPx()
                        val thumbWidthPx = ThumbWidth.toPx()
                        val thumbCenter = thumbWidthPx / 2f +
                            (size.width - thumbWidthPx) * state.coercedValueAsFraction
                        val thumbLeft = thumbCenter - thumbWidthPx / 2f
                        val thumbRight = thumbCenter + thumbWidthPx / 2f
                        val y = size.height / 2f - iconSizePx / 2f

                        if (insetEndIcon == null) {
                            val overlapsThumb =
                                paddingPx + iconSizePx > thumbLeft - gapPx &&
                                    paddingPx <= thumbRight + gapPx
                            val x = if (overlapsThumb) {
                                thumbRight + gapPx + paddingPx
                            } else {
                                paddingPx
                            }.coerceAtMost(size.width - iconSizePx - paddingPx)
                            val color =
                                if (overlapsThumb) inactiveIconColor else activeIconColor
                            translate(x, y) {
                                with(insetIcon) {
                                    draw(
                                        Size(iconSizePx, iconSizePx),
                                        colorFilter = ColorFilter.tint(color)
                                    )
                                }
                            }
                        } else {
                            val activeEnd = thumbLeft - gapPx
                            val inactiveStart = thumbRight + gapPx
                            if (iconSizePx < activeEnd - paddingPx * 2f) {
                                drawTrackIcon(insetIcon, paddingPx, y, iconSizePx, activeIconColor)
                                drawTrackIcon(
                                    insetEndIcon,
                                    activeEnd - paddingPx - iconSizePx,
                                    y,
                                    iconSizePx,
                                    activeIconColor
                                )
                            }
                            if (iconSizePx < size.width - inactiveStart - paddingPx * 2f) {
                                drawTrackIcon(
                                    insetIcon,
                                    inactiveStart + paddingPx,
                                    y,
                                    iconSizePx,
                                    inactiveIconColor
                                )
                                drawTrackIcon(
                                    insetEndIcon,
                                    size.width - paddingPx - iconSizePx,
                                    y,
                                    iconSizePx,
                                    inactiveIconColor
                                )
                            }
                        }
                    }
                } else {
                    Modifier
                }
            ),
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = trackColors,
        track = { sliderState ->
            ExpressiveTrack(
                sliderState = sliderState,
                enabled = enabled,
                colors = trackColors,
                trackHeight = trackHeight,
                trackCornerSize = trackCornerSize,
                maxParticleCount = particleCount
            )
        }
    )
}

@Composable
private fun ExpressiveTrack(
    sliderState: SliderState,
    enabled: Boolean,
    colors: androidx.compose.material3.SliderColors,
    trackHeight: Dp,
    trackCornerSize: Dp,
    maxParticleCount: Int
) {
    val iconSize = DpSize(20.dp, 20.dp)
    val iconPadding = 10.dp
    val thumbTrackGapSize = ThumbTrackGapSize
    val fraction = sliderState.coercedValueAsFraction
    val energy =
        ((fraction - EnergyStartFraction) / (1f - EnergyStartFraction)).coerceIn(0f, 1f)
    val particleLevel = maxParticleCount * energy
    val visibleParticleCount =
        if (particleLevel > 0f) ceil(particleLevel).toInt().coerceAtMost(maxParticleCount) else 0
    val particleSpeed = (3f * fraction - 1f).coerceIn(0.35f, 2f)
    val sliderParticles = remember(maxParticleCount) {
        buildSliderParticles(maxParticleCount)
    }
    val particles = buildList {
        repeat(if (enabled) visibleParticleCount else 0) { index ->
            key(index) {
                val particle = sliderParticles[index]
                val durationMillis =
                    (ParticleDurationMeanMillis / 2f * particle.durationSpread).roundToInt()
                val transition =
                    rememberInfiniteTransition(label = "expressiveSliderParticle$index")
                val phase by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = durationMillis,
                            easing = LinearEasing
                        )
                    ),
                    label = "expressiveSliderParticlePhase$index"
                )
                add(
                    AnimatedParticle(
                        particle = particle,
                        progress = phase,
                        visibility = (particleLevel - index).coerceIn(0f, 1f)
                    )
                )
            }
        }
    }
    val primary = MaterialTheme.colorScheme.primary

    Box(Modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(trackHeight)
        ) {
            val isRtl = layoutDirection == LayoutDirection.Rtl
            val cornerSize = trackCornerSize.toPx()
            val thumbTrackGap = ThumbWidth.toPx() / 2f + ThumbTrackGapSize.toPx()
            val sliderValueEnd =
                if (
                    sliderState.steps > 0 &&
                    fraction > 0f &&
                    fraction < 1f
                ) {
                    cornerSize + (size.width - 2f * cornerSize) * fraction
                } else {
                    size.width * fraction
                }
            val activeTrackWidth = sliderValueEnd - thumbTrackGap
            val activeTrackEnd = activeTrackWidth.coerceIn(0f, size.width)
            val activeRect = if (isRtl) {
                Rect(
                    left = size.width - activeTrackEnd,
                    top = 0f,
                    right = size.width,
                    bottom = size.height
                )
            } else {
                Rect(left = 0f, top = 0f, right = activeTrackEnd, bottom = size.height)
            }
            val activeTrackPath = Path().apply {
                val insideRadius = TrackInsideCornerSize.toPx()
                val outerRadius = cornerSize
                val startRadius = if (isRtl) insideRadius else outerRadius
                val endRadius = if (isRtl) outerRadius else insideRadius
                addRoundRect(
                    RoundRect(
                        rect = activeRect,
                        topLeft = CornerRadius(startRadius),
                        topRight = CornerRadius(endRadius),
                        bottomRight = CornerRadius(endRadius),
                        bottomLeft = CornerRadius(startRadius)
                    )
                )
            }
            val trackStart = if (isRtl) size.width else 0f
            val trackEnd = if (isRtl) 0f else size.width
            val endColor = sliderFillColor(primary, fraction)
            val activeTrackThreshold =
                if (sliderState.steps > 0) TrackInsideCornerSize.toPx() else 0f

            if (enabled && activeTrackWidth > activeTrackThreshold) {
                drawPath(
                    path = activeTrackPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(primary, endColor),
                        startX = trackStart,
                        endX = trackEnd
                    )
                )

                if (energy > 0f) {
                    drawPath(
                        path = activeTrackPath,
                        brush = Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0f to Color(0xFF3B1178),
                                0.3f to Color(0xFF6D28D9),
                                0.62f to Color(0xFF9333EA),
                                1f to Color(0xFFC084FC)
                            ),
                            startX = trackStart,
                            endX = trackEnd
                        ),
                        alpha = energy
                    )

                    val particleClipLeft = activeRect.left.coerceAtLeast(0f)
                    val particleClipRight = activeRect.right.coerceAtMost(size.width)
                    clipPath(activeTrackPath) {
                        particles.forEach { animatedParticle ->
                            val particle = animatedParticle.particle
                            val progress =
                                (
                                    animatedParticle.progress * particleSpeed +
                                        particle.phase +
                                        particle.phaseJitter
                                    ) % 1f
                            val x =
                                if (isRtl) {
                                    size.width * progress
                                } else {
                                    size.width * (1f - progress)
                                }
                            if (x in particleClipLeft..particleClipRight) {
                                val fadeIn = (progress / 0.06f).coerceIn(0f, 1f)
                                val fadeOut = ((1f - progress) / 0.06f).coerceIn(0f, 1f)
                                val alpha =
                                    min(fadeIn, fadeOut) *
                                        particle.brightness *
                                        (0.6f + 0.4f * energy) *
                                        animatedParticle.visibility
                                val center =
                                    Offset(x, particle.yFraction * size.height)
                                val radius = 1.5.dp.toPx() * particle.brightness

                                drawCircle(
                                    color = Color.White.copy(alpha = alpha * 0.3f),
                                    radius = radius * 2.4f,
                                    center = center
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = alpha),
                                    radius = radius,
                                    center = center
                                )
                            }
                        }
                    }

                }
            }
        }

        SliderDefaults.Track(
            sliderState = sliderState,
            trackCornerSize = trackCornerSize,
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight),
            enabled = enabled,
            colors = colors,
            drawStopIndicator = null,
            thumbTrackGapSize = ThumbTrackGapSize
        )

    }
}

private fun DrawScope.drawTrackIcon(
    painter: Painter,
    x: Float,
    y: Float,
    iconSize: Float,
    color: Color
) {
    translate(x, y) {
        with(painter) {
            draw(
                size = Size(iconSize, iconSize),
                colorFilter = ColorFilter.tint(color)
            )
        }
    }
}

private fun sliderFillColor(primary: Color, fraction: Float): Color {
    val interpolation = when {
        fraction <= EnergyStartFraction -> 0f
        fraction <= EnergyEndFraction ->
            (fraction - EnergyStartFraction) / (EnergyEndFraction - EnergyStartFraction) * 0.5f
        else ->
            0.5f + (fraction - EnergyEndFraction) /
                (1f - EnergyEndFraction) * 0.5f
    }
    val endpoint =
        if (interpolation <= 0.5f) {
            lerpColor(primary, Color(0xFF9333EA), interpolation * 2f)
        } else {
            lerpColor(Color(0xFF9333EA), Color(0xFF4C1D95), (interpolation - 0.5f) * 2f)
        }
    return endpoint
}

private fun lerpColor(start: Color, end: Color, fraction: Float): Color =
    Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = start.alpha + (end.alpha - start.alpha) * fraction
    )

private fun buildSliderParticles(count: Int): List<SliderParticle> {
    if (count == 0) return emptyList()

    val phases = List(count) { index -> (index + 1) * GoldenRatio % 1f }

    return List(count) { index ->
        SliderParticle(
            phase = phases[index],
            yFraction = 0.08f + vanDerCorput(index) * 0.84f,
            brightness =
                ParticleMinimumBrightness +
                    (1f - ParticleMinimumBrightness) * particleHash(index, 61),
            durationSpread =
                1f - ParticleDurationSpread +
                    2f * ParticleDurationSpread * particleHash(index, 29),
            phaseJitter = particleHash(index, 53) * 0.03f
        )
    }
}

private fun vanDerCorput(index: Int): Float {
    var bits = index + 1
    var reversed = 0f
    var scale = 0.5f

    while (bits > 0) {
        if (bits and 1 == 1) reversed += scale
        bits = bits ushr 1
        scale *= 0.5f
    }

    return reversed
}

private fun particleHash(index: Int, salt: Int): Float {
    val value = (index + 1L) * salt * 2654435761L % 4294967296L
    return ((value ushr 8) % 1000L) / 1000f
}
