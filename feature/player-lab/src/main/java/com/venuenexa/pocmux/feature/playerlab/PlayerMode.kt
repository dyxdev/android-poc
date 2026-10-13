package com.venuenexa.pocmux.feature.playerlab

// Modo de reproductor del laboratorio (prompt §2). Mismo asset firmado en los tres.
enum class PlayerMode {
    // Candidato: Mux Player + INetworkRequest con puerta + ExoPlayerBinding.
    MUX_GATED,

    // Control: Mux Player con solo addNetwork, sin binding (debe seguir enviando).
    MUX_UNGATED,

    // Reserva: Media3 propio con URL HLS firmada, sin SDK de Mux.
    MEDIA3_ONLY,
}
