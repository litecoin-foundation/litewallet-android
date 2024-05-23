package com.breadwallet.entities

import android.content.Context
import com.breadwallet.tools.preferences.SecurityPreferences

object SecureRepository {
    private lateinit var securePreferences: SecurityPreferences

    fun initialize(context: Context) {
        securePreferences = SecurityPreferences(context)
    }

    fun saveData(key: String, value: String) {
        securePreferences.saveData(key, value)
    }

    fun getData(key: String): String? {
        return securePreferences.getData(key)
    }

    fun saveDataBoolean(key: String, value: Boolean) {
        securePreferences.saveBooleanData(key, value)
    }

    fun getDataBoolean(key: String): Boolean {
        return securePreferences.getDataBoolean(key)
    }
}