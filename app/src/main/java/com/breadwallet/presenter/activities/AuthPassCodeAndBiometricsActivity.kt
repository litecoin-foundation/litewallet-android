package com.breadwallet.presenter.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
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
import com.breadwallet.R
import com.breadwallet.presenter.screens.Biometrics
import com.breadwallet.presenter.screens.ReEnterPassCodeBio
import com.breadwallet.tools.viewmodel.SecurityViewModel
import com.breadwallet.ui.theme.LitewalletAndroidTheme
import com.example.trypasscodeandbiometrics.screens.SetPassCode

class AuthPassCodeAndBiometricsActivity : AppCompatActivity() {
    private lateinit var viewModel: SecurityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            LitewalletAndroidTheme {
                viewModel = ViewModelProvider(this)[SecurityViewModel::class.java]
                val navController = rememberNavController()
                PassCodeBioNav(navController = navController, securityViewModel = viewModel)
            }
        }
    }
}

sealed class ScreenPassCodeBio(val route: String) {
    data object EnterPassCode : ScreenPassCodeBio("enter_pass_code")
    data object ReEnterPassCode : ScreenPassCodeBio("reenter_pass_code")
    data object Biometrics : ScreenPassCodeBio("biometrics")
}

@Composable
fun PassCodeBioNav(
    navController: NavHostController,
    securityViewModel: SecurityViewModel,
) {
    val context = LocalContext.current
    NavHost(navController, startDestination = ScreenPassCodeBio.EnterPassCode.route) {
        composable(ScreenPassCodeBio.EnterPassCode.route) {
            SetPassCode(
                navController = navController,
                onBackPress = { (context as? ComponentActivity)?.finishAffinity() },
                passCodeViewModel = securityViewModel
            )
        }
        composable(ScreenPassCodeBio.ReEnterPassCode.route) {
            if(securityViewModel.getDataBoolean("is_authenticated_with_passcode")!!){
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
                        navController.navigate(ScreenPassCodeBio.ReEnterPassCode.route)
                    }
                }
            }

            DisposableEffect(key1 = onBackPressedDispatcher) {
                onBackPressedDispatcher?.addCallback(backCallback)
                onDispose {
                    backCallback.remove()
                }
            }
            Biometrics(securityViewModel = securityViewModel, navController = navController)
        }
    }
}