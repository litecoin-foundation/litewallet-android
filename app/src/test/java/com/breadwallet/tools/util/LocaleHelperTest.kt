package com.breadwallet.tools.util

import android.content.Context
import com.breadwallet.entities.Language
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
}