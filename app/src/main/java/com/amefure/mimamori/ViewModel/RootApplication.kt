package com.amefure.mimamori.ViewModel

import android.app.Application
import com.amefure.mimamori.Repository.FirebaseAuthRepository

/**
 *   マニフェストファイルに
 *   [android:name=".ViewModel.RootApplication"]を追加
 */
class RootApplication : Application() {
    /**
     * [FirebaseAuthRepository]のインスタンス
     */
    val authRepository: FirebaseAuthRepository by lazy { FirebaseAuthRepository(this) }
    override fun onCreate() {
        super.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
    }

}