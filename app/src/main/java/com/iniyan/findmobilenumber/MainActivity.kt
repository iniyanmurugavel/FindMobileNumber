package com.iniyan.findmobilenumber

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tryGetCurrentUserPhoneNumber(this)
        onGotPhoneNumberToSendTo()
        findViewById<TextView>(R.id.textView).text = "$msg got number:$phoneNumber"
    }

    private fun onGotPhoneNumberToSendTo() {
        Toast.makeText(this, "$msg got number:$phoneNumber", Toast.LENGTH_SHORT).show()
    }


    companion object {
        private var phoneNumber = ""
        private var msg : String = ""
        @SuppressLint("MissingPermission", "HardwareIds", "ServiceCast")
        private fun tryGetCurrentUserPhoneNumber(context: Context): String {
            if (phoneNumber.isNotEmpty())
                return phoneNumber
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                try {
                    subscriptionManager.activeSubscriptionInfoList?.forEach {
                        val number: String? = it.number
                        if (!number.isNullOrBlank()) {
                            phoneNumber = number
                            msg = "subscriptionManager"
                            return number
                        }
                    }
                } catch (ignored: Exception) {}
            }
            try {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val number = telephonyManager.line1Number ?: ""
                if (!number.isBlank()) {
                    phoneNumber = number
                    msg = "telephonyManager"
                    return number
                }
            } catch (e: Exception) {
            }
            return ""
        }
    }
}