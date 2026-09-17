package com.bakeerp.app.work

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

/**
 * 通知工具：集中管理通道 ID 与消息构造。
 *
 * 通道 ID 与 AndroidManifest 引用一致；通知通道注册在 Application.onCreate 完成。
 */
object NotificationHelper {

    const val CHANNEL_EXPIRY = "channel_expiry"
    const val CHANNEL_INVENTORY = "channel_inventory"

    const val NOTIFICATION_ID_EXPIRY = 1001
    const val NOTIFICATION_ID_INVENTORY = 1002

    fun notifyExpiry(context: Context, title: String, content: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_EXPIRY)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID_EXPIRY, builder.build())
    }

    fun notifyInventory(context: Context, title: String, content: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_INVENTORY)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID_INVENTORY, builder.build())
    }
}