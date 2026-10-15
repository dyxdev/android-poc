package com.venuenexa.pocmux

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.venuenexa.pocmux.feature.home.HomeRoute
import com.venuenexa.pocmux.feature.playerlab.PlayerLabRoute
import kotlinx.coroutines.launch

private enum class Tab(
    @StringRes val label: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    Home(R.string.nav_home, Icons.Outlined.Home, Icons.Filled.Home),

    // Cuenta, por ahora, es la pantalla del laboratorio (identidad + Mux + consentimiento). Se rehace en su entrega.
    Account(R.string.nav_account, Icons.Outlined.Person, Icons.Filled.Person),
}

// Barra inferior con dos destinos. El detalle de venue (apilado encima) llega en la siguiente entrega.
@Composable
fun AppShell() {
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val detailSoon = stringResource(R.string.detail_soon)
    // El desplazamiento de Inicio sobrevive al cambio de pestaña.
    val homeListState = rememberLazyListState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { BottomBar(selected = tab, onSelected = { tab = it }) },
    ) { padding ->
        when (tab) {
            Tab.Home ->
                HomeRoute(
                    onVenueClick = { scope.launch { snackbar.showSnackbar(detailSoon) } },
                    onAccountClick = { tab = Tab.Account },
                    modifier = Modifier.padding(padding),
                    listState = homeListState,
                )
            Tab.Account ->
                Box(modifier = Modifier.padding(padding)) {
                    PlayerLabRoute(context = LocalContext.current)
                }
        }
    }
}

@Composable
private fun BottomBar(
    selected: Tab,
    onSelected: (Tab) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        Tab.entries.forEach { item ->
            val isSelected = item == selected
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelected(item) },
                icon = { Icon(if (isSelected) item.selectedIcon else item.icon, contentDescription = null) },
                label = { Text(stringResource(item.label)) },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
            )
        }
    }
}
