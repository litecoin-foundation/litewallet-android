package com.breadwallet.presenter.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
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
import com.breadwallet.tools.viewmodel.SecurityViewModel
import com.breadwallet.ui.theme.LitewalletAndroidTheme
import com.example.trypasscodeandbiometrics.screens.ReEnterPassCode
import com.example.trypasscodeandbiometrics.screens.SetPassCode

class AuthPassCodeActivity : AppCompatActivity() {
    private lateinit var viewModel: SecurityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            LitewalletAndroidTheme {
                viewModel = ViewModelProvider(this)[SecurityViewModel::class.java]
                val navController = rememberNavController()
                PassCodeNav(navController = navController, securityViewModel = viewModel)
            }
        }
    }
}

sealed class ScreenPassCode(val route: String) {
    data object EnterPassCode : ScreenPassCode("enter_pass_code")
    data object ReEnterPassCode : ScreenPassCode("reenter_pass_code")
}


@Composable
fun PassCodeNav(
    navController: NavHostController,
    securityViewModel: SecurityViewModel,
) {
    val context = LocalContext.current
    NavHost(navController, startDestination = ScreenPassCode.EnterPassCode.route) {
        composable(ScreenPassCode.EnterPassCode.route) {
            SetPassCode(
                navController = navController,
                onBackPress = { (context as? ComponentActivity)?.finishAffinity() },
                passCodeViewModel = securityViewModel
            )
        }
        composable(ScreenPassCode.ReEnterPassCode.route) {
            if(securityViewModel.getDataBoolean("is_authenticated_with_passcode")!!){
                ReEnterPassCode(
                    navController = navController,
                    passCodeViewModel = securityViewModel
                )
            }else{
                navController.navigate(ScreenPassCode.EnterPassCode.route)
            }
        }
    }
}