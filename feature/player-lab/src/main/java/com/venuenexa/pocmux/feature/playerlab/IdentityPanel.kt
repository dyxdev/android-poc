package com.venuenexa.pocmux.feature.playerlab

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.venuenexa.pocmux.core.identity.IdentityError
import com.venuenexa.pocmux.core.identity.IdentityState
import com.venuenexa.pocmux.core.identity.IdentityStatus
import java.text.DateFormat
import java.util.Date
import com.venuenexa.pocmux.feature.playerlab.R as LabR

// Tarjeta «Cuenta» (ADR-AND-0008): login con Keycloak en Custom Tabs, Google por el broker, renovar y
// cerrar sesión. Sin tokens en pantalla: solo el estado y cuándo caduca el de acceso.
@Composable
internal fun IdentityPanel(
    state: IdentityState,
    onSignIn: () -> Unit,
    onSignInGoogle: () -> Unit,
    onRefreshToken: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(LabR.string.identity_title), style = MaterialTheme.typography.titleMedium)
        Text(text = statusText(state), style = MaterialTheme.typography.bodyMedium)
        state.error?.let { error ->
            Text(
                text = stringResource(errorLabel(error)),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.status == IdentityStatus.SIGNED_IN) {
                Button(onClick = onRefreshToken) { Text(stringResource(LabR.string.identity_refresh)) }
                OutlinedButton(onClick = onSignOut) { Text(stringResource(LabR.string.identity_sign_out)) }
            } else {
                Button(onClick = onSignIn) { Text(stringResource(LabR.string.identity_sign_in)) }
                OutlinedButton(onClick = onSignInGoogle) { Text(stringResource(LabR.string.identity_sign_in_google)) }
            }
        }
    }
}

@Composable
private fun statusText(state: IdentityState): String =
    when (state.status) {
        IdentityStatus.UNKNOWN -> stringResource(LabR.string.identity_unknown)
        IdentityStatus.SIGNED_OUT -> stringResource(LabR.string.identity_signed_out)
        IdentityStatus.SIGNED_IN -> {
            val expiresAtMs = state.expiresAtMs
            if (expiresAtMs == null) {
                stringResource(LabR.string.identity_signed_in)
            } else {
                stringResource(
                    LabR.string.identity_signed_in_until,
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(expiresAtMs)),
                )
            }
        }
    }

@StringRes
private fun errorLabel(error: IdentityError): Int =
    when (error) {
        IdentityError.NOT_CONFIGURED -> LabR.string.identity_error_not_configured
        IdentityError.CANCELED -> LabR.string.identity_error_canceled
        IdentityError.SESSION_EXPIRED -> LabR.string.identity_error_session_expired
        IdentityError.NETWORK -> LabR.string.identity_error_network
        IdentityError.FAILED -> LabR.string.identity_error_failed
    }
