package com.ist.instocktracker.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ist.instocktracker.data.auth.TokenResponse
import com.ist.instocktracker.data.interfaces.TokenStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore implementation for storing and retrieving the Google ID Token
 */
class TokenDataStore(private val context: Context) : TokenStore {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")
        private val GOOGLE_ID_TOKEN_KEY = stringPreferencesKey("google_id_token")
        private val JWT_KEY = stringPreferencesKey("jwt")
        private val DEVICE_TOKEN_KEY = stringPreferencesKey("device_token")
    }

    override suspend fun getDeviceToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[DEVICE_TOKEN_KEY]
        }
    }

    override suspend fun saveDeviceToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[DEVICE_TOKEN_KEY] = token
        }
    }

    override suspend fun clearDeviceToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(DEVICE_TOKEN_KEY)
        }
    }


    // Google ID Token
    override suspend fun saveGoogleIdToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[GOOGLE_ID_TOKEN_KEY] = token
        }
    }

    override fun getGoogleIdToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[GOOGLE_ID_TOKEN_KEY]
        }
    }

    override suspend fun clearGoogleIdToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(GOOGLE_ID_TOKEN_KEY)
        }
    }

    // JWT
    override suspend fun saveJwt(token: TokenResponse) {
        context.dataStore.edit { preferences ->
            preferences[JWT_KEY] = token.toJson()
        }
    }

    override fun getJwt(): Flow<TokenResponse?> {
        return context.dataStore.data.map { preferences ->
            TokenResponse.fromJson(preferences[JWT_KEY])
        }
    }

    override suspend fun clearJwt() {
        Log.d("TokenDataStore", "clearJwt called")
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_KEY)
        }
    }

    /**
     * Check if the user is authenticated (has a Google ID Token)
     * @return Flow of boolean indicating if the user is authenticated
     */
    override fun isAuthenticated(): Flow<Boolean> {
        return getJwt().map { it != null }
    }

}