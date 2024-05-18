package com.example.trypasscodeandbiometrics.screens

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.breadwallet.R
import com.breadwallet.presenter.activities.intro.SetSecurityActivity
import com.breadwallet.tools.viewmodel.SecurityViewModel
import com.breadwallet.ui.theme.barlowSemiCondensed_bold
import com.breadwallet.ui.theme.barlowSemiCondensed_semi_bold


@Composable
fun SetPassCode(modifier: Modifier = Modifier, navController: NavController, passCodeViewModel: SecurityViewModel, onBackPress: () -> Unit) {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
        ?.onBackPressedDispatcher

    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Call the provided lambda to handle back press
                onBackPress()
            }
        }
    }

    DisposableEffect(key1 = onBackPressedDispatcher) {
        onBackPressedDispatcher?.addCallback(backCallback)
        onDispose {
            backCallback.remove()
        }
    }

    val context = LocalContext.current
    IconButton(
        modifier =  modifier
            .padding(top=20.dp, start = 20.dp, end = 20.dp)
        ,
        onClick = {
            val intent = Intent(context, SetSecurityActivity::class.java)
            context.startActivity(intent)
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
        Spacer(modifier = modifier.weight(2f))
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
            text = "Create",
            fontFamily = barlowSemiCondensed_bold,
            fontSize = 36.sp,
        )
        Text(
            modifier = Modifier
                .width(210.dp)
                .padding(top = 20.dp, start = 8.dp),
            text = "Pick a 6-digit passcode to unlock your Litewallet",
            fontFamily = barlowSemiCondensed_semi_bold,
            fontSize = 18.sp,
            lineHeight = 28.sp
        )
        Spacer(modifier = modifier.weight(0.3f))
        Passcode(navController=navController, passCodeViewModel = passCodeViewModel)
        Spacer(modifier = modifier.weight(1/2f))
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun Passcode(modifier: Modifier = Modifier, navController: NavController, passCodeViewModel: SecurityViewModel) {
    val pinState = remember { mutableStateListOf<Boolean>(false, false, false, false, false, false) }
    var enteredPassCode by remember { mutableStateOf("") }
    if(enteredPassCode.length == 6){
        passCodeViewModel.saveBooleanData("is_authenticated_with_passcode", true)
        passCodeViewModel.saveData("pass_code", enteredPassCode)
        navController.navigate("reenter_pass_code")
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
        val rows = listOf("0","1","2","3","4","5","6","7","8","9").shuffled()
        val firstRow = listOf(rows[0], rows[1], rows[2])
        val secondRow = listOf(rows[3], rows[4], rows[5])
        val thirdRow = listOf(rows[6], rows[7], rows[8])
        val fourthRow = listOf(null, rows[9], null)
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
//                            viewModel.putPassCode(viewModel.retrievePassCode() + it)
                            enteredPassCode = enteredPassCode.plus(it)
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
//                            viewModel.putPassCode(viewModel.retrievePassCode() + it)
//                            enteredPassCode = viewModel.retrievePassCode()
                            enteredPassCode = enteredPassCode.plus(it)
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
//                            viewModel.putPassCode(viewModel.retrievePassCode() + it)
//                            enteredPassCode = viewModel.retrievePassCode()
                            enteredPassCode = enteredPassCode.plus(it)
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
//                            viewModel.putPassCode(viewModel.retrievePassCode() + it)
//                            enteredPassCode = viewModel.retrievePassCode()
                            enteredPassCode = enteredPassCode.plus(it)
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
//                    viewModel.putPassCode(viewModel.retrievePassCode().dropLast(1))
//                    enteredPassCode = viewModel.retrievePassCode()
                    enteredPassCode = enteredPassCode.dropLast(1)
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
