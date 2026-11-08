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
import androidx.compose.material3.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
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
                    val insetIcon = rememberVectorPainter(Icons.Filled.Bolt)
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
                            insetIcon = if (insetIconEnabled) insetIcon else null,
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
                            if (insetIconEnabled) {
                                Modifier.drawWithContent {
                                    drawContent()
                                    val iconSizePx = iconSize.toPx()
                                    val paddingPx = iconPadding.toPx()
                                    val gapPx = thumbTrackGapSize.toPx()
                                    val yOffset = size.height / 2f - iconSizePx / 2f
                                    val thumbCenter = size.width * state.coercedValueAsFraction
                                    val leftEnd = paddingPx + iconSizePx
                                    val overlapsThumb =
                                        leftEnd > thumbCenter - gapPx &&
                                            paddingPx < thumbCenter + gapPx
                                    val iconColor =
                                        if (overlapsThumb) colors.inactiveTickColor
                                        else colors.activeTickColor
                                    val desiredX = if (overlapsThumb) {
                                        thumbCenter + gapPx + paddingPx
                                    } else {
                                        paddingPx
                                    }
                                    val x = desiredX.coerceAtMost(
                                        (size.width - iconSizePx - paddingPx).coerceAtLeast(paddingPx)
                                    )
                                    translate(x, yOffset) {
                                        with(insetIcon) {
                                            draw(
                                                androidx.compose.ui.geometry.Size(
                                                    iconSizePx,
                                                    iconSizePx
                                                ),
                                                colorFilter = ColorFilter.tint(iconColor)
                                            )
                                        }
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
