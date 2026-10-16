package com.venuenexa.pocmux

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venuenexa.pocmux.core.context.ContextViewModel
import com.venuenexa.pocmux.core.context.UserContext
import com.venuenexa.pocmux.feature.account.AccountRoute
import com.venuenexa.pocmux.feature.home.HomeRoute
import com.venuenexa.pocmux.feature.owner.OwnerRoute

private const val CROSSFADE_MS = 200

private enum class Tab(
    @StringRes val label: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    Home(R.string.nav_home, Icons.Outlined.Home, Icons.Filled.Home),
    Panel(R.string.nav_panel, Icons.AutoMirrored.Outlined.List, Icons.AutoMirrored.Filled.List),
    Account(R.string.nav_account, Icons.Outlined.Person, Icons.Filled.Person),
}

// Cada contexto tiene su barra (ADR-AND-0026); Cuenta es común a todos.
private val GuestTabs = listOf(Tab.Home, Tab.Account)
private val OwnerTabs = listOf(Tab.Panel, Tab.Account)

// Destino principal: barra inferior según el contexto activo. El detalle de un venue y el diagnóstico se apilan
// encima (AppNavHost). Cambiar de contexto no toca la sesión: solo cambia la barra y el contenido.
@Composable
fun MainScreen(
    onVenueClick: (String, String) -> Unit,
    onOpenDiagnostics: () -> Unit,
) {
    val contextViewModel: ContextViewModel = hiltViewModel()
    val contextState by contextViewModel.state.collectAsStateWithLifecycle()
    val isOwner = contextState.active == UserContext.OWNER
    val tabs = if (isOwner) OwnerTabs else GuestTabs
    // La última pestaña de cada contexto se recuerda al volver a él.
    var guestIndex by rememberSaveable { mutableIntStateOf(0) }
    var ownerIndex by rememberSaveable { mutableIntStateOf(0) }
    val tab = tabs[(if (isOwner) ownerIndex else guestIndex).coerceIn(tabs.indices)]
    val select: (Tab) -> Unit = { selected ->
        if (isOwner) {
            ownerIndex = tabs.indexOf(selected)
        } else {
            guestIndex = tabs.indexOf(selected)
        }
    }
    // El desplazamiento de Inicio sobrevive al cambio de pestaña.
    val homeListState = rememberLazyListState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomBar(tabs = tabs, selected = tab, onSelected = select) },
    ) { padding ->
        Crossfade(targetState = tab, animationSpec = tween(CROSSFADE_MS), label = "tab") { current ->
            when (current) {
                Tab.Home ->
                    HomeRoute(
                        onVenueClick = onVenueClick,
                        onAccountClick = { select(Tab.Account) },
                        modifier = Modifier.padding(padding),
                        listState = homeListState,
                    )
                Tab.Panel -> OwnerRoute(modifier = Modifier.padding(padding))
                Tab.Account ->
                    AccountRoute(
                        onOpenDiagnostics = if (BuildConfig.DEBUG) onOpenDiagnostics else null,
                        modifier = Modifier.padding(padding),
                    )
            }
        }
    }
}

@Composable
private fun BottomBar(
    tabs: List<Tab>,
    selected: Tab,
    onSelected: (Tab) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        tabs.forEach { item ->
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
