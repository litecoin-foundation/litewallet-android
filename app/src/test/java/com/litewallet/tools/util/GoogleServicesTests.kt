package com.litewallet.tools.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.io.FileReader
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

import android.content.Context
import com.breadwallet.entities.Language
import com.breadwallet.tools.util.LocaleHelper
import io.grpc.internal.JsonParser
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



class GoogleServicesTest {
    @Test
    fun testGoogleServicesJson() {
        // Load the app/google-services.json file
        val file = File("app/google-services.json")
        val json = JsonParser().parse(FileReader(file)).asJsonObject

        // Test the project_info property
        val projectInfo = json.getAsJsonObject("project_info")
        assertEquals("litewallet-beta", projectInfo.get("project_id").asString)
        assertEquals("230187998656", projectInfo.get("project_number").asString)
    }
}