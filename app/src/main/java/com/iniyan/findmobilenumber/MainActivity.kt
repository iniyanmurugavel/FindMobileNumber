package com.iniyan.findmobilenumber

import android.Manifest
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {

    private val MY_PERMISSION_REQUEST_CODE_PHONE_STATE = 1
    private val LOG_TAG = "AndroidExample"

    private var mSubscriptionManager: SubscriptionManager? = null
    var isMultiSimEnabled = false
    var requiredPermission = Manifest.permission.READ_PHONE_STATE

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askPermissionAndGetPhoneNumbers()

    }

    @SuppressLint("SetTextI18n")
    private fun onGotPhoneNumberToSendTo() {
        Toast.makeText(this, "$msg got number:$phoneNumber", Toast.LENGTH_SHORT).show()
        findViewById<TextView>(R.id.textView).text = "$msg got number:$phoneNumber"
    }



    private fun askPermissionAndGetPhoneNumbers() {

        // With Android Level >= 23, you have to ask the user
        // for permission to get Phone Number.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 23

            // Check if we have READ_PHONE_STATE permission
            val readPhoneStatePermission = ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
            )
            if (readPhoneStatePermission != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, requiredPermission)) {
                    Toast.makeText(this, "Phone state permission allows us to get phone number. Please allow it for additional functionality.", Toast.LENGTH_LONG).show()
                }
                /**
                 *  ActivityCompat.requestPermissions(this, arrayOf(permission), MY_PERMISSION_REQUEST_CODE_PHONE_STATE)
                 */
                requestPermissions(
                        arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS),
                        MY_PERMISSION_REQUEST_CODE_PHONE_STATE
                )
                return
            }

        }

        tryGetCurrentUserPhoneNumber(this)
        onGotPhoneNumberToSendTo()
        isDualSimOrNot()
        Log.e(LOG_TAG, "getMy10DigitPhoneNumber: Final No : ${getMy10DigitPhoneNumber()}")

        // this.getPhoneNumbers()

    }





    // When you have the request results
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSION_REQUEST_CODE_PHONE_STATE -> {

                // Note: If request is cancelled, the result arrays are empty.
                // Permissions granted (SEND_SMS).

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
                        PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    return
                }

                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {

                    Log.i(LOG_TAG, "Permission granted!")
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show()
                    //getPhoneNumbers()
                    tryGetCurrentUserPhoneNumber(this)
                    onGotPhoneNumberToSendTo()
                } else {
                    Log.i(LOG_TAG, "Permission denied!")

                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    // When results returned
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_PERMISSION_REQUEST_CODE_PHONE_STATE) {
            if (resultCode == RESULT_OK) {
                // Do something with data (Result returned).
                Toast.makeText(this, "Action OK", Toast.LENGTH_LONG).show()
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Action Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Action Failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private var phoneNumber = ""
        private var msg: String = ""


    }
    @SuppressLint("MissingPermission", "HardwareIds", "ServiceCast")
    private fun tryGetCurrentUserPhoneNumber(context: Context): String {
        if (phoneNumber.isNotEmpty())
            return phoneNumber

        //for dual sim mobile
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            try {

                if(subscriptionManager.activeSubscriptionInfoList.size > 1) {
                    Log.e(LOG_TAG, "tryGetCurrentUserPhoneNumber: Multi Sim : true")
                    //if there are two sims in dual sim mobile
                    val localList: List<*> = subscriptionManager.activeSubscriptionInfoList
                    val simInfo = localList[0] as SubscriptionInfo
                    val simInfo1 = localList[1] as SubscriptionInfo

                    val sim1 = simInfo.displayName.toString()
                    val sim2 = simInfo1.displayName.toString()


                    Log.e(LOG_TAG, "tryGetCurrentUserPhoneNumber: Sim1 : $sim1")
                    Log.e(LOG_TAG, "tryGetCurrentUserPhoneNumber: sim2  : $sim2")

                }
                else {
                    Log.e(LOG_TAG, "tryGetCurrentUserPhoneNumber: Multi Sim : false ")
                    //if there is 1 sim in dual sim mobile

                    //if there is 1 sim in dual sim mobile
                    val tManager = baseContext
                            .getSystemService(TELEPHONY_SERVICE) as TelephonyManager

                    val sim1 = tManager.networkOperatorName
                    Log.e(LOG_TAG, "tryGetCurrentUserPhoneNumber: Sim1 : $sim1")

                }

                subscriptionManager.activeSubscriptionInfoList?.forEach {
                    val number: String? = it.number
                    Log.e(LOG_TAG, "tryGetCurrentUserPhoneNumber: ${it.carrierName}")
                    Log.e(LOG_TAG, "tryGetCurrentUserPhoneNumber: ${it.countryIso}")
                    if (!number.isNullOrBlank()) {
                        phoneNumber = number
                        msg = "subscriptionManager"
                        return number
                    }
                }
            } catch (ignored: Exception) {
            }
        }else {
            //below android version 22
            try {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    return "";
                }
                val number = telephonyManager.line1Number ?: ""

                // get IMEI
                val imei = telephonyManager.deviceId

                Log.e(LOG_TAG, "tryGetCurrentUserPhoneNumber: $imei")

                // get SimSerialNumber
                val simSerialNumber = telephonyManager.simSerialNumber

                Log.e(LOG_TAG, "tryGetCurrentUserPhoneNumber: simSerialNumber: $simSerialNumber")



                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.e(LOG_TAG, "Device Slot Single 1: ${telephonyManager.getDeviceId(0)}")
                    Log.e(LOG_TAG, "Device Slot Single 1: ${telephonyManager.getDeviceId(1)}")
                    Log.e(LOG_TAG, "Device Slot Single 1: ${telephonyManager.phoneCount}")
                    Log.e(LOG_TAG, "Device Slot Single 1: ${telephonyManager.phoneType}")

                }

                if (!number.isBlank()) {
                    phoneNumber = number
                    msg = "telephonyManager"
                    return number
                }
            } catch (e: Exception) {
            }
        }

        return ""
    }




    private fun getMy10DigitPhoneNumber(): String? {
        val s: String = phoneNumber
        return if (s.length > 2) s.substring(3) else null
    }

    private fun isDualSimOrNot() {
        val telephonyInfo = TelephonyInfo.getInstance(this)
        val imeiSIM1 = telephonyInfo.imeiSIM1
        val imeiSIM2 = telephonyInfo.imeiSIM2
        val isSIM1Ready = telephonyInfo.isSIM1Ready
        val isSIM2Ready = telephonyInfo.isSIM2Ready
        val isDualSIM = telephonyInfo.isDualSIM
        Log.e(LOG_TAG + "Dual = ", """ IME1 : $imeiSIM1
        IME2 : $imeiSIM2
        IS DUAL SIM : $isDualSIM
        IS SIM1 READY : $isSIM1Ready
        IS SIM2 READY : $isSIM2Ready
        """)
    }


    // Need to ask user for permission: android.permission.READ_PHONE_STATE
    @SuppressLint("MissingPermission", "HardwareIds", "SetTextI18n")
    private fun getPhoneNumbers() {
        try {
            val manager = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            val phoneNumber1 = manager.line1Number ?: ""
            findViewById<TextView>(R.id.textView).text = "$msg got number:$phoneNumber1"

            Log.i(LOG_TAG, "Your Phone Number: $phoneNumber1")
            Toast.makeText(
                    this, "Your Phone Number: $phoneNumber1",
                    Toast.LENGTH_LONG
            ).show()

            // Other Informations:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API Level 26.
             //   val imei = manager.imei
                val phoneCount = manager.phoneCount
                Log.i(LOG_TAG, "Phone Count: $phoneCount")
                //Log.i(LOG_TAG, "EMEI: $imei")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // API Level 28.
                val signalStrength = manager.signalStrength
                val level = signalStrength!!.level
                Log.i(LOG_TAG, "Signal Strength Level: $level")
            }
        } catch (ex: java.lang.Exception) {
            Log.e(LOG_TAG, "Error: ", ex)
            Toast.makeText(
                    this, "Error: " + ex.message,
                    Toast.LENGTH_LONG
            ).show()
            ex.printStackTrace()
        }
    }
}