package com.venuenexa.pocmux.feature.account

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.venuenexa.pocmux.core.context.ContextState
import com.venuenexa.pocmux.core.context.ContextSwitcher
import com.venuenexa.pocmux.core.context.UserContext
import com.venuenexa.pocmux.core.designsystem.VenueNexaTheme
import com.venuenexa.pocmux.core.identity.IdentityState
import com.venuenexa.pocmux.core.identity.IdentityStatus

private val NoActions = AccountActions({}, {}, {}, {}, {})
private val GuestSwitcher = ContextSwitcher(ContextState(), {}, {})
private val OwnerSwitcher =
    ContextSwitcher(
        state = ContextState(listOf(UserContext.GUEST, UserContext.OWNER), UserContext.OWNER, hintPending = false),
        onOpen = {},
        onAcknowledgeHint = {},
    )

@Preview(name = "Cuenta, invitado", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun AccountGuestPreview() {
    VenueNexaTheme {
        AccountScreen(state = AccountUiState(), switcher = GuestSwitcher, actions = NoActions)
    }
}

@Preview(name = "Cuenta, con sesión y perfiles", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun AccountSignedInPreview() {
    VenueNexaTheme {
        AccountScreen(
            state =
                AccountUiState(
                    identity = IdentityState(status = IdentityStatus.SIGNED_IN, expiresAtMs = 1_800_000_000_000),
                    analyticsConsent = true,
                ),
            switcher = OwnerSwitcher,
            actions = NoActions,
        )
    }
}

@Preview(name = "Cuenta, fuente 1,5x", showBackground = true, widthDp = 360, heightDp = 780, fontScale = 1.5f)
@Composable
private fun AccountLargeFontPreview() {
    VenueNexaTheme {
        AccountScreen(state = AccountUiState(), switcher = GuestSwitcher, actions = NoActions)
    }
}

@Preview(name = "Bienvenida", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun WelcomePreview() {
    VenueNexaTheme {
        WelcomeScreen(covers = null, showSignIn = true, error = null, onExplore = {}, onSignIn = {})
    }
}

@Preview(name = "Diagnóstico", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun DiagnosticsPreview() {
    VenueNexaTheme {
        DiagnosticsScreen(
            state = DiagnosticsUiState(signedIn = false, ownerGranted = true),
            onBack = {},
            onOpenLab = {},
            onOwnerGranted = {},
            onReset = {},
        )
    }
}
