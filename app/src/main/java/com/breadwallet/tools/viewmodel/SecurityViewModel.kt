package com.breadwallet.tools.viewmodel

import androidx.lifecycle.ViewModel
import com.breadwallet.entities.SecureRepository

class SecurityViewModel () : ViewModel() {
    fun saveData(key: String, value: String) {
        SecureRepository.saveData(key, value)
    }

    fun getData(key: String): String? {
        return SecureRepository.getData(key)
    }

    fun saveBooleanData(key: String, value: Boolean) {
        SecureRepository.saveDataBoolean(key, value)
    }

    fun getDataBoolean(key: String): Boolean {
        return SecureRepository.getDataBoolean(key)
    }

}