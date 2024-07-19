package com.amefure.mimamori.Repository.DataStore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.amefure.mimamori.Model.AuthProviderModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException

class DataStoreRepository(private val context: Context) {

    companion object {
        private val SIGNIN_USER_PROVIDER = stringPreferencesKey("SIGNIN_USER_PROVIDER")
        private val IS_MAMORARE = booleanPreferencesKey("IS_MAMORARE")
    }

    suspend fun saveSignInProvider(provider: AuthProviderModel) {
        try {
            context.dataStore.edit { preferences ->
                preferences[SIGNIN_USER_PROVIDER] = provider.name
            }
        } catch (e: IOException) {
            Log.d("DataStore", "例外が発生したよ")
        }
    }

    public fun observeSignInProvider(): Flow<String?> {
        return context.dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[SIGNIN_USER_PROVIDER]
        }
    }

    suspend fun saveIsMamorare(isMamorare: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[IS_MAMORARE] = isMamorare
            }
        } catch (e: IOException) {
            Log.d("DataStore", "例外が発生したよ")
        }
    }

    public fun observeIsMamorare(): Flow<Boolean?> {
        return context.dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[IS_MAMORARE]
        }
    }

    public fun getIsMamorare(): Boolean {
        return runBlocking {
            try {
                val preferences = context.dataStore.data.first()
                preferences[IS_MAMORARE] ?: false
            } catch (e: IOException) {
                Log.d("DataStore", "Failed to read preferences", e)
                false
            } catch (e: Exception) {
                Log.d("DataStore", "Unexpected error", e)
                false
            }
        }
    }
}