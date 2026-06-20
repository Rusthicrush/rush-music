package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SparkBlue

@Composable
fun EqualizerDialog(
    currentBands: List<Int>, // 5 items
    onDismiss: () -> Unit,
    onBandsUpdated: (bands: List<Int>) -> Unit
) {
    var bandsState = remember(currentBands) { currentBands.toMutableStateList() }
    val labels = listOf("Sub-Bass (60Hz)", "Bass (230Hz)", "Midrange (910Hz)", "High-Mids (4kHz)", "Treble (14kHz)")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Equalizer, contentDescription = "Equalizer", tint = SparkBlue)
                Text("Equalizer")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Refining acoustic waves. Character preset: Balanced Depth.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                bandsState.forEachIndexed { index, value ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = labels[index],
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${if (value >= 0) "+" else ""}$value dB",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SparkBlue
                            )
                        }
                        Slider(
                            value = value.toFloat(),
                            onValueChange = { bandsState[index] = it.toInt() },
                            valueRange = -12f..12f,
                            steps = 24,
                            colors = SliderDefaults.colors(
                                activeTrackColor = SparkBlue,
                                thumbColor = SparkBlue
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("eq_band_$index")
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onBandsUpdated(bandsState.toList())
                    onDismiss()
                }
            ) {
                Text("Apply", color = SparkBlue)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    // Reset to zero (Flat preset)
                    onBandsUpdated(listOf(0, 0, 0, 0, 0))
                    onDismiss()
                }
            ) {
                Text("Flat Preset")
            }
        }
    )
}
