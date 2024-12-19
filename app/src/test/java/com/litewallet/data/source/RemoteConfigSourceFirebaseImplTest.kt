package com.litewallet.data.source

import com.google.android.gms.tasks.Tasks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RemoteConfigSourceFirebaseImplTest {

    private val firebaseRemoteConfig: FirebaseRemoteConfig = mockk(relaxed = true)
    private lateinit var remoteConfigSource: RemoteConfigSource

    @Before
    fun setUp() {
        remoteConfigSource = RemoteConfigSource.FirebaseImpl(firebaseRemoteConfig)
    }

    @Test
    fun `call initialize, then should return success`() {
        every { firebaseRemoteConfig.fetchAndActivate() } answers { Tasks.forResult(true) }

        every { firebaseRemoteConfig.addOnConfigUpdateListener(any()) } returns mockk(relaxed = true)

        remoteConfigSource.initialize()

        verify { firebaseRemoteConfig.fetchAndActivate() }
        verify { firebaseRemoteConfig.addOnConfigUpdateListener(any()) }
    }

    @Test
    fun `call getString, then should return with expected config value`() {

        every { firebaseRemoteConfig.getString(any()) } returns """
            {"enabled":false,"title":"litewallet-android repository","url":"https://github.com/litecoin-foundation/litewallet-android"}
        """.trimIndent()
        val actual =
            remoteConfigSource.getString(RemoteConfigSource.KEY_FEATURE_MENU_HIDDEN_EXAMPLE)
        runCatching { JSONObject(actual) }
            .onSuccess { configValue ->
                assertEquals(false, configValue.optBoolean("enabled"))
                assertEquals("litewallet-android repository", configValue.optString("title"))
                assertEquals(
                    "https://github.com/litecoin-foundation/litewallet-android",
                    configValue.optString("url")
                )
            }
    }

    @Test
    fun `call getBoolean, then should return with expected config value`() {
        every { firebaseRemoteConfig.getBoolean(any()) } returns true

        val actual = remoteConfigSource.getBoolean("TEST_KEY")

        assertEquals(true, actual)
    }

    @Test
    fun `call getNumber, then should return with expected config value`() {
        every { firebaseRemoteConfig.getDouble(any()) } returns 100.0

        val actual = remoteConfigSource.getNumber("TEST_KEY")

        assertEquals(100.0, actual, .1)
    }
}