package com.husamhammd.smsotp.util

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import com.google.firebase.messaging.FirebaseMessaging

object Prewarm {
    fun run(ctx: Context) {
        
        try { FirebaseMessaging.getInstance().token.addOnCompleteListener { } } catch (_: Exception) {}

        
        try {
            val subId = SubscriptionManager.getDefaultSmsSubscriptionId()
            val smsManager =
                if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID)
                    SmsManager.getSmsManagerForSubscriptionId(subId)
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    ctx.getSystemService(SmsManager::class.java)
                else
                    @Suppress("DEPRECATION") SmsManager.getDefault()

            
            smsManager.divideMessage("ping")
        } catch (_: Exception) {}
    }
}
