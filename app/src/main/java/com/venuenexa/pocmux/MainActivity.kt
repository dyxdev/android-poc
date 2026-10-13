package com.venuenexa.pocmux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.venuenexa.pocmux.feature.playerlab.PlayerLabRoute
import dagger.hilt.android.AndroidEntryPoint

// Pantalla única de la PoC (fuera de alcance: navegación entre pantallas).
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocMuxTheme {
                PlayerLabEntry()
            }
        }
    }
}

@Composable
private fun PlayerLabEntry() {
    val context = LocalContext.current
    PlayerLabRoute(context = context)
}

@Composable
private fun PocMuxTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
