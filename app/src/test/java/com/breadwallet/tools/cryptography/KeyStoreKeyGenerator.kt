package com.breadwallet.tools.cryptography


import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.AUTH_BIOMETRIC_STRONG
import android.security.keystore.KeyProperties.KEY_ALGORITHM_AES
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import com.breadwallet.tools.security.BRKeyStore
import com.breadwallet.tools.security.BRKeyStore.NEW_BLOCK_MODE
import com.breadwallet.tools.security.BRKeyStore.NEW_PADDING
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

interface KeyStoreKeyGenerator {
    fun generateKey(alias: String, isAuthRequired: Boolean, authTimeout: Int?): SecretKey

    class Impl : KeyStoreKeyGenerator {

        private val keyGenerator by lazy {
            KeyGenerator.getInstance(KEY_ALGORITHM_AES, BRKeyStore.ANDROID_KEY_STORE)
        }

        override fun generateKey(
            alias: String,
            isAuthRequired: Boolean,
            authTimeout: Int?
        ): SecretKey {
            val keySpecBuilder = getKeyGenSpecBuilder(alias)
            if (isAuthRequired) {
                setUserAuth(keySpecBuilder, authTimeout)
            }
            keyGenerator.init(keySpecBuilder.build())
            return keyGenerator.generateKey()
        }

        private fun getKeyGenSpecBuilder(alias: String): KeyGenParameterSpec.Builder {
            val purposes = PURPOSE_DECRYPT or PURPOSE_ENCRYPT

            return KeyGenParameterSpec.Builder(alias, purposes)
                .setBlockModes(NEW_BLOCK_MODE)
                .setRandomizedEncryptionRequired(false)
                .setEncryptionPaddings(NEW_PADDING)
        }

        private fun setUserAuth(
            builder: KeyGenParameterSpec.Builder, authTimeout: Int?
        ) {
            builder.setUserAuthenticationRequired(true)
            if (authTimeout != null) {
                setAuthTimeout(builder, authTimeout)
            }
        }

        private fun setAuthTimeout(builder: KeyGenParameterSpec.Builder, authTimeout: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (authTimeout != 0) {
                    builder.setUserAuthenticationParameters(authTimeout, AUTH_BIOMETRIC_STRONG)
                }
            } else {
                val timeout = if (authTimeout == 0) -1 else authTimeout
                builder.setUserAuthenticationValidityDurationSeconds(timeout)
            }
        }

    }
}