package com.salkuadrat.biometricx

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.google.gson.Gson
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface CryptoManager {
    /**
     * This will gets or generates an instance of SecretKey, and then initializes Chiper with the key.
     * The secret key uses [ENCRYPT_MODE][Cipher.ENCRYPT_MODE].
     *
     * @return [Cipher]
     */
    fun getInitializedCipherForEncryption(keyName: String): Cipher

    /**
     * This will gets or generates an instance of SecretKey, and then initializes Cipher with the key.
     * The secret key uses [DECRYPT_MODE][Cipher.DECRYPT_MODE].
     *
     * @return [Cipher]
     */
    fun getInitializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher

    /**
     * Cipher created with [getInitializedCipherForEncryption] is used here to encrypt [message].
     *
     * @return [Ciphertext]
     */
    fun encryptData(message: String, cipher: Cipher): Ciphertext

    /**
     * Cipher created with [getInitializedCipherForDecryption] is used here to decrypt [ciphertext].
     *
     * @return [String]
     */
    fun decryptData(ciphertext: ByteArray, cipher: Cipher): String

    /**
     * Save [ciphertext] to Shared Preferences.
     *
     * @param context [Context]
     * @param ciphertext [Ciphertext]
     * @param prefName [String]
     * @param prefMode [Int]
     * @param prefKey [String]
     */
    fun saveCiphertext(
        context: Context,
        ciphertext: Ciphertext,
        prefName: String,
        prefMode: Int,
        prefKey: String
    )

    /**
     * Restored a saved [Ciphertext] from Shared Preferences.
     *
     * @param context [Context]
     * @param prefName [String]
     * @param prefMode [Int]
     * @param prefKey [String]
     *
     * @return [Ciphertext]
     */
    fun restoreCiphertext(
        context: Context,
        prefName: String,
        prefMode: Int,
        prefKey: String
    ): Ciphertext?
}

fun CryptoManager(): CryptoManager = CryptoManagerImpl()

data class Ciphertext(val ciphertext: ByteArray, val initializationVector: ByteArray)

private class CryptoManagerImpl: CryptoManager {

    private val KEY_SIZE = 256
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    private val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
    private val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES

    override fun getInitializedCipherForEncryption(keyName: String): Cipher {
        val cipher = getCipher()
        val secretKey = getSecretKey(keyName)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    override fun getInitializedCipherForDecryption(
        keyName: String, 
        initializationVector: ByteArray
    ): Cipher {
        val cipher = getCipher()
        val secretKey = getSecretKey(keyName)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
        return cipher
    }

    override fun encryptData(message: String, cipher: Cipher): Ciphertext {
        val messageByte = message.toByteArray(Charset.forName("UTF-8"))
        val ciphertext = cipher.doFinal(messageByte)
        return Ciphertext(ciphertext, cipher.iv)
    }

    override fun decryptData(ciphertext: ByteArray, cipher: Cipher): String {
        val messageByte = cipher.doFinal(ciphertext)
        val message = String(messageByte, Charset.forName("UTF-8"))
        return message
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance("$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING")
    }

    private fun getSecretKey(keyName: String): SecretKey {
        // If Secretkey exist for that keyName, grab and return it.
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        keyStore.getKey(keyName, null)?.let { return it as SecretKey }

        // If not, generate a new one
        val keyGen = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, 
            ANDROID_KEYSTORE
        )

        keyGen.init(
            KeyGenParameterSpec.Builder(
                keyName, 
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(ENCRYPTION_BLOCK_MODE)
                .setEncryptionPaddings(ENCRYPTION_PADDING)
                .setUserAuthenticationRequired(true)
                .setKeySize(KEY_SIZE)
                .build()
        )

        return keyGen.generateKey()
    }

    override fun saveCiphertext(
        context: Context,
        ciphertext: Ciphertext,
        prefName: String,
        prefMode: Int,
        prefKey: String
    ) {
        val json = Gson().toJson(ciphertext)
        context.getSharedPreferences(prefName, prefMode).edit().putString(prefKey, json).apply()
    }

    override fun restoreCiphertext(
        context: Context,
        prefName: String,
        prefMode: Int,
        prefKey: String
    ): Ciphertext? {
        val json = context.getSharedPreferences(prefName, prefMode).getString(prefKey, null)
        return Gson().fromJson(json, Ciphertext::class.java)
    }
}