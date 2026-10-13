package com.venuenexa.pocmux.feature.playerlab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.venuenexa.pocmux.feature.playerlab.R as LabR

// Solo hosts y contadores: sin cuerpos, sin tokens, sin datos personales.
@Composable
fun CountersPanel(state: PlayerLabUiState, onReset: () -> Unit, modifier: Modifier = Modifier) {
    val counters = state.counters
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = stringResource(LabR.string.lab_counters), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(LabR.string.lab_received, counters.received))
        Text(stringResource(LabR.string.lab_forwarded, counters.forwarded))
        Text(stringResource(LabR.string.lab_dropped, counters.dropped))
        counters.byMethodReceived.forEach { (method, count) ->
            val fwd = counters.byMethodForwarded[method] ?: 0
            Text(stringResource(LabR.string.lab_method_row, method, count, fwd))
        }
        counters.byHost.forEach { (host, count) ->
            Text(stringResource(LabR.string.lab_host_row, host, count))
        }
        OutlinedButton(onClick = onReset) {
            Text(stringResource(LabR.string.lab_reset))
        }
    }
}
