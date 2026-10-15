package com.venuenexa.pocmux

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.venuenexa.pocmux.core.designsystem.LocalAnimatedVisibilityScope
import com.venuenexa.pocmux.core.designsystem.LocalSharedTransitionScope
import com.venuenexa.pocmux.feature.account.DiagnosticsRoute
import com.venuenexa.pocmux.feature.detail.VenueDetailRoute
import com.venuenexa.pocmux.feature.playerlab.PlayerLabRoute

// Grafo de navegación de la app. El SharedTransitionLayout y los dos ámbitos de composición permiten que la foto
// de la tarjeta viaje al detalle sin que las features se conozcan entre sí.
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(navController = navController, startDestination = MainDestination) {
                composable<MainDestination> {
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        MainScreen(
                            onVenueClick = { venueId, sharedKey ->
                                navController.navigate(VenueDetailDestination(venueId, sharedKey))
                            },
                            onOpenDiagnostics = { navController.navigate(DiagnosticsDestination) },
                        )
                    }
                }
                composable<VenueDetailDestination> { entry ->
                    val route = entry.toRoute<VenueDetailDestination>()
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        VenueDetailRoute(
                            venueId = route.venueId,
                            sharedKey = route.sharedKey,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
                composable<DiagnosticsDestination> {
                    DiagnosticsRoute(
                        onBack = { navController.popBackStack() },
                        onOpenLab = { navController.navigate(LabDestination) },
                    )
                }
                composable<LabDestination> { PlayerLabRoute(context = LocalContext.current) }
            }
        }
    }
}
