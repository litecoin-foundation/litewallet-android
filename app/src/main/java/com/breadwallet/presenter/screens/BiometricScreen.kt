package com.breadwallet.presenter.screens

import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.breadwallet.R
import com.breadwallet.entities.PreferencesKeys
import com.breadwallet.presenter.activities.ScreenPassCodeBio
import com.breadwallet.tools.util.AppTheme
import com.breadwallet.tools.util.authenticateUser
import com.breadwallet.tools.viewmodel.SecurityViewModel
import com.breadwallet.ui.theme.barlowSemiCondensed_normal
import com.breadwallet.ui.theme.barlowSemiCondensed_semi_bold

@Composable
fun Biometrics(modifier: Modifier = Modifier,
               biometricPrompt: BiometricPrompt,
               securityViewModel: SecurityViewModel,
               navController: NavHostController,
               theme: State<AppTheme>
){
    val context = LocalContext.current
    val biometricManager: BiometricManager =  BiometricManager.from(context)
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(200.dp))
        Image(
            modifier = Modifier
                .size(200.dp),
            painter = if(theme.value == AppTheme.MODE_DAY) painterResource(id = R.drawable.litewallet_logo_black_without_text) else painterResource(
                id = R.drawable.litewallet_logotype_white_200
            ),
            contentDescription = stringResource(id = R.string.litewallet_logo)
        )
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.protect_your_wallet),
            fontFamily = barlowSemiCondensed_semi_bold,
            fontSize = 24.sp
        )
        Text(
            modifier = Modifier
                .width(330.dp)
                .padding(top = 18.dp),
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.add_pin_or_biometric),
            fontFamily = barlowSemiCondensed_normal,
            color = if(theme.value == AppTheme.MODE_DAY) Color.DarkGray else Color.LightGray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            modifier = Modifier
                .width(35.dp)
                .padding(bottom = 30.dp)
            ,
            onClick = {
                authenticateUser(biometricPrompt)

                when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                    BiometricManager.BIOMETRIC_SUCCESS ->{
                        if(securityViewModel.getDataBoolean(PreferencesKeys.IS_AUTHENTICATED_WITH_BIOMETRICS)){
                            navController.navigate(ScreenPassCodeBio.Biometrics.route)
                        }
                    }
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                        securityViewModel.saveBooleanData(PreferencesKeys.IS_AUTHENTICATED_WITH_BIOMETRICS, false)
                    }
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                        securityViewModel.saveBooleanData(PreferencesKeys.IS_AUTHENTICATED_WITH_BIOMETRICS, false)
                        AlertDialog.Builder(context)
                            .setTitle(R.string.failed_authentication_biometric)
                            .setMessage(R.string.hardware_unavailable)
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->{
                        securityViewModel.saveBooleanData(PreferencesKeys.IS_AUTHENTICATED_WITH_BIOMETRICS, false)
                        AlertDialog.Builder(context)
                            .setTitle(R.string.no_biometrics)
                            .setMessage(R.string.no_biometrics_set_up)
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                    BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                        securityViewModel.saveBooleanData(PreferencesKeys.IS_AUTHENTICATED_WITH_BIOMETRICS, false)
                        AlertDialog.Builder(context)
                            .setTitle(R.string.failed_authentication_biometric)
                            .setMessage(R.string.hardware_not_compatible_with_biometric)
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                    BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->{
                        securityViewModel.saveBooleanData(PreferencesKeys.IS_AUTHENTICATED_WITH_BIOMETRICS, false)
                        AlertDialog.Builder(context)
                            .setTitle(R.string.failed_authentication_biometric)
                            .setMessage(R.string.failed_to_authenticate_unknown)
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
            }
        ) {
            Image(
                modifier = Modifier
                    .size(50.dp)
                ,
                painter = if(theme.value == AppTheme.MODE_DAY) painterResource(R.drawable.fingerprint_black) else painterResource(
                    id = R.drawable.fingerprint_white
                ),
                contentDescription = stringResource(id = R.string.biometrics)
            )
        }
    }
}