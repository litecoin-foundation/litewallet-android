package com.breadwallet.tools.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.lifecycle.AndroidViewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurityViewModel (application: Application) : AndroidViewModel(application) {

    private lateinit var sharedPreferences: SharedPreferences

    init {
        setupEncryptedSharedPreferences("my_shared_preferences")
    }

    private fun setupEncryptedSharedPreferences(sharedPrefFile: String) {
        val spec = KeyGenParameterSpec.Builder(
            MasterKey.DEFAULT_MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
            .build()

        val masterKey = MasterKey.Builder(getApplication()).setKeyGenParameterSpec(spec).build()

        sharedPreferences = EncryptedSharedPreferences.create(
            getApplication(),
            sharedPrefFile,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

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

    fun getDataBoolean(key: String) : Boolean? {
        return sharedPreferences.getBoolean(key, false)
    }
}