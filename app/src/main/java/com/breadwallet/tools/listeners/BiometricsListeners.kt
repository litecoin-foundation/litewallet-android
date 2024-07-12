package com.breadwallet.tools.listeners

import androidx.biometric.BiometricPrompt

interface BiometricsListeners {
    fun onBiometricAuthenticateError(error: Int, errMsg: String)
    fun onAuthenticationFailed()
    fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult)
}