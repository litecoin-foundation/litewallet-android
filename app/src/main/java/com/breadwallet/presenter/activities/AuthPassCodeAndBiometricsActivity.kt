package com.breadwallet.presenter.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.breadwallet.R
import com.breadwallet.entities.PreferencesKeys
import com.breadwallet.presenter.activities.intro.SetSecurityActivity
import com.breadwallet.presenter.screens.Biometrics
import com.breadwallet.presenter.screens.ReEnterPassCodeBio
import com.breadwallet.tools.listeners.BiometricsListeners
import com.breadwallet.tools.util.AppTheme
import com.breadwallet.tools.util.ThemeSetting
import com.breadwallet.tools.util.initBiometricPrompt
import com.breadwallet.tools.viewmodel.SecurityViewModel
import com.breadwallet.ui.theme.LitewalletAndroidTheme
import com.breadwallet.ui.theme.barlowSemiCondensed_bold
import com.breadwallet.ui.theme.barlowSemiCondensed_semi_bold
import com.example.trypasscodeandbiometrics.screens.SetPassCode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class AuthPassCodeAndBiometricsActivity : AppCompatActivity() {

    @Inject
    lateinit var themeSetting: ThemeSetting

    private lateinit var viewModel: SecurityViewModel
    private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val theme = themeSetting.themeStream.collectAsState()
            val useDarkColors = when (theme.value) {
                AppTheme.MODE_AUTO -> isSystemInDarkTheme()
                AppTheme.MODE_DAY -> false
                AppTheme.MODE_NIGHT -> true
            }
            LitewalletAndroidTheme (
                darkTheme = useDarkColors
            ){
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
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
                        categoryActivity = categoryActivity,
                        theme = theme
                    )
                }
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
    categoryActivity: String?,
    theme: State<AppTheme>
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
                categoryActivity = categoryActivity,
                theme = theme
            )
        }
        composable(ScreenPassCodeBio.ReEnterPassCode.route) {
            if(securityViewModel.getDataBoolean(PreferencesKeys.IS_AUTHENTICATED_WITH_PASSCODE)!!){
                ReEnterPassCodeBio(
                    navController = navController,
                    passCodeViewModel = securityViewModel,
                    theme = theme
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
            Biometrics(biometricPrompt = biometricPrompt, securityViewModel = securityViewModel, navController = navController, theme = theme)
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
    val context = LocalContext.current
    val intent = Intent(context, Example::class.java)
    context.startActivity(intent)
}