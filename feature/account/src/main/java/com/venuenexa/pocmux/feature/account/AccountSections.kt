package com.venuenexa.pocmux.feature.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.venuenexa.pocmux.core.context.ContextSwitcher
import com.venuenexa.pocmux.core.context.titleRes
import com.venuenexa.pocmux.core.designsystem.PrimaryButton
import com.venuenexa.pocmux.core.designsystem.SecondaryButton
import com.venuenexa.pocmux.core.designsystem.spacing
import com.venuenexa.pocmux.core.identity.IdentityState
import com.venuenexa.pocmux.core.identity.IdentityStatus

private val AvatarSize = 48.dp

// Tarjeta de la pantalla de Cuenta; con onClick es pulsable entera.
@Composable
private fun AccountCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    val width = modifier.fillMaxWidth()
    if (onClick == null) {
        Card(modifier = width, shape = shape, colors = colors, border = border) { content() }
    } else {
        Card(onClick = onClick, modifier = width, shape = shape, colors = colors, border = border) { content() }
    }
}

@Composable
internal fun TitleAndText(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun CardLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

// Sesión: invitado o con sesión (sin tokens ni nombre: solo cuándo caduca el de acceso).
@Composable
internal fun SessionCard(
    identity: IdentityState,
    actions: AccountActions,
) {
    val signedIn = identity.status == IdentityStatus.SIGNED_IN
    AccountCard {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar()
                TitleAndText(
                    title =
                        stringResource(
                            if (signedIn) R.string.account_signed_in_title else R.string.account_guest_title,
                        ),
                    text = sessionSubtitle(identity),
                )
            }
            identity.error?.let { error ->
                Text(
                    text = stringResource(errorLabel(error)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            SessionButtons(signedIn = signedIn, actions = actions)
        }
    }
}

@Composable
private fun SessionButtons(
    signedIn: Boolean,
    actions: AccountActions,
) {
    val full = Modifier.fillMaxWidth()
    if (signedIn) {
        SecondaryButton(text = stringResource(R.string.account_sign_out), onClick = actions.onSignOut, modifier = full)
    } else {
        PrimaryButton(text = stringResource(R.string.account_sign_in), onClick = actions.onSignIn, modifier = full)
        SecondaryButton(
            text = stringResource(R.string.account_sign_in_google),
            onClick = actions.onSignInGoogle,
            modifier = full,
        )
    }
}

// Avatar neutro (sin color de marca): la identidad no trae nombre ni foto todavía.
@Composable
private fun Avatar() {
    Box(
        modifier =
            Modifier
                .size(AvatarSize)
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Perfil activo y entrada a la hoja de contextos (solo con más de un contexto).
@Composable
internal fun ProfileCard(switcher: ContextSwitcher) {
    AccountCard {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            CardLabel(text = stringResource(R.string.account_profile_title))
            Text(
                text = stringResource(switcher.state.active.titleRes()),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            SecondaryButton(
                text = stringResource(R.string.account_profile_switch),
                onClick = switcher.onOpen,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun PrivacyCard(
    granted: Boolean,
    onChange: (Boolean) -> Unit,
) {
    AccountCard {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            CardLabel(text = stringResource(R.string.account_privacy_title))
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TitleAndText(
                    title = stringResource(R.string.account_analytics_title),
                    text = stringResource(R.string.account_analytics_description),
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = granted, onCheckedChange = onChange)
            }
        }
    }
}

@Composable
internal fun DiagnosticsCard(onClick: () -> Unit) {
    AccountCard(onClick = onClick) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TitleAndText(
                title = stringResource(R.string.account_diagnostics_title),
                text = stringResource(R.string.account_diagnostics_subtitle),
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
