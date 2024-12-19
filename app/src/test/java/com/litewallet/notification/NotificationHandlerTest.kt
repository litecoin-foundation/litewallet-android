package com.litewallet.notification

import android.content.Context
import android.os.Bundle
import androidx.collection.arrayMapOf
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.Constants.MessageNotificationKeys
import com.google.firebase.messaging.Constants.MessagePayloadKeys
import com.google.firebase.messaging.RemoteMessage
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NotificationHandlerTest {

    private val context: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(MessagePayloadKeys::class)
    }

    @Test
    fun `invoke handleMessageReceived with data not contains key litewallet, then should return false`() {

        val remoteMessage = RemoteMessage(Bundle())

        every { MessagePayloadKeys.extractDeveloperDefinedPayload(any()) } returns arrayMapOf()

        val actual = NotificationHandler.handleMessageReceived(context, remoteMessage)

        assertEquals(false, actual)
    }

    @Test
    fun `invoke handleMessageReceived with valid data & notification, then should return true`() {

        val mapData = mapOf(
            MessageNotificationKeys.TITLE to "Hello There!",
            MessageNotificationKeys.BODY to "This is Body!",
            MessageNotificationKeys.CHANNEL to "general",
            MessageNotificationKeys.ENABLE_NOTIFICATION to "1",
            NotificationHandler.KEY_DATA_LITEWALLET to "true",
            "title" to "Hello There!",
            "body" to "This is Body!"
        )
        val remoteMessage = RemoteMessage.Builder("")
            .setData(mapData)
            .build()

        every { MessagePayloadKeys.extractDeveloperDefinedPayload(any()) } returns arrayMapOf(
            MessageNotificationKeys.TITLE to "Hello There!",
            MessageNotificationKeys.BODY to "This is Body!",
            MessageNotificationKeys.CHANNEL to "general",
            MessageNotificationKeys.ENABLE_NOTIFICATION to "1",
            NotificationHandler.KEY_DATA_LITEWALLET to "true",
            "title" to "Hello There!",
            "body" to "This is Body!"
        )

        mockkStatic(NotificationManagerCompat::class)
        every { NotificationManagerCompat.from(context).notify(any()) } just Runs

        //todo: revisit, test still fail because specific platform API e.g. android.app.Notification$Builder
        val actual = NotificationHandler.handleMessageReceived(context, remoteMessage)

        assertEquals(true, actual)
    }

}