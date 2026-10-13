package com.venuenexa.pocmux.feature.playerlab

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.venuenexa.pocmux.core.consent.NetworkCounters

// Previews con estado falso (también sirven como entrada de screenshot tests).
@Preview(name = "Denied, no token", showBackground = true)
@Composable
private fun DeniedPreview() {
    PlayerLabContent(
        state = PlayerLabUiState(
            consentGranted = false,
            mode = PlayerMode.MUX_GATED,
            counters = NetworkCounters(received = 12, dropped = 12),
        ),
        onConsentChange = {},
        onModeChange = {},
        onLoadToken = {},
        onResetCounters = {},
    )
}

@Preview(name = "Granted, forwarded", showBackground = true)
@Composable
private fun GrantedPreview() {
    PlayerLabContent(
        state = PlayerLabUiState(
            consentGranted = true,
            mode = PlayerMode.MUX_GATED,
            playbackId = "demo",
            token = "demo",
            counters = NetworkCounters(
                received = 8,
                forwarded = 8,
                byMethodReceived = mapOf("postWithCompletion" to 8),
                byMethodForwarded = mapOf("postWithCompletion" to 8),
                byHost = mapOf("litix.io" to 8),
            ),
        ),
        onConsentChange = {},
        onModeChange = {},
        onLoadToken = {},
        onResetCounters = {},
    )
}
