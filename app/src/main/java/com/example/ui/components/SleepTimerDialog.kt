package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SparkBlue

@Composable
fun SleepTimerDialog(
    currentMinutesRemaining: Int,
    onDismiss: () -> Unit,
    onStartTimer: (minutes: Int) -> Unit,
    onCancelTimer: () -> Unit
) {
    var sliderValue by remember { mutableStateOf(30f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Timer, contentDescription = "Timer", tint = SparkBlue)
                Text("Sleep Timer")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentMinutesRemaining > 0) {
                    val mm = currentMinutesRemaining / 60
                    val ss = currentMinutesRemaining % 60
                    Text(
                        text = "Active: %02d:%02d left".format(mm, ss),
                        style = MaterialTheme.typography.titleMedium,
                        color = SparkBlue,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Text("Stop playback automatically after:")
                Text(
                    text = "${sliderValue.toInt()} minutes",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 5f..120f,
                    steps = 22, // Increments of 5 mins
                    colors = SliderDefaults.colors(
                        activeTrackColor = SparkBlue,
                        thumbColor = SparkBlue
                    ),
                    modifier = Modifier.testTag("sleep_timer_slider")
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onStartTimer(sliderValue.toInt())
                    onDismiss()
                },
                modifier = Modifier.testTag("start_timer_confirm")
            ) {
                Text("Start Timer", color = SparkBlue)
            }
        },
        dismissButton = {
            Row {
                if (currentMinutesRemaining > 0) {
                    TextButton(
                        onClick = {
                            onCancelTimer()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("cancel_timer_btn")
                    ) {
                        Text("Cancel Active")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
