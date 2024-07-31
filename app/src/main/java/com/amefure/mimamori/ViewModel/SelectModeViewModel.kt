package com.amefure.mimamori.ViewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.amefure.mimamori.Repository.DataStore.DataStoreRepository
import kotlinx.coroutines.launch

/**
 * [Onboarding3Fragment] と [SelectAppMainModeFragment]で使用
 * サインインユーザーのマモラレフラグを切り替える
 */
class SelectModeViewModel(val app: Application) : RootViewModel(app) {

    /** マモラレ/ミマモリに設定 */
    public fun selectIsMamorareMode(isMamorare: Boolean) {
        authRepository.getCurrentUser()?.uid?.let { userId ->
            viewModelScope.launch {
                dataStoreRepository.savePreference(DataStoreRepository.IS_MAMORARE, isMamorare)
            }
            databaseRepository.updateUserInfo(userId, isMamorare = isMamorare)
        }
    }
}