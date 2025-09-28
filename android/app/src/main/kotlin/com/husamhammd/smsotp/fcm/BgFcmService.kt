package com.husamhammd.smsotp.fcm

import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.graphics.Color

class BgFcmService : FirebaseMessagingService() {


        private fun ensureChannel() {
            val channelId = "sms_channel"
            val nm = getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(channelId, "SMS Jobs", NotificationManager.IMPORTANCE_HIGH)
            ch.description = "Notifications for incoming SMS jobs"
            ch.enableLights(true); ch.lightColor = Color.BLUE
            nm.createNotificationChannel(ch)
        }

        private fun showLocal(title: String, body: String) {
    val channelId = "sms_channel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val nm = getSystemService(NotificationManager::class.java)
        val ch = NotificationChannel(
            channelId,
            "SMS Jobs",
            NotificationManager.IMPORTANCE_HIGH
        )
        ch.description = "Notifications for incoming SMS jobs"
        nm.createNotificationChannel(ch)
    }

 
    val smallIcon = android.R.drawable.stat_notify_chat

    val builder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(smallIcon)
        .setContentTitle(title)
        .setContentText(body)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    NotificationManagerCompat.from(this)
        .notify((System.currentTimeMillis() / 1000).toInt(), builder.build())
}
    override fun onMessageReceived(message: RemoteMessage) {
        val phone = message.data["phone"]?.trim().orEmpty()
        val text  = message.data["text"]?.trim().orEmpty()

        if (!isValidE164(phone) || text.isBlank()) {
            Log.w(TAG, "Invalid payload: ${message.data}")
            return
        }

        try {
            val smsManager = getDefaultSmsManager()
            smsManager.sendTextMessage(phone, null, text, null, null)
            Log.i(TAG, "SMS requested to $phone")

            val title = message.data["title"] ?: "SMS Job"
            val body  = message.data["body"] ?: message.data["text"] ?: "وُصلت مهمة جديدة"
            showLocal(title, body)
        } catch (se: SecurityException) {
            Log.e(TAG, "SEND_SMS permission not granted", se)
        } catch (e: Exception) {
            Log.e(TAG, "SMS send error: ${e.message}", e)
        }
    }

    override fun onNewToken(token: String) {
        Log.i(TAG, "New FCM token: $token")
        // (اختياري) أرسله لسيرفرك
    }

    private fun getDefaultSmsManager(): SmsManager {
        return try {
            val subId = SubscriptionManager.getDefaultSmsSubscriptionId()
            if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                SmsManager.getSmsManagerForSubscriptionId(subId)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
            }
        } catch (_: Throwable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
        }
    }

    private fun isValidE164(s: String): Boolean =
        Regex("^\\+[1-9]\\d{7,14}$").matches(s)

    companion object {
        private const val TAG = "BgFcmService"
    }
}
