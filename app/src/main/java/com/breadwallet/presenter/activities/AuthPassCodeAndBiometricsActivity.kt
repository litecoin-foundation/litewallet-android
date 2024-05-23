package com.breadwallet.presenter.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.breadwallet.entities.PreferencesKeys
import com.breadwallet.presenter.screens.Biometrics
import com.breadwallet.presenter.screens.ReEnterPassCodeBio
import com.breadwallet.tools.listeners.BiometricsListeners
import com.breadwallet.tools.util.initBiometricPrompt
import com.breadwallet.tools.viewmodel.SecurityViewModel
import com.breadwallet.ui.theme.LitewalletAndroidTheme
import com.example.trypasscodeandbiometrics.screens.SetPassCode

class AuthPassCodeAndBiometricsActivity : AppCompatActivity() {
    private lateinit var viewModel: SecurityViewModel
    private lateinit var biometricPrompt: BiometricPrompt
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LitewalletAndroidTheme {
                val categoryActivity = intent.getStringExtra("category")
                viewModel = ViewModelProvider(this)[SecurityViewModel::class.java]
                val navController = rememberNavController()
                val listener = object : BiometricsListeners {
                    override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
                        if(error == BiometricPrompt.ERROR_NEGATIVE_BUTTON){
                            viewModel.saveBooleanData(PreferencesKeys.IS_AUTHENTICATED_WITH_BIOMETRICS, false)
                            navController.navigate(ScreenPassCodeBio.Biometrics.route)
                        }
                    }
                    override fun onAuthenticationFailed() {
                        viewModel.saveBooleanData(PreferencesKeys.IS_AUTHENTICATED_WITH_BIOMETRICS, false)
                    }

                    override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
                        viewModel.saveBooleanData(PreferencesKeys.IS_AUTHENTICATED_WITH_BIOMETRICS, true)
                        navController.navigate(ScreenPassCodeBio.ExampleProtectedScreen.route)
                    }
                }
                biometricPrompt = initBiometricPrompt(this, listener)
                PassCodeNav(
                    navController = navController,
                    securityViewModel = viewModel,
                    biometricPrompt = biometricPrompt,
                    categoryActivity = categoryActivity
                )
            }
        }
    }
}

sealed class ScreenPassCodeBio(val route: String) {
    data object EnterPassCode : ScreenPassCodeBio("enterpin")
    data object ReEnterPassCode : ScreenPassCodeBio("reenterpin")
    data object Biometrics : ScreenPassCodeBio("biometrics")
    data object ExampleProtectedScreen : ScreenPassCodeBio("example")
}

@Composable
fun PassCodeNav(
    navController: NavHostController,
    securityViewModel: SecurityViewModel,
    biometricPrompt: BiometricPrompt,
    categoryActivity: String?
) {
    val context = LocalContext.current
    NavHost(navController, startDestination = ScreenPassCodeBio.EnterPassCode.route) {
        composable(ScreenPassCodeBio.EnterPassCode.route) {
//            if(securityViewModel.getDataBoolean("is_authenticated_with_biometrics") ==  true){
//                navController.navigate(Screen.ExampleProtectedScreen.route)
//            }
            SetPassCode(
                navController = navController,
                onBackPress = { (context as? ComponentActivity)?.finishAffinity() },
                passCodeViewModel = securityViewModel,
                categoryActivity = categoryActivity
            )
        }
        composable(ScreenPassCodeBio.ReEnterPassCode.route) {
            if(securityViewModel.getDataBoolean(PreferencesKeys.IS_AUTHENTICATED_WITH_PASSCODE)!!){
                ReEnterPassCodeBio(
                    navController = navController,
                    passCodeViewModel = securityViewModel
                )
            }else{
                navController.navigate(ScreenPassCodeBio.EnterPassCode.route)
            }
        }
        composable(ScreenPassCodeBio.Biometrics.route){
            val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                ?.onBackPressedDispatcher

            val backCallback = remember {
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        navController.navigate("reenterpin")
                    }
                }
            }

            DisposableEffect(key1 = onBackPressedDispatcher) {
                onBackPressedDispatcher?.addCallback(backCallback)
                onDispose {
                    backCallback.remove()
                }
            }
            Biometrics(biometricPrompt = biometricPrompt, securityViewModel = securityViewModel, navController = navController)
        }
        composable(ScreenPassCodeBio.ExampleProtectedScreen.route){
            if(securityViewModel.getDataBoolean(PreferencesKeys.IS_AUTHENTICATED_WITH_BIOMETRICS) == true){
                ExampleProtectedScreen()
            }else{
                navController.navigate(ScreenPassCodeBio.Biometrics.route)
            }

        }
    }
}

@Composable
fun ExampleProtectedScreen(){
    Text("Authorization Succeed")
}