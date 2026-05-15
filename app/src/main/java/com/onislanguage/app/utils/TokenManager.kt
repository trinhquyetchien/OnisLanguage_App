package com.onislanguage.app.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "onis_prefs")

object TokenManager {
    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val EMAIL_KEY = stringPreferencesKey("user_email")
    private val DISPLAY_NAME_KEY = stringPreferencesKey("display_name")

    suspend fun saveToken(context: Context, token: String, userId: String, email: String? = null, displayName: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            if (!email.isNullOrBlank()) prefs[EMAIL_KEY] = email
            if (!displayName.isNullOrBlank()) prefs[DISPLAY_NAME_KEY] = displayName
        }
    }

    suspend fun saveUserProfile(context: Context, userId: String, email: String, displayName: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
            prefs[EMAIL_KEY] = email
            prefs[DISPLAY_NAME_KEY] = displayName
        }
    }

    data class StoredUser(
        val token: String?,
        val userId: String?,
        val email: String?,
        val displayName: String?
    )

    fun getStoredUser(context: Context): Flow<StoredUser> {
        return context.dataStore.data.map { prefs ->
            StoredUser(
                token = prefs[TOKEN_KEY],
                userId = prefs[USER_ID_KEY],
                email = prefs[EMAIL_KEY],
                displayName = prefs[DISPLAY_NAME_KEY]
            )
        }
    }

    fun getToken(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }
    }

    suspend fun clearToken(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(EMAIL_KEY)
            prefs.remove(DISPLAY_NAME_KEY)
        }
    }
}
