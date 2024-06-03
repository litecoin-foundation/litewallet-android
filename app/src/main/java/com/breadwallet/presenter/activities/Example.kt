package com.breadwallet.presenter.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.breadwallet.tools.util.AppTheme
import com.breadwallet.tools.util.ThemeSetting
import com.breadwallet.ui.theme.LitewalletAndroidTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Example : AppCompatActivity() {
    @Inject
    lateinit var themeSetting: ThemeSetting
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val theme = themeSetting.themeStream.collectAsState()
            val useDarkColors = when (theme.value) {
                AppTheme.MODE_AUTO -> isSystemInDarkTheme()
                AppTheme.MODE_DAY -> false
                AppTheme.MODE_NIGHT -> true
            }
            LitewalletAndroidTheme(
                darkTheme = useDarkColors
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(50.dp)
                        ,
                        text = "Welcome!",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}