package com.litewallet.tools.util

import com.breadwallet.tools.util.BRConstants
import org.junit.Assert.assertSame
import org.junit.Test

class BRConstantsTest {

    @Test
    fun `validate Litecoin symbol constant`() {
        assertSame(BRConstants.litecoinLowercase, "ł")
        assertSame(BRConstants.litecoinUppercase, "Ł")
    }

    @Test
    fun `validate App external URL constant`() {
        assertSame(BRConstants.TWITTER_LINK, "https://twitter.com/Litewallet_App")
        assertSame(BRConstants.INSTAGRAM_LINK, "https://www.instagram.com/litewallet.app")
        assertSame(BRConstants.WEB_LINK, "https://litewallet.io")
        assertSame(BRConstants.TOS_LINK, "https://litewallet.io/privacy")
        assertSame(BRConstants.BITREFILL_AFFILIATE_LINK, "https://www.bitrefill.com/")
        assertSame(
                BRConstants.CUSTOMER_SUPPORT_LINK,
                "https://support.litewallet.io/hc/en-us/requests/new"
        )
    }
}
