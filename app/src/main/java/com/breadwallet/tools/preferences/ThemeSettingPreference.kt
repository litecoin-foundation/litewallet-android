package com.breadwallet.tools.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.breadwallet.tools.util.AppTheme
import com.breadwallet.tools.util.ThemeSetting
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ThemeSettingPreference @Inject constructor(
    @ApplicationContext context: Context
): ThemeSetting {

    override val themeStream: MutableStateFlow<AppTheme>
    override var theme: AppTheme by AppThemePreferenceDelegate("app_theme", AppTheme.MODE_DAY)

    private val preferences: SharedPreferences = context.getSharedPreferences("theme_mode", Context.MODE_PRIVATE)

    init {
        themeStream = MutableStateFlow(theme)
    }

    inner class AppThemePreferenceDelegate(
        private val name: String,
        private val default: AppTheme
    ): ReadWriteProperty<Any?, AppTheme> {
        override operator fun getValue(thisRef: Any?, property: KProperty<*>): AppTheme =
            AppTheme.fromOrdinal(preferences.getInt(name, default.ordinal))

        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: AppTheme) {
            themeStream.value = value
            preferences.edit {
                putInt(name, value.ordinal)
            }
        }

    }
}