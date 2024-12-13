package com.litewallet.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import com.breadwallet.R
import com.litewallet.notification.NotificationHandler.Companion.NOTIFICATION_CHANNEL_ID_GENERAL
import com.litewallet.notification.NotificationHandler.Companion.NOTIFICATION_CHANNEL_ID_LITECOIN_NEWS
import com.litewallet.notification.NotificationHandler.Companion.NOTIFICATION_CHANNEL_ID_LITEWALLET_UPDATE

interface NotificationHandler {

    //todo

    companion object {
        const val NOTIFICATION_CHANNEL_ID_GENERAL = "general"
        const val NOTIFICATION_CHANNEL_ID_LITECOIN_NEWS = "litecoin-news"
        const val NOTIFICATION_CHANNEL_ID_LITEWALLET_UPDATE = "litewallet-update"
    }
}

fun setupNotificationChannels(context: Context) {
    createNotificationChannel(
        context,
        NOTIFICATION_CHANNEL_ID_GENERAL,
        context.getString(R.string.notification_channel_name_general)
    )
    createNotificationChannel(
        context,
        NOTIFICATION_CHANNEL_ID_LITECOIN_NEWS,
        context.getString(R.string.notification_channel_name_litecoin_news)
    )
    createNotificationChannel(
        context,
        NOTIFICATION_CHANNEL_ID_LITEWALLET_UPDATE,
        context.getString(R.string.notification_channel_name_litewallet_update)
    )
}

private fun createNotificationChannel(
    context: Context,
    channelId: String,
    name: String,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel(
            channelId,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        ).also { notificationChannel ->
            val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}