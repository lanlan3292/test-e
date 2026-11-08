package com.example.expressive.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
            val trackHeight = trackHeightText.toIntOrNull()?.coerceIn(12, 100) ?: 40
            val segmentCount = segmentCountText.toIntOrNull()?.coerceAtLeast(0) ?: 0
            val sliderSteps = if (segmentCount > 0) (segmentCount - 1).coerceAtLeast(0) else 0
            MaterialTheme {
                key(sliderSteps) {
                    val sliderState = rememberSliderState(value = .50f, steps = sliderSteps)
                    val interactionSource = remember { MutableInteractionSource() }
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
                        Text("Expressive Intensity Slider", style = MaterialTheme.typography.titleMedium)
                        ExpressiveIntensitySlider(
                            sliderValue,
                            { sliderState.value = it },
                            steps = sliderSteps,
                            trackHeight = trackHeight.dp,
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
            track = {
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(trackHeight.dp),
                    trackCornerSize = 12.dp,
                    drawStopIndicator = null,
                    thumbTrackGapSize = 6.dp,
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

