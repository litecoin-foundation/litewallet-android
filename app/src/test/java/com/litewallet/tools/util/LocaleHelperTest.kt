package com.litewallet.tools.util

import android.content.Context
import com.breadwallet.entities.Language
import com.breadwallet.presenter.language.LanguageAdapter
import com.breadwallet.tools.util.LocaleHelper
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class LocaleHelperTest {

    @Test
    fun `given LocaleHelper instance, then should validate default value`() {
        val context: Context = mockk(relaxed = true)

        LocaleHelper.init(context)

        val currentLocale = LocaleHelper.instance.currentLocale

        assertTrue(currentLocale == Language.ENGLISH)
        assertEquals("en", currentLocale.code)
        assertEquals("English", currentLocale.title)
        assertEquals("Select language", currentLocale.desc)
    }

    @Test
    fun `getLocale invoked, should return Locale object`() {
        val localeIndonesian = LocaleHelper.getLocale(Language.INDONESIAN)

        assertEquals("id", localeIndonesian.language)

        val localeChineseSimplified = LocaleHelper.getLocale(Language.CHINESE_SIMPLIFIED)

        assertEquals("zh", localeChineseSimplified.language)
        assertEquals("CN", localeChineseSimplified.country)
    }

    @Test
    fun `setLocaleIfNeeded invoked, should update current locale`() {
        val context: Context = mockk(relaxed = true)
        LocaleHelper.init(context)

        val currentLocale = LocaleHelper.instance.currentLocale
        assertTrue(currentLocale == Language.ENGLISH)

        var changed = LocaleHelper.instance.setLocaleIfNeeded(Language.INDONESIAN)
        assertTrue(changed)

        changed = LocaleHelper.instance.setLocaleIfNeeded(Language.INDONESIAN)
        assertFalse(changed)
    }

    @Test
    fun `check all language codes`() {
        Language.entries
            .also { languages ->
                assertEquals(15, languages.size)
            }
            .map { it.code }
            .also { langCodes ->
                assertEquals(
                    listOf(
                        "en",
                        "de",
                        "es",
                        "fr",
                        "in",
                        "it",
                        "pt",
                        "tr",
                        "uk",
                        "ru",
                        "ko",
                        "ja",
                        "zh-CN",
                        "zh-TW",
                        "ar"
                    ),
                    langCodes
                )
            }


    }
}