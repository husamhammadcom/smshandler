package com.husamhammd.smsotp

import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.husamhammd.smsotp/native"

    override fun configureFlutterEngine(engine: FlutterEngine) {
        super.configureFlutterEngine(engine)
        MethodChannel(engine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    // نوفّر الطريقتين لتوافق الكود القديم والجديد
                    "sendSms", "scheduleSms" -> {
                        val phone = call.argument<String>("phone")?.trim().orEmpty()
                        val text  = call.argument<String>("text")?.trim().orEmpty()
                        if (!isValidE164(phone) || text.isBlank()) {
                            result.success(false); return@setMethodCallHandler
                        }
                        try {
                            val sms = getDefaultSmsManager()
                            sms.sendTextMessage(phone, null, text, null, null)
                            result.success(true)
                        } catch (_: Exception) {
                            result.success(false)
                        }
                    }
                    "prewarm" -> {
                        // اختياري: إن كنت أضفت كلاس Prewarm
                        try { com.husamhammd.smsotp.util.Prewarm.run(this) } catch (_: Exception) {}
                        result.success(true)
                    }
                    else -> result.notImplemented()
                }
            }
    }

    private fun getDefaultSmsManager(): SmsManager {
        val subId = SubscriptionManager.getDefaultSmsSubscriptionId()
        return try {
            if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                SmsManager.getSmsManagerForSubscriptionId(subId)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    getSystemService(SmsManager::class.java)
                else @Suppress("DEPRECATION") SmsManager.getDefault()
            }
        } catch (_: Throwable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                getSystemService(SmsManager::class.java)
            else @Suppress("DEPRECATION") SmsManager.getDefault()
        }
    }

    private fun isValidE164(s: String) = Regex("^\\+[1-9]\\d{7,14}$").matches(s)
}
