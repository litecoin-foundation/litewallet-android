package com.breadwallet.entities

import android.content.res.Configuration
import android.content.res.Resources
import com.breadwallet.R
import java.util.Locale

class AudioSource() {
    fun getLocalizedText(locale: Locale, resourceId: Int): String {
        val config = Configuration()
        config.setLocale(locale)
        return Resources.getSystem().getString(resourceId)
    }
    fun loadAudios(): List<CountryAudio>{
        return listOf<CountryAudio>(
            // R.string.StartViewController_message
            CountryAudio(getLocalizedText(Locale.ENGLISH, R.string.StartViewController_message), getLocalizedText(Locale.ENGLISH, R.string.Settings_ChangeLanguage_alertMessage), R.raw.english),
            CountryAudio(getLocalizedText(Locale.GERMAN, R.string.StartViewController_message), getLocalizedText(Locale.GERMAN, R.string.Settings_ChangeLanguage_alertMessage), R.raw.deutsch),
            CountryAudio(getLocalizedText(Locale.FRENCH, R.string.StartViewController_message), getLocalizedText(Locale.FRENCH, R.string.Settings_ChangeLanguage_alertMessage), R.raw.french),
            CountryAudio(getLocalizedText(Locale("IN"), R.string.StartViewController_message), getLocalizedText(Locale("IN"), R.string.Settings_ChangeLanguage_alertMessage), R.raw.bahasaindonesia),
            CountryAudio(getLocalizedText(Locale.ITALIAN, R.string.StartViewController_message), getLocalizedText(Locale.ITALIAN, R.string.Settings_ChangeLanguage_alertMessage), R.raw.italiano),
            CountryAudio(getLocalizedText(Locale.JAPANESE, R.string.StartViewController_message), getLocalizedText(Locale.JAPANESE, R.string.Settings_ChangeLanguage_alertMessage), R.raw.japanese),
            CountryAudio(getLocalizedText(Locale.KOREAN, R.string.StartViewController_message), getLocalizedText(Locale.KOREAN, R.string.Settings_ChangeLanguage_alertMessage), R.raw.korean),
            CountryAudio(getLocalizedText(Locale("PT"), R.string.StartViewController_message), getLocalizedText(Locale("PT"), R.string.Settings_ChangeLanguage_alertMessage), R.raw.portugues),
            CountryAudio(getLocalizedText(Locale("RU"), R.string.StartViewController_message), getLocalizedText(Locale("RU"), R.string.Settings_ChangeLanguage_alertMessage), R.raw.russian),
            CountryAudio(getLocalizedText(Locale("TR"), R.string.StartViewController_message), getLocalizedText(Locale("TR"), R.string.Settings_ChangeLanguage_alertMessage), R.raw.turkish),
            CountryAudio(getLocalizedText(Locale("UK"), R.string.StartViewController_message), getLocalizedText(Locale("UK"), R.string.Settings_ChangeLanguage_alertMessage), R.raw.ukrainian),
            CountryAudio(getLocalizedText(Locale("ZH-TW"), R.string.StartViewController_message), getLocalizedText(Locale("ZH-TW"), R.string.Settings_ChangeLanguage_alertMessage), R.raw.traditionalchinese)
        )
    }
}