package com.venuenexa.pocmux.feature.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.venuenexa.pocmux.core.designsystem.PrimaryButton
import com.venuenexa.pocmux.core.designsystem.ShimmerBox
import com.venuenexa.pocmux.core.designsystem.formatPrice
import com.venuenexa.pocmux.core.designsystem.spacing
import kotlinx.coroutines.launch
import com.venuenexa.pocmux.core.designsystem.R as DsR

// Pantalla sin estado del detalle: previsualizable (ver DetailPreviews).
@Composable
fun DetailScreen(
    state: DetailUiState,
    sharedKey: String,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val reserveSoon = stringResource(R.string.detail_reserve_soon)
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (state is DetailUiState.Content) {
                ReserveBar(
                    pricePerDay = state.venue.pricePerDay,
                    available = state.venue.isAvailable,
                    onReserve = { scope.launch { snackbar.showSnackbar(reserveSoon) } },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                DetailUiState.Loading -> LoadingDetail(onBack = onBack)
                DetailUiState.NotFound -> NotFoundDetail(onBack = onBack)
                is DetailUiState.Content ->
                    DetailContent(
                        state = state,
                        sharedKey = sharedKey,
                        onBack = onBack,
                        onToggleSaved = onToggleSaved,
                    )
            }
        }
    }
}

@Composable
private fun DetailContent(
    state: DetailUiState.Content,
    sharedKey: String,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
    ) {
        DetailGallery(
            venue = state.venue,
            saved = state.saved,
            sharedKey = sharedKey,
            onBack = onBack,
            onToggleSaved = onToggleSaved,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg),
        )
        DetailHeader(venue = state.venue, modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg))
        DetailFigures(venue = state.venue, modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg))
        state.venue.tourPlaybackId?.let { playbackId ->
            TourSection(
                venue = state.venue,
                playbackId = playbackId,
                analyticsConsent = state.analyticsConsent,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg),
            )
        }
    }
}

@Composable
private fun LoadingDetail(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.lg)) {
        ShimmerBox(
            Modifier
                .fillMaxWidth()
                .aspectRatio(GALLERY_ASPECT)
                .clip(MaterialTheme.shapes.medium),
        )
        BackButton(onBack = onBack, modifier = Modifier.padding(MaterialTheme.spacing.sm))
    }
}

@Composable
private fun NotFoundDetail(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.lg)) {
        BackButton(onBack = onBack)
        Text(
            text = stringResource(R.string.detail_not_found),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// Barra inferior con el precio y «Reservar». La reserva y el pago llegan en una entrega posterior.
@Composable
private fun ReserveBar(
    pricePerDay: Int,
    available: Boolean,
    onReserve: () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier =
                Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(MaterialTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(DsR.string.venue_price_per_day, formatPrice(pricePerDay, locale)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f),
            )
            PrimaryButton(
                text = stringResource(if (available) R.string.detail_reserve else DsR.string.venue_unavailable),
                onClick = onReserve,
                enabled = available,
            )
        }
    }
}
