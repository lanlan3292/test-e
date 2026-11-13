package com.lanlan3292.material3.extension.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.rememberSliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lanlan3292.material3.extension.ExpressiveIntensitySlider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var trackHeightText by remember { mutableStateOf("40") }
            var particleCountText by remember { mutableStateOf("22") }
            var segmentCountText by remember { mutableStateOf("0") }
            var insetIconStyle by remember { mutableIntStateOf(0) }
            var iconMenuExpanded by remember { mutableStateOf(false) }
            val trackHeight = trackHeightText.toIntOrNull()?.coerceIn(12, 100) ?: 40
            val particleCount = particleCountText.toIntOrNull()?.coerceIn(0, 100) ?: 22
            val segmentCount = segmentCountText.toIntOrNull()?.coerceAtLeast(0) ?: 0
            val sliderSteps = if (segmentCount > 0) (segmentCount - 1).coerceAtLeast(0) else 0
            MaterialTheme {
                key(sliderSteps) {
                    val sliderState = rememberSliderState(value = .50f, steps = sliderSteps)
                    val interactionSource = remember { MutableInteractionSource() }
                    val boltIcon = rememberVectorPainter(Icons.Filled.Bolt)
                    val musicIcon = rememberVectorPainter(Icons.Filled.MusicNote)
                    val musicOffIcon = rememberVectorPainter(Icons.Filled.MusicOff)
                    val showInsetIcon = insetIconStyle != 0
                    val officialInsetIcons = insetIconStyle == 2
                    val sliderValue = sliderState.value
                    Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Text("${(sliderValue)}", style = MaterialTheme.typography.displaySmall)
                        TextField(
                            value = trackHeightText,
                            onValueChange = { trackHeightText = it.filter(Char::isDigit) },
                            label = { Text("Track height (dp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                        TextField(
                            value = particleCountText,
                            onValueChange = { particleCountText = it.filter(Char::isDigit) },
                            label = { Text("Particle count (0-100)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                        TextField(
                            value = segmentCountText,
                            onValueChange = { segmentCountText = it.filter(Char::isDigit) },
                            label = { Text("Segments") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                        Box {
                            Button(onClick = { iconMenuExpanded = true }) {
                                Text(
                                    when (insetIconStyle) {
                                        1 -> "Inset icon: Lightning"
                                        2 -> "Inset icon: Official sample"
                                        else -> "Inset icon: Off"
                                    }
                                )
                            }
                            DropdownMenu(
                                expanded = iconMenuExpanded,
                                onDismissRequest = { iconMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Off") },
                                    onClick = {
                                        insetIconStyle = 0
                                        iconMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Lightning") },
                                    onClick = {
                                        insetIconStyle = 1
                                        iconMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Official sample") },
                                    onClick = {
                                        insetIconStyle = 2
                                        iconMenuExpanded = false
                                    }
                                )
                            }
                        }
                        Text("Expressive Intensity Slider", style = MaterialTheme.typography.titleMedium)
                        ExpressiveIntensitySlider(
                            sliderValue,
                            { sliderState.value = it },
                            steps = sliderSteps,
                            trackHeight = trackHeight.dp,
                            particleCount = particleCount,
                            insetIcon = if (showInsetIcon && !officialInsetIcons) boltIcon else if (officialInsetIcons) musicIcon else null,
                            insetEndIcon = if (officialInsetIcons) musicOffIcon else null,
                        )
                        Text("Standard Slider", style = MaterialTheme.typography.titleMedium)
                        Slider(
            state = sliderState,
            onValueChange = { sliderState.value = it },
            interactionSource = interactionSource,
            onValueChangeFinished = {
                // launch some business logic update with the state you hold
                // viewModel.updateSelectedSliderValue(sliderPosition)
            },
            track = { state ->
                val colors = SliderDefaults.colors()
                val iconSize = 20.dp
                val iconPadding = 10.dp
                val thumbTrackGapSize = 6.dp
                SliderDefaults.Track(
                    sliderState = state,
                    modifier = Modifier
                        .height(trackHeight.dp)
                        .then(
                            if (showInsetIcon) {
                                Modifier.drawWithContent {
                                    drawContent()
                                    val iconSizePx = iconSize.toPx()
                                    val paddingPx = iconPadding.toPx()
                                    val gapPx = thumbTrackGapSize.toPx()
                                    val yOffset = size.height / 2f - iconSizePx / 2f
                                    val thumbWidthPx = 4.dp.toPx()
                                    val thumbCenter =
                                        thumbWidthPx / 2f +
                                            (size.width - thumbWidthPx) * state.coercedValueAsFraction
                                    val thumbLeft = thumbCenter - thumbWidthPx / 2f
                                    val thumbRight = thumbCenter + thumbWidthPx / 2f
                                    fun drawIcon(painter: androidx.compose.ui.graphics.painter.Painter, x: Float, color: androidx.compose.ui.graphics.Color) {
                                        translate(x, yOffset) {
                                            with(painter) {
                                                draw(
                                                    Size(iconSizePx, iconSizePx),
                                                    colorFilter = ColorFilter.tint(color)
                                                )
                                            }
                                        }
                                    }

                                    if (officialInsetIcons) {
                                        val activeEnd = thumbLeft - gapPx
                                        val inactiveStart = thumbRight + gapPx
                                        if (iconSizePx < activeEnd - paddingPx * 2f) {
                                            drawIcon(musicIcon, paddingPx, colors.activeTickColor)
                                            drawIcon(musicOffIcon, activeEnd - paddingPx - iconSizePx, colors.activeTickColor)
                                        }
                                        if (iconSizePx < size.width - inactiveStart - paddingPx * 2f) {
                                            drawIcon(musicIcon, inactiveStart + paddingPx, colors.inactiveTickColor)
                                            drawIcon(musicOffIcon, size.width - paddingPx - iconSizePx, colors.inactiveTickColor)
                                        }
                                    } else {
                                        val overlapsThumb =
                                            paddingPx + iconSizePx > thumbLeft - gapPx &&
                                                paddingPx <= thumbRight + gapPx
                                        val iconColor =
                                            if (overlapsThumb) colors.inactiveTickColor
                                            else colors.activeTickColor
                                        val desiredX =
                                            if (overlapsThumb) thumbRight + gapPx + paddingPx
                                            else paddingPx
                                        val x = desiredX.coerceAtMost(
                                            (size.width - iconSizePx - paddingPx).coerceAtLeast(paddingPx)
                                        )
                                        drawIcon(boltIcon, x, iconColor)
                                        }
                                }
                            } else {
                                Modifier
                            }
                        ),
                    trackCornerSize = 12.dp,
                    drawStopIndicator = null,
                    thumbTrackGapSize = thumbTrackGapSize,
                )
            },
                        )
                    }
                }
            }
        }
    }
}
}
