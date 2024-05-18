package com.breadwallet.presenter.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.breadwallet.R
import com.breadwallet.presenter.activities.ScreenPassCodeBio
import com.breadwallet.tools.viewmodel.SecurityViewModel
import com.breadwallet.ui.theme.barlowSemiCondensed_normal
import com.breadwallet.ui.theme.barlowSemiCondensed_semi_bold

@Composable
fun Biometrics(modifier: Modifier = Modifier, navController: NavController, securityViewModel: SecurityViewModel){

    IconButton(
        modifier =  modifier
            .padding(top=20.dp, start = 20.dp, end = 20.dp)
        ,
        onClick = {
            navController.navigate(ScreenPassCodeBio.ReEnterPassCode.route)
        }
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = ""
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(200.dp))
        Image(
            modifier = Modifier
                .size(200.dp),
            painter = painterResource(id = R.drawable.litewallet_logo_black_without_text),
            contentDescription = "Litewallet Logo"
        )
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            textAlign = TextAlign.Center,
            text = "Protect Your Wallet",
            fontFamily = barlowSemiCondensed_semi_bold,
            fontSize = 24.sp
        )
        Text(
            modifier = Modifier
                .width(330.dp)
                .padding(top = 18.dp),
            textAlign = TextAlign.Center,
            text = "Adding PIN or biometric you are securing your wallet from unwanted access.",
            fontFamily = barlowSemiCondensed_normal,
            color = Color.DarkGray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            modifier = Modifier
                .width(35.dp)
                .padding(bottom = 30.dp)
            ,
            onClick = {
            }
        ) {
            Image(
                modifier = Modifier
                    .size(50.dp)
                ,
                painter = painterResource(R.drawable.fingerprints),
                contentDescription = "Biometrics"
            )
        }
    }
}
