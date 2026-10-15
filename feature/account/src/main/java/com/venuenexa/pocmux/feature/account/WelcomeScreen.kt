package com.venuenexa.pocmux.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venuenexa.pocmux.core.designsystem.PrimaryButton
import com.venuenexa.pocmux.core.designsystem.SecondaryButton
import com.venuenexa.pocmux.core.designsystem.ShimmerBox
import com.venuenexa.pocmux.core.designsystem.StaggeredEntrance
import com.venuenexa.pocmux.core.designsystem.VenueNexaWordmark
import com.venuenexa.pocmux.core.designsystem.VenuePhoto
import com.venuenexa.pocmux.core.designsystem.spacing
import com.venuenexa.pocmux.core.identity.IdentityStatus

private const val MAIN_PHOTO_ASPECT = 1.6f
private const val SIDE_PHOTO_ASPECT = 1.4f

// Bienvenida de la primera vez: «Explorar» entra como invitado; «Iniciar sesión» abre Keycloak y, si el login
// termina bien, también continúa. Se muestra una sola vez (lo decide :app con las preferencias).
@Composable
fun WelcomeRoute(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    accountViewModel: AccountViewModel = hiltViewModel(),
    welcomeViewModel: WelcomeViewModel = hiltViewModel(),
) {
    val account by accountViewModel.uiState.collectAsStateWithLifecycle()
    val covers by welcomeViewModel.covers.collectAsStateWithLifecycle()
    val auth = rememberAuthActions(accountViewModel)
    val status = account.identity.status
    val initialStatus = remember { status }
    // Continúa solo si la sesión se inicia desde esta pantalla (no si ya venía abierta).
    LaunchedEffect(status) {
        if (status == IdentityStatus.SIGNED_IN && initialStatus != IdentityStatus.SIGNED_IN) onFinished()
    }
    WelcomeScreen(
        covers = covers,
        showSignIn = status != IdentityStatus.SIGNED_IN,
        onExplore = onFinished,
        onSignIn = auth.signIn,
        modifier = modifier,
    )
}

// Pantalla sin estado de la bienvenida: previsualizable (ver AccountPreviews).
@Composable
fun WelcomeScreen(
    covers: List<String>?,
    showSignIn: Boolean,
    onExplore: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState())
                    .padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xl),
        ) {
            VenueNexaWordmark()
            StaggeredEntrance(index = 0, play = true) { Collage(covers = covers) }
            StaggeredEntrance(index = 1, play = true) { Headline() }
            StaggeredEntrance(index = 2, play = true) {
                Buttons(showSignIn = showSignIn, onExplore = onExplore, onSignIn = onSignIn)
            }
        }
    }
}

@Composable
private fun Headline() {
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        Text(
            text = stringResource(R.string.welcome_headline),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Buttons(
    showSignIn: Boolean,
    onExplore: () -> Unit,
    onSignIn: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        PrimaryButton(
            text = stringResource(R.string.welcome_explore),
            onClick = onExplore,
            modifier = Modifier.fillMaxWidth(),
        )
        if (showSignIn) {
            SecondaryButton(
                text = stringResource(R.string.account_sign_in),
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// Collage de tres fotos (una grande y dos pequeñas); brillo de carga hasta tener las portadas.
@Composable
private fun Collage(covers: List<String>?) {
    val gap = MaterialTheme.spacing.sm
    if (covers == null || covers.size < 3) {
        ShimmerBox(
            Modifier
                .fillMaxWidth()
                .aspectRatio(MAIN_PHOTO_ASPECT)
                .clip(MaterialTheme.shapes.large),
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        VenuePhoto(
            url = covers[0],
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().aspectRatio(MAIN_PHOTO_ASPECT).clip(MaterialTheme.shapes.large),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            covers.drop(1).forEach { url ->
                VenuePhoto(
                    url = url,
                    contentDescription = null,
                    modifier = Modifier.weight(1f).aspectRatio(SIDE_PHOTO_ASPECT).clip(MaterialTheme.shapes.large),
                )
            }
        }
    }
}
