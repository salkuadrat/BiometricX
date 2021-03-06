package com.salkuadrat.biometricx 

/**
 * BiometricPromptInfo is a data class to simplify the creation of PromptInfo
 * from Biometric AndroidX.
 */
data class BiometricPromptInfo(
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val negativeButtonText: String,
    val confirmationRequired: Boolean = false,
    val deviceCredentialAllowed: Boolean = false
)