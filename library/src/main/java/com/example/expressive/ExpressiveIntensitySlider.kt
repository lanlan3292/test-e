package com.example.expressive

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

private val TrackInsideCornerSize = 2.dp
private val ThumbWidth = 4.dp
private val ThumbTrackGapSize = 6.dp
private const val EnergyStartFraction = 1f / 3f
private const val EnergyEndFraction = 2f / 3f
private const val ParticleCount = 22
private const val GoldenRatio = 0.6180339887498949f
private const val ParticleDurationMeanMillis = 1500f
private const val ParticleDurationSpread = 0.08f
private const val ParticleMinimumBrightness = 0.5f
private const val ParticleMinimumHeightSlotDistance = 3

private data class SliderParticle(
    val phase: Float,
    val yFraction: Float,
    val brightness: Float,
    val durationSpread: Float,
    val phaseJitter: Float
)

private data class AnimatedParticle(
    val particle: SliderParticle,
    val progress: Float
)

private val SliderParticles = buildSliderParticles()

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
    trackCornerSize: Dp = 12.dp
) {
    val state = rememberSliderState(value, steps, valueRange)
    state.value = value
    val trackColors =
        if (enabled) {
            SliderDefaults.colors(activeTrackColor = Color.Transparent)
        } else {
            SliderDefaults.colors()
        }

    Slider(
        state = state,
        onValueChange = onValueChange,
        modifier = modifier
            .semantics { this.contentDescription = contentDescription },
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = trackColors,
        track = { sliderState ->
            ExpressiveTrack(
                sliderState = sliderState,
                enabled = enabled,
                colors = trackColors,
                trackHeight = trackHeight,
                trackCornerSize = trackCornerSize
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
    trackCornerSize: Dp
) {
    val fraction = sliderState.coercedValueAsFraction
    val energy =
        ((fraction - EnergyStartFraction) / (1f - EnergyStartFraction)).coerceIn(0f, 1f)
    val particleCount = (ParticleCount * energy).roundToInt()
    val particleSpeed = (3f * fraction - 1f).coerceIn(0.35f, 2f)
    val particles = buildList {
        repeat(if (enabled) particleCount else 0) { index ->
            key(index) {
                val particle = SliderParticles[index]
                val durationMillis =
                    (ParticleDurationMeanMillis * 2f /
                        particleSpeed * particle.durationSpread).roundToInt()
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
                add(AnimatedParticle(particle, phase))
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
                                (animatedParticle.progress + particle.phase + particle.phaseJitter) %
                                    1f
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
                                        (0.6f + 0.4f * energy)
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

private fun buildSliderParticles(): List<SliderParticle> {
    val phases = List(ParticleCount) { index -> (index + 1) * GoldenRatio % 1f }
    val phaseOrder = phases.indices.sortedBy { phases[it] }
    var slots: List<Int>? = null

    for (salt in 71 until 4000) {
        val candidate = (0 until ParticleCount)
            .sortedWith(compareBy<Int> { particleHash(it, salt) }.thenBy { it })
        if (
            (1 until ParticleCount).all { index ->
                abs(candidate[index] - candidate[index - 1]) >=
                    ParticleMinimumHeightSlotDistance
            }
        ) {
            slots = candidate
            break
        }
    }

    val heightSlots = slots ?: (0 until ParticleCount).map { it * 5 % ParticleCount }
    val phaseRanks = IntArray(ParticleCount)
    phaseOrder.forEachIndexed { rank, index -> phaseRanks[index] = rank }

    return List(ParticleCount) { index ->
        val phase = phases[index]
        val rank = phaseRanks[index]
        SliderParticle(
            phase = phase,
            yFraction = 0.08f + heightSlots[rank] / (ParticleCount - 1f) * 0.84f,
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

private fun particleHash(index: Int, salt: Int): Float {
    val value = (index + 1L) * salt * 2654435761L % 4294967296L
    return ((value ushr 8) % 1000L) / 1000f
}
