package com.autopulse.dev.storage

import android.content.Context
import java.util.UUID

class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var jwtToken: String?
        get() = prefs.getString(KEY_JWT_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_JWT_TOKEN, value).apply()

    // Auto-generates a UUID on first access and persists it forever
    val merchantId: String
        get() = prefs.getString(KEY_MERCHANT_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_MERCHANT_ID, it).apply()
        }

    var businessName: String?
        get() = prefs.getString(KEY_BUSINESS_NAME, null)
        set(value) = prefs.edit().putString(KEY_BUSINESS_NAME, value).apply()

    var ownerPhone: String?
        get() = prefs.getString(KEY_OWNER_PHONE, null)
        set(value) = prefs.edit().putString(KEY_OWNER_PHONE, value).apply()

    // Stored locally only — used in future SMS templates
    var businessPhone: String?
        get() = prefs.getString(KEY_BUSINESS_PHONE, null)
        set(value) = prefs.edit().putString(KEY_BUSINESS_PHONE, value).apply()

    val isOnboarded: Boolean
        get() = jwtToken != null

    val bearerToken: String
        get() = "Bearer ${jwtToken.orEmpty()}"

    fun clear() = prefs.edit().clear().apply()

    companion object {
        private const val PREFS_NAME = "recall_prefs"
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_MERCHANT_ID = "merchant_id"
        private const val KEY_BUSINESS_NAME = "business_name"
        private const val KEY_OWNER_PHONE = "owner_phone"
        private const val KEY_BUSINESS_PHONE = "business_phone"
    }
}
