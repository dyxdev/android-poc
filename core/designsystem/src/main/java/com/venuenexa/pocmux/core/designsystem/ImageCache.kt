package com.venuenexa.pocmux.core.designsystem

import android.content.Context
import coil3.SingletonImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Al cerrar sesión se vacían las cachés de imágenes en memoria y en disco (ADR-AND-0013): las fotos de datos
// privados no deben sobrevivir a la sesión. El disco se vacía fuera del hilo principal.
suspend fun clearImageCaches(context: Context) {
    withContext(Dispatchers.IO) {
        val loader = SingletonImageLoader.get(context)
        loader.memoryCache?.clear()
        loader.diskCache?.clear()
    }
}
