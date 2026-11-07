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
            val sliderState = rememberSliderState(.72f)
            val interactionSource = remember { MutableInteractionSource() }
            val sliderValue = sliderState.value
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Text("${(sliderValue * 100).toInt()}%", style = MaterialTheme.typography.displaySmall)
                        Text("Expressive Intensity Slider", style = MaterialTheme.typography.titleMedium)
                        ExpressiveIntensitySlider(sliderValue, { sliderState.value = it })
                        Text("Expressive Intensity Slider (24 dp)", style = MaterialTheme.typography.titleMedium)
                        ExpressiveIntensitySlider(sliderValue, { sliderState.value = it }, trackHeight = 24.dp)
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
                    modifier = Modifier.height(40.dp),
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
