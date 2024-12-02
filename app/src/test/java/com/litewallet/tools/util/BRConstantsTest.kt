package com.litewallet.tools.util

import com.breadwallet.tools.util.BRConstants
import org.junit.Assert
import org.junit.Assert.assertSame
import org.junit.Test

//TODO: migrate from [com.litewallet.analytics.ConstantsTests]
class BRConstantsTest {

    @Test
    fun `validate Litecoin symbol constant`() {
        assertSame(BRConstants.litecoinLowercase,"ł")
        assertSame(BRConstants.litecoinUppercase,"Ł")
    }
}