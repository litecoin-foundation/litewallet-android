package com.breadwallet.presenter.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.breadwallet.entities.PreferencesKeys
import com.breadwallet.tools.util.AppTheme
import com.breadwallet.tools.util.ThemeSetting
import com.breadwallet.tools.viewmodel.SecurityViewModel
import com.breadwallet.ui.theme.LitewalletAndroidTheme
import com.example.trypasscodeandbiometrics.screens.ReEnterPassCode
import com.example.trypasscodeandbiometrics.screens.SetPassCode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class AuthPassCodeActivity : AppCompatActivity() {
    @Inject
    lateinit var themeSetting: ThemeSetting
    private lateinit var viewModel: SecurityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            val theme = themeSetting.themeStream.collectAsState()
            val useDarkColors = when (theme.value) {
                AppTheme.MODE_AUTO -> isSystemInDarkTheme()
                AppTheme.MODE_DAY -> false
                AppTheme.MODE_NIGHT -> true
            }
            LitewalletAndroidTheme(
                darkTheme = useDarkColors
            ) {
                Surface (
                    color = MaterialTheme.colorScheme.background
                ) {
                    val categoryActivity = intent.getStringExtra("category")
                    viewModel = ViewModelProvider(this)[SecurityViewModel::class.java]
                    val navController = rememberNavController()
                    PassCodeNav(navController = navController, securityViewModel = viewModel, categoryActivity = categoryActivity, theme = theme)
                }
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
    categoryActivity: String?,
    theme: State<AppTheme>
) {
    val context = LocalContext.current
    NavHost(navController, startDestination = ScreenPassCode.EnterPassCode.route) {
        composable(ScreenPassCode.EnterPassCode.route) {
            SetPassCode(
                navController = navController,
                onBackPress = { (context as? ComponentActivity)?.finishAffinity() },
                passCodeViewModel = securityViewModel,
                categoryActivity = categoryActivity,
                theme = theme
            )
        }
        composable(ScreenPassCode.ReEnterPassCode.route) {
            if(securityViewModel.getDataBoolean(PreferencesKeys.IS_AUTHENTICATED_WITH_PASSCODE)!!){
                ReEnterPassCode(
                    navController = navController,
                    passCodeViewModel = securityViewModel,
                    theme = theme
                )
            }else{
                navController.navigate(ScreenPassCode.EnterPassCode.route)
            }
        }
        composable(ScreenPassCode.ExampleProtectedScreenPassCode.route){
            ExampleProtectedScreenPasscode()
        }
    }
}

@Composable
fun ExampleProtectedScreenPasscode(){
    val context = LocalContext.current
    val intent = Intent(context, Example::class.java)
    context.startActivity(intent)
}