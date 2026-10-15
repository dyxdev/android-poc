package com.venuenexa.pocmux.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venuenexa.pocmux.core.context.ContextSwitcher
import com.venuenexa.pocmux.core.context.rememberContextSwitcher
import com.venuenexa.pocmux.core.designsystem.spacing

@Composable
fun AccountRoute(
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val auth = rememberAuthActions(viewModel)
    AccountScreen(
        state = state,
        switcher = rememberContextSwitcher(),
        actions =
            AccountActions(
                onSignIn = auth.signIn,
                onSignInGoogle = auth.signInGoogle,
                onSignOut = auth.signOut,
                onConsentChange = viewModel::onConsentChange,
                onOpenDiagnostics = onOpenDiagnostics,
            ),
        modifier = modifier,
    )
}

// Pantalla sin estado de la cuenta: previsualizable (ver AccountPreviews).
@Composable
fun AccountScreen(
    state: AccountUiState,
    switcher: ContextSwitcher,
    actions: AccountActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(MaterialTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
    ) {
        Text(
            text = stringResource(R.string.account_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        SessionCard(identity = state.identity, actions = actions)
        if (switcher.state.canSwitch) ProfileCard(switcher = switcher)
        PrivacyCard(granted = state.analyticsConsent, onChange = actions.onConsentChange)
        DiagnosticsCard(onClick = actions.onOpenDiagnostics)
    }
}
