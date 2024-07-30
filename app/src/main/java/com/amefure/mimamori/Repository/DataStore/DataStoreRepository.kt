package com.amefure.mimamori.Repository.DataStore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException

class DataStoreRepository(private val context: Context) {

    companion object {
        public val SIGNIN_USER_NAME = stringPreferencesKey("SIGNIN_USER_NAME")
        public val SIGNIN_USER_ID = stringPreferencesKey("SIGNIN_USER_ID")
        public val SIGNIN_USER_PROVIDER = stringPreferencesKey("SIGNIN_USER_PROVIDER")
        public val IS_MAMORARE = booleanPreferencesKey("IS_MAMORARE")

        public val NOTIFY_SELECT_NUMBER = stringPreferencesKey("NOTIFY_SELECT_NUMBER")
        public val NOTIFY_MSG_1 = stringPreferencesKey("NOTIFY_MSG_1")
        public val NOTIFY_MSG_2 = stringPreferencesKey("NOTIFY_MSG_2")
        public val NOTIFY_MSG_3 = stringPreferencesKey("NOTIFY_MSG_3")

        /** 通知メッセージナンバー */
        enum class NotifyMsgNumber {
            ONE,
            TWO,
            THREE
        }

    }

    /** 任意のキーと値のペアを保存 */
    suspend fun <T> savePreference(key: Preferences.Key<T>, value: T) {
        try {
            context.dataStore.edit { preferences ->
                preferences[key] = value
            }
        } catch (e: IOException) {
            Log.d("DataStore", "例外が発生したよ")
        }
    }

    /** 任意のキーから値を取得 */
    public fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): T {
        return runBlocking {
            try {
                val preferences = context.dataStore.data.first()
                preferences[key] ?: defaultValue
            } catch (e: IOException) {
                Log.d("DataStore", "例外が発生したよ")
                defaultValue
            }
        }
    }

    /** 任意のキーの値を監視 */
    public fun <T> observePreference(key: Preferences.Key<T>): Flow<T?> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[key]
            }
    }

}