package com.breadwallet.presenter.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.breadwallet.entities.PreferencesKeys
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
                val categoryActivity = intent.getStringExtra("category")
                viewModel = ViewModelProvider(this)[SecurityViewModel::class.java]
                val navController = rememberNavController()
                PassCodeNav(navController = navController, securityViewModel = viewModel, categoryActivity = categoryActivity)
            }
        }
    }
}

sealed class ScreenPassCode(val route: String) {
    data object EnterPassCode : ScreenPassCode("enter_pass_code")
    data object ReEnterPassCode : ScreenPassCode("reenter_pass_code")
    data object ExampleProtectedScreenPassCode : ScreenPassCodeBio("example")
}


@Composable
fun PassCodeNav(
    navController: NavHostController,
    securityViewModel: SecurityViewModel,
    categoryActivity: String?
) {
    val context = LocalContext.current
    NavHost(navController, startDestination = ScreenPassCode.EnterPassCode.route) {
        composable(ScreenPassCode.EnterPassCode.route) {
            SetPassCode(
                navController = navController,
                onBackPress = { (context as? ComponentActivity)?.finishAffinity() },
                passCodeViewModel = securityViewModel,
                categoryActivity = categoryActivity
            )
        }
        composable(ScreenPassCode.ReEnterPassCode.route) {
            if(securityViewModel.getDataBoolean(PreferencesKeys.IS_AUTHENTICATED_WITH_PASSCODE)!!){
                ReEnterPassCode(
                    navController = navController,
                    passCodeViewModel = securityViewModel
                )
            }else{
                navController.navigate(ScreenPassCode.EnterPassCode.route)
            }
        }
        composable(ScreenPassCode.ExampleProtectedScreenPassCode.route){
            ExampleProtectedScreen()
        }
    }
}