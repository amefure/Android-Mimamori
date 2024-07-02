package com.amefure.mimamori.Repository.DataStore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.amefure.mimamori.Model.AuthProviderModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreRepository(private val context: Context) {

    companion object {
        private val SIGNIN_USER_PROVIDER = stringPreferencesKey("SIGNIN_USER_PROVIDER")
    }

    suspend fun saveSignInProvider(provider: AuthProviderModel) {
        try {
            context.dataStore.edit { preferences ->
                preferences[SIGNIN_USER_PROVIDER] = provider.name
            }
        } catch (e: IOException) {
            print("例外が発生したよ")
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
}