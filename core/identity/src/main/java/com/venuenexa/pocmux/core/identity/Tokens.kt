package com.venuenexa.pocmux.core.identity

import java.util.Base64

// Tokens de la sesión. Nunca van a logs ni a la UI.
data class Tokens(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String?,
    val expiresAtMs: Long?,
) {
    // El toString generado de un data class volcaría los tokens: solo el vencimiento.
    override fun toString(): String = "Tokens(expiresAtMs=$expiresAtMs)"
}

// Formato propio y mínimo (org.json no existe en los tests JVM): versión y cuatro campos, uno por
// línea, los tokens en Base64 URL. Otra versión o forma se trata como sesión no válida.
internal object TokenCodec {
    private const val VERSION = "v1"
    private const val ACCESS_LINE = 1
    private const val REFRESH_LINE = 2
    private const val ID_LINE = 3
    private const val EXPIRES_LINE = 4
    private const val LINES = 5

    fun encode(tokens: Tokens): String =
        listOf(
            VERSION,
            encodeField(tokens.accessToken),
            encodeField(tokens.refreshToken),
            encodeField(tokens.idToken),
            tokens.expiresAtMs?.toString().orEmpty(),
        ).joinToString("\n")

    fun decode(raw: String): Tokens? {
        val lines = raw.split("\n")
        return if (lines.size == LINES && lines[0] == VERSION) decodeLines(lines) else null
    }

    private fun decodeLines(lines: List<String>): Tokens? =
        try {
            val accessToken = decodeField(lines[ACCESS_LINE])
            val expiresRaw = lines[EXPIRES_LINE]
            val expires = expiresRaw.toLongOrNull()
            if (accessToken == null || (expiresRaw.isNotEmpty() && expires == null)) {
                null
            } else {
                Tokens(accessToken, decodeField(lines[REFRESH_LINE]), decodeField(lines[ID_LINE]), expires)
            }
        } catch (ignored: IllegalArgumentException) {
            // Base64 dañado: se trata como sesión no válida.
            null
        }

    private fun encodeField(value: String?): String =
        value?.let { Base64.getUrlEncoder().withoutPadding().encodeToString(it.toByteArray()) }.orEmpty()

    private fun decodeField(value: String): String? =
        if (value.isEmpty()) null else String(Base64.getUrlDecoder().decode(value))
}
