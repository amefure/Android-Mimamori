package com.amefure.mimamori.ViewModel

import android.app.Application
import com.amefure.mimamori.Repository.DataStore.DataStoreRepository
import com.amefure.mimamori.Repository.FBDatabaseRepository
import com.amefure.mimamori.Repository.FirebaseAuthRepository

/**
 *   マニフェストファイルに
 *   [android:name=".ViewModel.RootApplication"]を追加
 */
class RootApplication : Application() {

    /** [FirebaseAuthRepository]のインスタンス */
    val authRepository: FirebaseAuthRepository by lazy { FirebaseAuthRepository(this) }

    /** [FirebaseAuthRepository]のインスタンス */
    val databaseRepository: FBDatabaseRepository by lazy { FBDatabaseRepository.instance }

    /** [DataStoreRepository]のインスタンス */
    val dataStoreRepository: DataStoreRepository by lazy { DataStoreRepository(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    companion object {
        lateinit var instance: RootApplication
    }
}