package com.breadwallet.tools.preferences

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurityPreferences (context: Context) {
    private val spec = KeyGenParameterSpec.Builder(
        MasterKey.DEFAULT_MASTER_KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
        .build()

    private val masterKey = MasterKey.Builder(context)
        .setKeyGenParameterSpec(spec)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "my_shared_preferences",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveData(key: String, value: String) {
        with (sharedPreferences.edit()) {
            putString(key, value)
            commit()
        }
    }

    fun saveBooleanData(key: String, value: Boolean){
        with (sharedPreferences.edit()) {
            putBoolean(key, value)
            commit()
        }
    }

    fun getData(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun getDataBoolean(key: String) : Boolean {
        return sharedPreferences.getBoolean(key, false)
    }
}