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
        // De momento solo hay tema claro (simplicidad de la PoC): barras transparentes con iconos oscuros. Con el tema
        // oscuro habrá que elegir el estilo de las barras según el tema.
        val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
        setContent {
            VenueNexaTheme {
                AppRoot()
            }
        }
    }
}
