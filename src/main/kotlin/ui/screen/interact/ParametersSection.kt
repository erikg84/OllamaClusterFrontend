package ui.screen.interact

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Parameters heading (optional)
        Text(
            text = "Parameters",
            style = MaterialTheme.typography.titleMedium
        )

        // Temperature
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Temperature",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "%.1f".format(tempTemp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Slider(
                value = tempTemp.toFloat(),
                onValueChange = { tempTemp = it.toDouble() },
                onValueChangeFinished = { onParametersChanged(tempTemp, null, null, null) },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Top P
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top P",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "%.1f".format(tempTopP),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Slider(
                value = tempTopP.toFloat(),
                onValueChange = { tempTopP = it.toDouble() },
                onValueChangeFinished = { onParametersChanged(null, tempTopP, null, null) },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Length & Diversity Header
        Text(
            text = "Length & Diversity",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        // Max Tokens
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Max Tokens",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = tempMaxTokens.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Slider(
                value = tempMaxTokens.toFloat(),
                onValueChange = { tempMaxTokens = it.roundToInt() },
                onValueChangeFinished = { onParametersChanged(null, null, tempMaxTokens, null) },
                valueRange = 1f..2048f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Frequency Penalty
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Frequency Penalty",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "%.1f".format(tempFreqPenalty),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Slider(
                value = tempFreqPenalty.toFloat(),
                onValueChange = { tempFreqPenalty = it.toDouble() },
                onValueChangeFinished = { onParametersChanged(null, null, null, tempFreqPenalty) },
                valueRange = 0f..2f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Parameter Information
        Text(
            text = "Parameter Information",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        // Temperature info
        ParameterInfoItem(
            name = "Temperature",
            description = "Controls randomness. Higher values (0.7-1.0) make output more random, while lower values (0.1-0.3) make it more focused and deterministic."
        )

        // Top P info
        ParameterInfoItem(
            name = "Top P",
            description = "Controls diversity via nucleus sampling. 0.9 means the model considers tokens comprising the top 90% probability mass."
        )

        // Max Tokens info
        ParameterInfoItem(
            name = "Max Tokens",
            description = "The maximum number of tokens to generate in the response."
        )

        // Frequency Penalty info
        ParameterInfoItem(
            name = "Frequency Penalty",
            description = "Reduces repetition by penalizing tokens that have already appeared in the text. Higher values mean less repetition."
        )
    }
}

@Composable
fun ParameterInfoItem(
    name: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}