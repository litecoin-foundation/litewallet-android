package com.breadwallet.tools.util
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.breadwallet.R
import com.breadwallet.tools.listeners.BiometricsListeners

fun authenticateUser(biometricPrompt: BiometricPrompt) {
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(R.string.biometrics.toString())
        .setDescription(R.string.authenticate_with_biometric.toString())
        .setNegativeButtonText("Cancel")
        .build()

    biometricPrompt.authenticate(promptInfo)
}

@Composable
fun initBiometricPrompt(
    activity: FragmentActivity,
    listener: BiometricsListeners
): BiometricPrompt {
    val executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            listener.onBiometricAuthenticateError(errorCode, errString.toString())
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            listener.onAuthenticationFailed()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            listener.onBiometricAuthenticateSuccess(result)
        }
    }
    return BiometricPrompt(activity, executor, callback)
}