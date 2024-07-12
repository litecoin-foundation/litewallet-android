package com.breadwallet.presenter.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.breadwallet.R
import com.breadwallet.presenter.activities.intro.Greeting
import com.breadwallet.presenter.activities.intro.SetSecurity
import com.breadwallet.ui.theme.LitewalletAndroidTheme
import com.breadwallet.ui.theme.barlowSemiCondensed_bold
import com.breadwallet.ui.theme.barlowSemiCondensed_semi_bold

class ReEnterPinActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LitewalletAndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ReEnterPin()
                }
            }
        }
    }
}

@Composable
fun ReEnterPin(modifier: Modifier = Modifier) {
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
        PasscodeReEnter()
        Spacer(modifier = Modifier
            .weight(1/2f)
        )
    }
}


@Composable
fun PasscodeReEnter(modifier: Modifier = Modifier) {
    val pinState = remember { mutableStateListOf<Boolean>(false, false, false, false, false, false) }
    val textState : MutableList<Int> = remember { mutableStateListOf() }
    // Function to add an item (limit to 6)
    fun addItem(item: Int) {
        if (textState.size < 6) {
            textState.add(item)
        }
    }

    fun deleteItem(item: Int){
        if(textState.size > 0){
            textState.removeAt(item)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // For debugging
        // Text(text = "${textState.toList()}")
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
                            addItem(it)
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
                            addItem(it)
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
                            addItem(it)
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
                            addItem(it)
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
                    deleteItem(filledDotIndex)
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

@Preview(showBackground = true)
@Composable
fun ReEnterPinActivityPreview() {
    LitewalletAndroidTheme {
        ReEnterPin()
    }
}