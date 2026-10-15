package com.venuenexa.pocmux

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.venuenexa.pocmux.core.designsystem.VenueNexaTheme
import dagger.hilt.android.AndroidEntryPoint

// Actividad única: dibuja de borde a borde y pone el tema de VenueNexa. La bienvenida y la navegación viven en AppRoot.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Solo hay tema claro: barras transparentes con iconos oscuros, sin depender del modo oscuro del sistema.
        val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
        setContent {
            VenueNexaTheme {
                AppRoot()
            }
        }
    }
}
