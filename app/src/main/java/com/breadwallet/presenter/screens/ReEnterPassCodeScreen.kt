package com.example.trypasscodeandbiometrics.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.breadwallet.R
import com.breadwallet.entities.PreferencesKeys
import com.breadwallet.presenter.activities.ScreenPassCode
import com.breadwallet.tools.viewmodel.SecurityViewModel
import com.breadwallet.ui.theme.barlowSemiCondensed_bold
import com.breadwallet.ui.theme.barlowSemiCondensed_semi_bold

@Composable
fun ReEnterPassCode(modifier: Modifier = Modifier, navController: NavController, passCodeViewModel: SecurityViewModel) {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
        ?.onBackPressedDispatcher

    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.navigate(ScreenPassCode.EnterPassCode.route)
            }
        }
    }

    DisposableEffect(key1 = onBackPressedDispatcher) {
        onBackPressedDispatcher?.addCallback(backCallback)
        onDispose {
            backCallback.remove()
        }
    }
    IconButton(
        modifier =  modifier
            .padding(top=20.dp, start = 20.dp, end = 20.dp)
        ,
        onClick = {
            navController.navigate(ScreenPassCode.EnterPassCode.route)
        }
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = ""
        )
    }
    Column (modifier= modifier
        .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier
            .weight(1f)
        )
        Spacer(modifier = Modifier.weight(1f))
        Image(
            modifier = Modifier
                .size(45.dp)
            ,
            painter = painterResource(id = R.drawable.litewallet_logo_black_without_text),
            contentDescription = "Litewallet Logo",
        )
        Text(
            modifier = Modifier
                .padding(top=20.dp),
            text = "Confirm",
            fontFamily = barlowSemiCondensed_bold,
            fontSize = 36.sp,
        )
        Text(
            modifier = Modifier
                .width(250.dp)
                .padding(top = 20.dp, start = 8.dp),
            text = "Your 6-digit passcode should be different than your phone lock",
            fontFamily = barlowSemiCondensed_semi_bold,
            fontSize = 18.sp,
            lineHeight = 28.sp
        )
        Spacer(modifier = modifier.weight(0.5f))
        PasscodeReEnter(navController=navController, passCodeViewModel = passCodeViewModel)
        Spacer(modifier = Modifier
            .weight(1/2f)
        )
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun PasscodeReEnter(modifier: Modifier = Modifier, navController: NavController, passCodeViewModel: SecurityViewModel) {
    val pinState = remember { mutableStateListOf<Boolean>(false, false, false, false, false, false) }
    var reEnteredPassCode by remember { mutableStateOf("") }
    var match = false
    var passCode : String? = ""
    if (reEnteredPassCode.length >= 6){
        passCode = passCodeViewModel.getData(PreferencesKeys.PASS_CODE)
        match = reEnteredPassCode == passCode
        navController.navigate(ScreenPassCode.ExampleProtectedScreenPassCode.route)
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(!match){
            Text(
                modifier = Modifier
                    .padding(bottom=10.dp),
                text = "Wrong Passcode",
                color = Color.Red
            )
        }else{
            Text(text = "")
        }
        Row(
            modifier = modifier
                .padding(bottom = 50.dp)
            ,
        ){
            repeat(6) {
                val dotPainter = if (pinState[it]) R.drawable.ic_pin_dot_black else R.drawable.ellipse_outer_black
                IconButton(
                    modifier = Modifier
                        .width(35.dp)
                    ,
                    onClick = { /*TODO*/ }
                ) {
                    Image(
                        modifier = Modifier
                            .size(if(dotPainter == R.drawable.ellipse_outer_black)18.dp else 26.dp)
                        ,
                        painter = painterResource(dotPainter),
                        contentDescription = ""
                    )
                }
            }
        }
        val rows = listOf(0,1,2,3,4,5,6,7,8,9)
        val firstRow = listOf(rows[1], rows[2], rows[3])
        val secondRow = listOf(rows[4], rows[5], rows[6])
        val thirdRow = listOf(rows[7], rows[8], rows[9])
        val fourthRow = listOf(null, rows[0], null)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp, bottom = 10.dp, start = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(30.dp))
            firstRow.forEach {
                if (it != null) {
                    IconButton(
                        onClick = {
                            val emptyDotIndex = pinState.indexOfFirst { !it }
                            if (emptyDotIndex != -1) {
                                pinState[emptyDotIndex] = true
                            }
                            if(reEnteredPassCode.length < 6) reEnteredPassCode += it
                        },
                    ) {
                        Text(
                            text = it.toString(),
                            color = Color.Black,
                            fontFamily = barlowSemiCondensed_semi_bold,
                            fontSize = 32.sp
                        )
                    }
                    Spacer(modifier = Modifier.size(40.dp))
                } else {
                    Spacer(modifier = Modifier.size(10.dp))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp, bottom = 10.dp, start = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(30.dp))
            secondRow.forEach {
                if (it != null) {
                    IconButton(
                        onClick = {
                            val emptyDotIndex = pinState.indexOfFirst { !it }
                            if (emptyDotIndex != -1) {
                                pinState[emptyDotIndex] = true
                            }
                            if(reEnteredPassCode.length < 6) reEnteredPassCode += it
                        },
                    ) {
                        Text(
                            text = it.toString(),
                            color = Color.Black,
                            fontFamily = barlowSemiCondensed_semi_bold,
                            fontSize = 32.sp
                        )
                    }
                    Spacer(modifier = Modifier.size(40.dp))
                } else {
                    Spacer(modifier = Modifier.size(10.dp))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp, bottom = 10.dp, start = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(30.dp))
            thirdRow.forEach {
                if (it != null) {
                    IconButton(
                        onClick = {
                            val emptyDotIndex = pinState.indexOfFirst { !it }
                            if (emptyDotIndex != -1) {
                                pinState[emptyDotIndex] = true
                            }
                            if(reEnteredPassCode.length < 6) reEnteredPassCode += it
                        },
                    ) {
                        Text(
                            text = it.toString(),
                            color = Color.Black,
                            fontFamily = barlowSemiCondensed_semi_bold,
                            fontSize = 32.sp
                        )
                    }
                    Spacer(modifier = Modifier.size(40.dp))
                } else {
                    Spacer(modifier = Modifier.size(10.dp))
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp, bottom = 8.dp, start = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(65.dp))
            fourthRow.forEach {
                if (it != null) {
                    IconButton(
                        onClick = {
                            val emptyDotIndex = pinState.indexOfFirst { !it }
                            if (emptyDotIndex != -1) {
                                pinState[emptyDotIndex] = true
                            }
                            if(reEnteredPassCode.length < 6) reEnteredPassCode += it
                        },
                    ) {
                        Text(
                            text = it.toString(),
                            color = Color.Black,
                            fontFamily = barlowSemiCondensed_semi_bold,
                            fontSize = 32.sp
                        )
                    }
                    Spacer(modifier = Modifier.size(30.dp))
                } else {
                    Spacer(modifier = Modifier.size(10.dp))
                }
            }
            IconButton(
                onClick = {
                    val filledDotIndex = pinState.indexOfLast { it }
                    if (filledDotIndex != -1) {
                        pinState[filledDotIndex] = false
                    }
                    reEnteredPassCode = reEnteredPassCode.dropLast(1)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = ""
                )
            }
        }
    }


}
