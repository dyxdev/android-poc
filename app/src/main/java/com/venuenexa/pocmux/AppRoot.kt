package com.venuenexa.pocmux

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venuenexa.pocmux.feature.account.WelcomeRoute

// Raíz de la app: la bienvenida solo la primera vez; después, el grafo de navegación.
@Composable
fun AppRoot(viewModel: AppRootViewModel = hiltViewModel()) {
    val welcomeSeen by viewModel.welcomeSeen.collectAsStateWithLifecycle()
    when (welcomeSeen) {
        null -> Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        false -> WelcomeRoute(onFinished = viewModel::onWelcomeFinished)
        true -> AppNavHost()
    }
}
