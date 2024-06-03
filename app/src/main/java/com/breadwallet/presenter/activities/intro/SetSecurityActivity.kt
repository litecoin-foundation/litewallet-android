package com.breadwallet.presenter.activities.intro

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModelProvider
import com.breadwallet.R
import com.breadwallet.entities.IntroLanguage
import com.breadwallet.entities.IntroLanguageResource
import com.breadwallet.presenter.activities.AuthPassCodeActivity
import com.breadwallet.presenter.activities.AuthPassCodeAndBiometricsActivity
import com.breadwallet.tools.util.AppTheme
import com.breadwallet.tools.util.LocaleHelper
import com.breadwallet.tools.util.LocaleHelper.Companion.instance
import com.breadwallet.tools.util.ThemeSetting
import com.breadwallet.tools.viewmodel.SecurityViewModel
import com.breadwallet.ui.theme.LitewalletAndroidTheme
import com.breadwallet.ui.theme.barlowSemiCondensed_light
import com.breadwallet.ui.theme.barlowSemiCondensed_normal
import com.breadwallet.ui.theme.barlowSemiCondensed_semi_bold
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetSecurityActivity : ComponentActivity() {
    @Inject
    lateinit var themeSetting: ThemeSetting
    private lateinit var viewModel: SecurityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SecurityViewModel::class.java]
        setContent {
            val theme = themeSetting.themeStream.collectAsState()
            val useDarkColors = when (theme.value) {
                AppTheme.MODE_AUTO -> isSystemInDarkTheme()
                AppTheme.MODE_DAY -> false
                AppTheme.MODE_NIGHT -> true
            }
            LitewalletAndroidTheme(
                darkTheme = useDarkColors
            ){
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val audio = MediaPlayer()
                    SetSecurity(viewModel = viewModel, theme =  theme, themeSetting = themeSetting, audio = audio)
                }
            }
        }
    }
}

@Composable
fun SetSecurity(modifier: Modifier = Modifier,
                viewModel: SecurityViewModel,
                theme: State<AppTheme>,
                themeSetting: ThemeSetting,
                audio: MediaPlayer
){
    val lazyListState = rememberLazyListState()
    val arrayLanguages = IntroLanguageResource().loadResources()
    Column (modifier= modifier
        .fillMaxWidth()
        .padding(0.dp, 60.dp, 0.dp, 0.dp)
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Image(
            painter = if(theme.value == AppTheme.MODE_DAY) painterResource(id = R.drawable.litewallet_black_logo) else painterResource(
                id = R.drawable.litewallet_logotype_white
            ),
            contentDescription = stringResource(id = R.string.litewallet_logo),
            modifier = Modifier
                .width(300.dp)
                .height(200.dp)
                .padding(0.dp, 0.dp, 0.dp, 80.dp)
        )
        // Scroll Languages
        LanguagePicker(languagesArray = arrayLanguages, lazyListState = lazyListState, theme = theme, audio = audio)
        // Texts Information
        TextInformation()
        // Button dark mode
        Spacer(modifier = Modifier.height(10.dp))
        IconButton(
            modifier = Modifier
                .size(50.dp),
            onClick = {
                if (theme.value == AppTheme.MODE_DAY) {
                    themeSetting.theme = AppTheme.fromOrdinal(AppTheme.MODE_NIGHT.ordinal)
                } else if (theme.value == AppTheme.MODE_NIGHT) {
                    themeSetting.theme = AppTheme.fromOrdinal(AppTheme.MODE_DAY.ordinal)
                }
            },
        ) {
            Image(
                painter = if (theme.value == AppTheme.MODE_DAY) painterResource(id = R.drawable.dark_mode_moon)
                else painterResource(id = R.drawable.light_mode_moon),
                contentScale = ContentScale.Inside,
                contentDescription = stringResource(id = R.string.dark_mode),
            )
        }
        Spacer(modifier = Modifier.weight(0.5f))
        // Button biometrics and passcode
        ButtonBiometrics(theme = theme)
        Text(
            modifier = Modifier.padding(top=10.dp, bottom = 10.dp),
            fontSize = 12.sp,
            fontFamily = barlowSemiCondensed_normal,
            text = "v1.19.0"
        )

    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun LanguagePicker(modifier: Modifier = Modifier,
              languagesArray: Array<IntroLanguage>,
              lazyListState: LazyListState,
              audio: MediaPlayer,
              theme: State<AppTheme>
){
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var showPopup by remember { mutableStateOf(false) }

    val centeredItemIndex = derivedStateOf {
        val layoutInfo = lazyListState.layoutInfo
        val viewportCenter = lazyListState.layoutInfo.viewportStartOffset + (lazyListState.layoutInfo.viewportSize.height / 2)
        layoutInfo.visibleItemsInfo.firstOrNull { itemInfo ->
            itemInfo.offset <= viewportCenter && itemInfo.offset + itemInfo.size > viewportCenter
        }?.index ?: -1
    }.value

    val context = LocalContext.current
    LaunchedEffect(centeredItemIndex) {
        showPopup = true
        selectedIndex = centeredItemIndex
    }
    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .width(160.dp)
            .height(42.dp)
            .background(
                color = colorResource(id = R.color.white_two),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(0.dp, 4.dp, 0.dp, 4.dp)
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(languagesArray.size){
            val paddingBottom = if (it == languagesArray.size-1) 8 else 0
            val paddingTop = if (it == 0) 2 else 4
            val isSelected = it == centeredItemIndex
            Text(
                languagesArray[it].name,
                modifier = Modifier
                    .padding(0.dp, paddingTop.dp, 0.dp, paddingBottom.dp)
                    .selectable(
                        selected = isSelected,
                        onClick = {}
                    )
                ,
                fontFamily = barlowSemiCondensed_light,
                fontSize = 18.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Light,
                color = Color.Black
            )
        }
    }

    if (showPopup) {
        if (audio.isPlaying) {
            audio.stop()
            audio.reset()
        }
        audio.reset()
        audio.setDataSource(context, Uri.parse("android.resource://" + context.packageName + "/" + languagesArray[centeredItemIndex].audio))
        audio.prepare()
        audio.start()
        AlertDialog(
            onDismissRequest = { showPopup = false },
            title = { Text("Selected Language") },
            text = { Text(languagesArray[centeredItemIndex].name) },
            confirmButton = {
                Button(onClick = {
                    showPopup = false

                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showPopup = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            if (audio.isPlaying) {
                audio.stop()
            }
            audio.release()
        }
    }
}

@Composable
fun TextInformation(modifier: Modifier = Modifier){
    Column(
        modifier = modifier
            .padding(0.dp, 60.dp, 0.dp, 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.set_access),
            modifier = Modifier
                .padding(12.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 28.sp,
        )
        Text(
            text = stringResource(id = R.string.intro_description),
            fontFamily = barlowSemiCondensed_semi_bold,
            fontSize = 16.sp,
            modifier = Modifier
                .width(220.dp)
        )
    }
}

@Composable
fun ButtonBiometrics(
    modifier: Modifier = Modifier,
    theme: State<AppTheme>
) {
    val context = LocalContext.current
    Button(
        modifier = Modifier
            .width(300.dp)
            .height(45.dp)
        ,
        shape = RectangleShape,
        colors = if(theme.value == AppTheme.MODE_DAY) ButtonDefaults.buttonColors(Color.White, Color.Black) else ButtonDefaults.buttonColors(Color.Black, Color.White),
        border = BorderStroke(1.dp, if(theme.value == AppTheme.MODE_DAY) Color.Black else Color.White),
        onClick = {
            val intent = Intent(context, AuthPassCodeAndBiometricsActivity::class.java).apply {
                putExtra("category", "0")
            }
            context.startActivity(intent)
        }
    ) {
        Text(
            fontFamily = barlowSemiCondensed_light,
            text= stringResource(id = R.string.biometrics_and_passcode)
        )
    }
    Spacer(modifier = Modifier.padding(10.dp))
    Button(
        modifier = Modifier
            .width(300.dp)
            .height(45.dp)
        ,
        shape = RectangleShape,
        colors = if(theme.value == AppTheme.MODE_DAY) ButtonDefaults.buttonColors(Color.White, Color.Black) else ButtonDefaults.buttonColors(Color.Black, Color.White),
        border = BorderStroke(1.dp, if(theme.value == AppTheme.MODE_DAY) Color.Black else Color.White),
        onClick = {
            val intent = Intent(context, AuthPassCodeActivity::class.java).apply {
                putExtra("category", "1")
            }
            context.startActivity(intent)
        }
    ) {
        Text(
            fontFamily = barlowSemiCondensed_light,
            text= stringResource(id = R.string.passcode_only)
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun SetSecurityPreview() {
    LitewalletAndroidTheme {
//        SetSecurity(viewModel = vie)
    }
}