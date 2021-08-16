package com.salkuadrat.biometricx

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.annotation.NonNull
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.nio.charset.Charset
import javax.crypto.Cipher

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** BiometricxPlugin */
class BiometricxPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {

    private var activity: FragmentActivity? = null

    private lateinit var context: Context
    private lateinit var channel: MethodChannel
    private lateinit var cryptoManager: CryptoManager
    private lateinit var biometricHelper: BiometricHelper
    private lateinit var biometricManager: BiometricManager
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    companion object {
        private const val TAG = "BiometricxPlugin"
        private const val SHARED_PREFS_NAME = "com.salkuadrat.biometricx"
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        Log.d(TAG, "onAttachedToEngine")
        
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "biometricx")
        channel.setMethodCallHandler(this)

        context = flutterPluginBinding.applicationContext

        cryptoManager = CryptoManager()
        biometricManager = BiometricManager.from(context)
        biometricHelper = BiometricHelper(context)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when(call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "type" -> checkType(result)
            "encrypt" -> encrypt(call, result)
            "decrypt" -> decrypt(call, result)
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        Log.d(TAG, "onDetachedFromEngine")
        channel.setMethodCallHandler(null)
    }

    override fun onDetachedFromActivity() {
        Log.d(TAG, "onDetachedFromActivity")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        Log.d(TAG, "onReattachedToActivityForConfigChanges")
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        Log.d(TAG, "onAttachedToActivity")
        activity = binding.activity as FragmentActivity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        Log.d(TAG, "onDetachedFromActivityForConfigChanges")
    }

    private fun checkType(@NonNull result: Result) {
        val type = biometricHelper.biometricType()

        when (type) {
            BiometricType.FACE -> result.success("FACE")
            BiometricType.FINGERPRINT -> result.success("FINGERPRINT")
            BiometricType.IRIS -> result.success("IRIS")
            BiometricType.MULTIPLE -> result.success("MULTIPLE")
            BiometricType.NONE -> result.success("NONE")
            BiometricType.NO_HARDWARE -> result.success("NO_HARDWARE")
            BiometricType.UNAVAILABLE -> result.success("UNAVAILABLE")
            else -> result.success("UNSUPPORTED")
        }
    }

    private fun encrypt(@NonNull call: MethodCall, @NonNull result: Result) {
        val params = call.arguments as Map<String, String>
        val biometricKey = params["biometric_key"] as String
        val messageKey = params["message_key"] as String 
        val message = params["message"] as String
        val title = params["title"] as String
        val subtitle = params["subtitle"] as String
        val description = params["description"] as String
        val negativeButtonText = params["negative_button_text"] as String
        val confirmationRequired = params["confirmation_required"] as Boolean
        val deviceCredentialAllowed = params["device_credential_allowed"] as Boolean

        try {
            val cipher = cryptoManager.getInitializedCipherForEncryption(biometricKey)
            val crypto = BiometricPrompt.CryptoObject(cipher)

            biometricHelper.showBiometricPrompt(
                activity!!,
                BiometricPromptInfo(
                    title,
                    subtitle,
                    description,
                    negativeButtonText,
                    confirmationRequired,
                    deviceCredentialAllowed
                ),
                crypto, 
                { res ->
                    res.cryptoObject?.cipher?.let { cipher ->
                        val time = System.currentTimeMillis().toString()
                        val resultKey = when {
                            messageKey.isEmpty() -> "${biometricKey}_${time}"
                            else -> messageKey
                        }
                        val ciphertext = cryptoManager.encryptData(message, cipher)
                        
                        cryptoManager.saveCiphertext(
                            context, 
                            ciphertext,
                            SHARED_PREFS_NAME,
                            Context.MODE_PRIVATE,
                            resultKey
                        )

                        result.success(resultKey)
                    } ?: run {
                        failed(result)
                    }
                }, 
                { errCode, errString ->
                    result.error(errCode.toString(), errString.toString(), null)
                }, 
                {
                    failed(result)
                }
            )
        } catch (ex:Exception) {
            failed(result)
        }
    }

    private fun decrypt(@NonNull call: MethodCall, @NonNull result: Result) {
        val params = call.arguments as Map<String, String>
        val biometricKey = params["biometric_key"] as String
        val messageKey = params["message_key"] as String
        val title = params["title"] as String
        val subtitle = params["subtitle"] as String
        val description = params["description"] as String
        val negativeButtonText = params["negative_button_text"] as String
        val confirmationRequired = params["confirmation_required"] as Boolean
        val deviceCredentialAllowed = params["device_credential_allowed"] as Boolean

        val ciphertext = cryptoManager.restoreCiphertext(
            context,
            SHARED_PREFS_NAME,
            Context.MODE_PRIVATE,
            messageKey
        )

        ciphertext?.initializationVector?.let { iv -> 
            try {
                val cipher = cryptoManager.getInitializedCipherForDecryption(biometricKey, iv)
                val crypto = BiometricPrompt.CryptoObject(cipher)

                biometricHelper.showBiometricPrompt(
                    activity!!,
                    BiometricPromptInfo(
                        title,
                        subtitle,
                        description,
                        negativeButtonText,
                        confirmationRequired,
                        deviceCredentialAllowed
                    ),
                    crypto,
                    { res ->
                        ciphertext?.ciphertext?.let { ciphertext ->
                            res.cryptoObject?.cipher?.let { cipher ->
                                val message = cryptoManager.decryptData(ciphertext, cipher)
                                result.success(message)
                            } ?: run {
                                failed(result)
                            }
                        } ?: run {
                            failed(result)
                        }
                    },
                    { errCode, errString ->
                        result.error(errCode.toString(), errString.toString(), null)
                    },
                    {
                        failed(result)
                    }
                )
            } catch (ex:Exception) {
                failed(result)
            }
        } ?: run {
            failed(result)
        }
    }

    private fun failed(@NonNull result: Result) {
        result.error(
            "", 
            "Authentication failed for an unknown reason", 
            null
        )
    }
}