package com.litewallet.tools.util
import io.mockk.every
import io.mockk.Runs
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File


class GoogleServicesTest {

    private lateinit var googleServicesJSONFile: File

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Before
    fun setUp() {
        // Create a temporary file with specific contents
        googleServicesJSONFile = temporaryFolder.newFile("google-services.json")
        googleServicesJSONFile.writeText("\"{\\n  \\\"project_info\\\": {\\n    \\\"project_number\\\": \\\"230187998656\\\",\\n    \\\"project_id\\\": \\\"litewallet-beta\\\"\\n  }\\n}\"")
    }

    @Test
    fun `read GoogleServices file`() {

    }
    @Test
    fun `read GoogleServices file and parse into a JSON object , should return the correct project_id`() {

        //        val projectInfo = json.getAsJsonObject("project_info")
        //        assertEquals("litewallet-beta", projectInfo.get("project_id").asString)
    }

    @Test
    fun `read GoogleServices file and parse into a JSON object , should return the correct project_number`() {
        //        val projectInfo = json.getAsJsonObject("project_info")
        //        assertEquals("230187998656", projectInfo.get("project_number").asString)
    }
}