package com.iniyan.findmobilenumber

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.api.GoogleApiClient

class MainActivity : AppCompatActivity() {
    private lateinit var googleApiClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tryGetCurrentUserPhoneNumber(this)
//        googleApiClient = GoogleApiClient.Builder(this).addApi(Auth.CREDENTIALS_API).build()
//        if (phoneNumber.isEmpty()) {
//            val hintRequest = HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build()
//            val intent = Auth.CredentialsApi.getHintPickerIntent(googleApiClient, hintRequest)
//            try {
//                startIntentSenderForResult(intent.intentSender, REQUEST_PHONE_NUMBER, null, 0, 0, 0);
//            } catch (e: IntentSender.SendIntentException) {
//                Toast.makeText(this, "failed to show phone picker", Toast.LENGTH_SHORT).show()
//            }
//        } else
            onGotPhoneNumberToSendTo()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PHONE_NUMBER) {
            if (resultCode == Activity.RESULT_OK) {
                val cred: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
                phoneNumber = cred?.id ?: ""
                if (phoneNumber.isEmpty())
                    Toast.makeText(this, "failed to get phone number", Toast.LENGTH_SHORT).show()
                else
                    onGotPhoneNumberToSendTo()
            }
        }
    }

    private fun onGotPhoneNumberToSendTo() {
        Toast.makeText(this, "got number:$phoneNumber", Toast.LENGTH_SHORT).show()
    }


    companion object {
        private const val REQUEST_PHONE_NUMBER = 1
        private var phoneNumber = ""

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
                            return number
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
            try {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val number = telephonyManager.line1Number ?: ""
                if (!number.isBlank()) {
                    phoneNumber = number
                    return number
                }
            } catch (e: Exception) {
            }
            return ""
        }
    }
}