package com.example.cryptotrader.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Simple helper for storing API credentials securely using
 * [EncryptedSharedPreferences] backed by the Android Keystore.
 */
object ApiKeyStorage {

    private const val PREFS_FILE = "secure_api_prefs"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_API_SECRET = "api_secret"

    private lateinit var prefs: SharedPreferences

    /**
     * Initializes the encrypted preferences. Should be called once, e.g. in
     * [android.app.Application.onCreate].
     */
    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Stores the given key and secret. */
    fun saveCredentials(apiKey: String, apiSecret: String) {
        prefs.edit()
            .putString(KEY_API_KEY, apiKey)
            .putString(KEY_API_SECRET, apiSecret)
            .apply()
    }

    fun getApiKey(): String? = prefs.getString(KEY_API_KEY, null)

    fun getApiSecret(): String? = prefs.getString(KEY_API_SECRET, null)
}

