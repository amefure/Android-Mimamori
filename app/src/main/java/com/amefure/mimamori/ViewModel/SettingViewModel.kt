package com.amefure.mimamori.ViewModel

import android.app.Application

class SettingViewModel(app: Application) : RootViewModel(app) {

    /** マモラレに設定 */
    public fun selectMamorare() {
        authRepository.getCurrentUser()?.uid?.let { userId ->
            databaseRepository.updateUserInfo(userId, isMamorare = true)
        }
    }

    /** ミマモリに設定 */
    public fun selectMimamori() {
        authRepository.getCurrentUser()?.uid?.let { userId ->
            databaseRepository.updateUserInfo(userId, isMamorare = false)
        }
    }

}