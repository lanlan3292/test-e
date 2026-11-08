package com.example.expressive.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.DpSize
import androidx.compose.material3.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
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
import com.example.expressive.ExpressiveIntensitySlider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var trackHeightText by remember { mutableStateOf("40") }
            var segmentCountText by remember { mutableStateOf("0") }
            var insetIconEnabled by remember { mutableStateOf(false) }
            val trackHeight = trackHeightText.toIntOrNull()?.coerceIn(12, 100) ?: 40
            val segmentCount = segmentCountText.toIntOrNull()?.coerceAtLeast(0) ?: 0
            val sliderSteps = if (segmentCount > 0) (segmentCount - 1).coerceAtLeast(0) else 0
            MaterialTheme {
                key(sliderSteps) {
                    val sliderState = rememberSliderState(value = .50f, steps = sliderSteps)
                    val interactionSource = remember { MutableInteractionSource() }
                    val startIcon = rememberVectorPainter(Icons.Filled.VolumeUp)
                    val endIcon = rememberVectorPainter(Icons.Filled.VolumeOff)
                    val sliderValue = sliderState.value
                    Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Text("${(sliderValue * 100).toInt()}%", style = MaterialTheme.typography.displaySmall)
                        TextField(
                            value = trackHeightText,
                            onValueChange = { trackHeightText = it.filter(Char::isDigit) },
                            label = { Text("Track height (dp)") },
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
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text("Inset icon")
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = insetIconEnabled,
                                onCheckedChange = { insetIconEnabled = it },
                            )
                        }
                        Text("Expressive Intensity Slider", style = MaterialTheme.typography.titleMedium)
                        ExpressiveIntensitySlider(
                            sliderValue,
                            { sliderState.value = it },
                            steps = sliderSteps,
                            trackHeight = trackHeight.dp,
                            insetIcons = if (insetIconEnabled) startIcon to endIcon else null,
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
                val iconSize = DpSize(20.dp, 20.dp)
                val iconPadding = 10.dp
                val thumbTrackGapSize = 6.dp
                SliderDefaults.Track(
                    sliderState = state,
                    modifier = Modifier
                        .height(trackHeight.dp)
                        .then(
                            if (insetIconEnabled) {
                                Modifier.drawWithContent {
                                    drawContent()
                                    val iconWidthPx = iconSize.width.toPx()
                                    val iconHeightPx = iconSize.height.toPx()
                                    val paddingPx = iconPadding.toPx()
                                    val gapPx = thumbTrackGapSize.toPx()
                                    val yOffset = size.height / 2f - iconHeightPx / 2f
                                    val activeTrackEnd =
                                        size.width * state.coercedValueAsFraction - gapPx
                                    val inactiveTrackStart = activeTrackEnd + gapPx * 2f
                                    val inactiveTrackEnd = size.width

                                    fun drawIcon(painter: androidx.compose.ui.graphics.painter.Painter, x: Float, color: androidx.compose.ui.graphics.Color) {
                                        translate(x, yOffset) {
                                            with(painter) {
                                                draw(
                                                    androidx.compose.ui.geometry.Size(iconWidthPx, iconHeightPx),
                                                    colorFilter = ColorFilter.tint(color)
                                                )
                                            }
                                        }
                                    }

                                    if (iconWidthPx < activeTrackEnd - paddingPx * 2f) {
                                        drawIcon(startIcon, paddingPx, colors.activeTickColor)
                                        drawIcon(
                                            endIcon,
                                            activeTrackEnd - paddingPx - iconWidthPx,
                                            colors.activeTickColor
                                        )
                                    }
                                    val inactiveWidth = inactiveTrackEnd - inactiveTrackStart
                                    if (iconWidthPx < inactiveWidth - paddingPx * 2f) {
                                        drawIcon(
                                            startIcon,
                                            inactiveTrackStart + paddingPx,
                                            colors.inactiveTickColor
                                        )
                                        drawIcon(
                                            endIcon,
                                            inactiveTrackEnd - paddingPx - iconWidthPx,
                                            colors.inactiveTickColor
                                        )
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
