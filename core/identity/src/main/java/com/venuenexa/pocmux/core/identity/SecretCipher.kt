package com.venuenexa.pocmux.core.identity

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

// Cifrado de los tokens en disco (ADR-AND-0008, RNF-043).
interface SecretCipher {
    fun encrypt(plain: ByteArray): ByteArray

    // Lanza GeneralSecurityException si la clave ya no sirve o el blob está dañado.
    @Throws(GeneralSecurityException::class)
    fun decrypt(blob: ByteArray): ByteArray
}

// AES-GCM con una clave no exportable de Android Keystore. El blob es IV (12 bytes) + texto cifrado.
// No se prueba en JVM (no hay Keystore): su prueba es la del dispositivo, ver docs/identidad-keycloak.md.
class KeystoreCipher
    @Inject
    constructor() : SecretCipher {
        override fun encrypt(plain: ByteArray): ByteArray {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key())
            return cipher.iv + cipher.doFinal(plain)
        }

        override fun decrypt(blob: ByteArray): ByteArray {
            if (blob.size <= IV_BYTES) throw GeneralSecurityException("blob demasiado corto")
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, blob, 0, IV_BYTES))
            return cipher.doFinal(blob, IV_BYTES, blob.size - IV_BYTES)
        }

        private fun key(): SecretKey {
            val store = KeyStore.getInstance(PROVIDER).apply { load(null) }
            (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
            generator.init(
                KeyGenParameterSpec
                    .Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_BITS)
                    .build(),
            )
            return generator.generateKey()
        }

        private companion object {
            const val PROVIDER = "AndroidKeyStore"
            const val ALIAS = "pocmux_identity_aes"
            const val TRANSFORMATION = "AES/GCM/NoPadding"
            const val IV_BYTES = 12
            const val TAG_BITS = 128
            const val KEY_BITS = 256
        }
    }
