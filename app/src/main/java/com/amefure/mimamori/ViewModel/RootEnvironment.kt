package com.amefure.mimamori.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.amefure.mimamori.Model.AppUser
import com.amefure.mimamori.Model.myFcmToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RootEnvironment(app: Application) : RootViewModel(app) {

    // 自分のユーザー情報
    public var myAppUser: Flow<AppUser> = databaseRepository.myAppUser as Flow<AppUser>

    /**
     * クラウドから取得したAppUser情報を観測開始
     */
    public fun observeMyUserData() {
        // クラウドから取得したAppUser情報を観測
        viewModelScope.launch(Dispatchers.Main) {
            databaseRepository.myAppUser.collect { user ->
                Log.d("Realtime Database", "USER：${user.toString()}")
            }
        }

        // ローカルにあるユーザーIDを使用してクラウドを観測開始
        authRepository.getCurrentUser()?.uid?.let { userId ->
            Log.d("Realtime Database", "USERID：${userId}")
            databaseRepository.observeMyUserData(userId)
        }
    }
}