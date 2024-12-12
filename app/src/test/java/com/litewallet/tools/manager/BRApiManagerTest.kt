package com.litewallet.tools.manager

import android.app.Activity
import android.content.Context
import com.breadwallet.presenter.activities.util.ActivityUTILS
import com.breadwallet.tools.manager.BRApiManager
import com.breadwallet.tools.util.BRConstants
import com.breadwallet.tools.util.Utils
import com.litewallet.data.source.RemoteConfigSource
import com.platform.APIClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verifyAll
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BRApiManagerTest {

    private val remoteConfigSource: RemoteConfigSource = mockk()
    private lateinit var apiManager: BRApiManager

    @Before
    fun setUp() {
        apiManager = spyk(BRApiManager(remoteConfigSource), recordPrivateCalls = true)
    }

    @Test
    fun `invoke fetchRates, should return success with parsed JSONArray`() {
        val activity: Activity = mockk(relaxed = true)
        val responseString = """
            [
                {
                    "code": "AED",
                    "n": 416.81128312406213,
                    "price": "AED416.811283124062145364",
                    "name": "United Arab Emirates Dirham"
                },
                {
                    "code": "AFN",
                    "n": 7841.21263788453,
                    "price": "Af7841.212637884529266812",
                    "name": "Afghan Afghani"
                },
                {
                    "code": "ALL",
                    "n": 10592.359754930994,
                    "price": "ALL10592.359754930995026136",
                    "name": "Albanian Lek"
                }
            ]
        """.trimIndent()
        mockkStatic(ActivityUTILS::class)
        mockkObject(APIClient.getInstance(activity))
        every {
            remoteConfigSource.getBoolean(RemoteConfigSource.KEY_API_BASEURL_PROD_NEW_ENABLED)
        } returns false
        every {
            apiManager invoke "createGETRequestURL" withArguments (listOf(
                activity as Context,
                BRConstants.LW_API_HOST
            ))
        } returns responseString
        every { ActivityUTILS.isMainThread() } returns false
        every { APIClient.getInstance(activity).getCurrentLocale(activity) } returns "en"

        val request = Request.Builder()
            .url(BRConstants.LW_API_HOST)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("User-agent", Utils.getAgentString(activity, "android/HttpURLConnection"))
            .get().build()
        every {
            APIClient.getInstance(activity).sendRequest(request, false, 0)
        } returns Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseString.toResponseBody())
            .build()

        val result = apiManager.fetchRates(activity)

        verifyAll {
            remoteConfigSource.getBoolean(RemoteConfigSource.KEY_API_BASEURL_PROD_NEW_ENABLED)

            ActivityUTILS.isMainThread()
            APIClient.getInstance(activity).getCurrentLocale(activity)
            APIClient.getInstance(activity).sendRequest(any(), any(), any())
        }

        val jsonAED = result.getJSONObject(0)
        assertEquals("AED", jsonAED.optString("code"))
        assertEquals("United Arab Emirates Dirham", jsonAED.optString("name"))
    }

    @Test
    fun `invoke getBaseUrlProd with KEY_API_BASEURL_PROD_NEW_ENABLED true, then should return new baseUrlProd`() {
        every { remoteConfigSource.getBoolean(RemoteConfigSource.KEY_API_BASEURL_PROD_NEW_ENABLED) } returns true

        val actual = apiManager.baseUrlProd

        assertEquals(BRConstants.LW_API_HOST_NEW, actual)
    }

    @Test
    fun `invoke getBaseUrlProd with KEY_API_BASEURL_PROD_NEW_ENABLED false, then should return old baseUrlProd`() {
        every { remoteConfigSource.getBoolean(RemoteConfigSource.KEY_API_BASEURL_PROD_NEW_ENABLED) } returns false

        val actual = apiManager.baseUrlProd

        assertEquals(BRConstants.LW_API_HOST, actual)
    }
}