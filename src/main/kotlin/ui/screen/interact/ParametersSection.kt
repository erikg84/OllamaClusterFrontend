package ui.screen.interact

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ParametersSection(
    temperature: Double,
    topP: Double,
    maxTokens: Int,
    frequencyPenalty: Double,
    onParametersChanged: (Double?, Double?, Int?, Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    var tempTemp by remember { mutableStateOf(temperature) }
    var tempTopP by remember { mutableStateOf(topP) }
    var tempMaxTokens by remember { mutableStateOf(maxTokens) }
    var tempFreqPenalty by remember { mutableStateOf(frequencyPenalty) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Parameters",
            style = MaterialTheme.typography.titleMedium
        )

        // Temperature slider
        ParameterSlider(
            label = "Temperature",
            value = tempTemp,
            valueRange = 0.0f..1.0f,
            valueDisplay = "%.1f".format(tempTemp),
            onValueChange = { tempTemp = it.toDouble() },
            onValueChangeFinished = { onParametersChanged(tempTemp, null, null, null) }
        )

        // Top P slider
        ParameterSlider(
            label = "Top P",
            value = tempTopP,
            valueRange = 0.0f..1.0f,
            valueDisplay = "%.1f".format(tempTopP),
            onValueChange = { tempTopP = it.toDouble() },
            onValueChangeFinished = { onParametersChanged(null, tempTopP, null, null) }
        )

        // Max tokens slider
        ParameterSlider(
            label = "Max Tokens",
            value = tempMaxTokens.toDouble(),
            valueRange = 1f..2048f,
            valueDisplay = tempMaxTokens.toString(),
            onValueChange = { tempMaxTokens = it.roundToInt() },
            onValueChangeFinished = { onParametersChanged(null, null, tempMaxTokens, null) }
        )

        // Frequency penalty slider
        ParameterSlider(
            label = "Frequency Penalty",
            value = tempFreqPenalty,
            valueRange = 0.0f..2.0f,
            valueDisplay = "%.1f".format(tempFreqPenalty),
            onValueChange = { tempFreqPenalty = it.toDouble() },
            onValueChangeFinished = { onParametersChanged(null, null, null, tempFreqPenalty) }
        )
    }
}

@Composable
fun ParameterSlider(
    label: String,
    value: Double,
    valueRange: ClosedFloatingPointRange<Float>,
    valueDisplay: String,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = valueDisplay,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Slider(
            value = value.toFloat(),
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
