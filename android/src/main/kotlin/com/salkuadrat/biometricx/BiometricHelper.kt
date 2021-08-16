package com.salkuadrat.biometricx 

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat

class BiometricHelper(private val context: Context) {

    companion object {
        private const val TAG = "BiometricHelper"
    }

    private val biometricManager: BiometricManager = BiometricManager.from(context)

    private val features: List<BiometricType> = 
        listOf(
            "android.hardware.fingerprint" to BiometricType.FINGERPRINT,
            "android.hardware.biometrics.face" to BiometricType.FACE,
            "android.hardware.iris" to BiometricType.IRIS
        ).filter { context.packageManager.hasSystemFeature(it.first) }.map { it.second }

    private fun checkMinVersion(): Boolean = 
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M 

    /**
     * Returns the [BiometricType] available on the device.
     *
     * @return [BiometricType]
     */
    fun biometricType(): BiometricType = 
        if (checkMinVersion()) {
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS -> when {
                    features.isEmpty() -> BiometricType.NONE 
                    features.size == 1 -> features[0]
                    else -> BiometricType.MULTIPLE
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricType.NO_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricType.UNAVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricType.NONE
                else -> BiometricType.UNSUPPORTED
            }
        } else BiometricType.UNSUPPORTED
    
    /**
     * Show biometric prompt with [BiometricPromptInfo] and callback.
     *
     * @param activity [FragmentActivity]
     * @param biometricPromptInfo [BiometricPromptInfo]
     * @param onSuccess Success callback
     * @param onError Error callback optional, null by default 
     * @param onFailed Failed callback optional, null by default
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        biometricPromptInfo: BiometricPromptInfo,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: ((Int, CharSequence) -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        showBiometricPrompt(
            activity,
            biometricPromptInfo.toPromptInfo(),
            authenticationCallback(onSuccess, onError, onFailed)
        )
    }

    /**
     * Show biometric prompt with [BiometricPromptInfo], [BiometricPrompt.CryptoObject] and callback.
     *
     * @param activity [FragmentActivity]
     * @param biometricPromptInfo [BiometricPromptInfo]
     * @param crypto [BiometricPrompt.CryptoObject]
     * @param onSuccess Success callback
     * @param onError Error callback optional, null by default 
     * @param onFailed Failed callback optional, null by default
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        biometricPromptInfo: BiometricPromptInfo,
        crypto: BiometricPrompt.CryptoObject,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: ((Int, CharSequence) -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        showBiometricPrompt(
            activity,
            biometricPromptInfo.toPromptInfo(),
            authenticationCallback(onSuccess, onError, onFailed),
            crypto
        )
    }

    /**
     * Show biometric prompt with [BiometricPromptInfo], 
     * [BiometricPrompt.AuthenticationCallback] and [BiometricPrompt.CryptoObject].
     *
     * @param activity [FragmentActivity]
     * @param biometricPromptInfo [BiometricPromptInfo]
     * @param callback [BiometricPrompt.AuthenticationCallback]
     * @param crypto [BiometricPrompt.CryptoObject]
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        biometricPromptInfo: BiometricPromptInfo,
        callback: BiometricPrompt.AuthenticationCallback,
        crypto: BiometricPrompt.CryptoObject,
    ) {
        showBiometricPrompt(
            activity,
            biometricPromptInfo.toPromptInfo(),
            callback,
            crypto
        )
    }

    /**
     * Show biometric prompt with [PromptInfo], [BiometricPrompt.CryptoObject] and callback.
     *
     * @param activity [FragmentActivity]
     * @param biometricPromptInfo [BiometricPromptInfo]
     * @param crypto [BiometricPrompt.CryptoObject]
     * @param onSuccess Success callback
     * @param onError Error callback optional, null by default 
     * @param onFailed Failed callback optional, null by default
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        promptInfo: PromptInfo,
        crypto: BiometricPrompt.CryptoObject,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: ((Int, CharSequence) -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        showBiometricPrompt(
            activity,
            promptInfo,
            authenticationCallback(onSuccess, onError, onFailed),
            crypto
        )
    }

    /**
     * Show biometric prompt with [PromptInfo], [BiometricPrompt.AuthenticationCallback]
     * and [BiometricPrompt.CryptoObject].
     *
     * @param activity [FragmentActivity]
     * @param promptInfo [PromptInfo]
     * @param callback [BiometricPrompt.AuthenticationCallback]
     * @param crypto [BiometricPrompt.CryptoObject] optional, null by default
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        promptInfo: PromptInfo,
        callback: BiometricPrompt.AuthenticationCallback,
        crypto: BiometricPrompt.CryptoObject? = null,
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        BiometricPrompt(
            activity,
            executor,
            callback
        ).apply {
            crypto?.let { authenticate(promptInfo, it) } ?: authenticate(promptInfo)
        }
    }

    private fun BiometricPromptInfo.toPromptInfo(): PromptInfo = 
        PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setConfirmationRequired(confirmationRequired)
            .setDeviceCredentialAllowed(deviceCredentialAllowed)
            .setNegativeButtonText(negativeButtonText)
            .build()

    private fun authenticationCallback(
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: ((Int, CharSequence) -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ): BiometricPrompt.AuthenticationCallback = 
        object: BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Authentication success")
                onSuccess(result)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "$errorCode: $errString")
                onError?.invoke(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "Authentication failed")
                onFailed?.invoke()
            }
        }
}
