package com.amefure.mimamori.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.amefure.mimamori.Model.AppUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

    /**
     * ミマモリIDを元に対象のミマモリユーザーをクラウドに登録
     * 1.自身のユーザーIDを取得
     * 2.ミマモリIDのユーザーのマモラレリストに自身のユーザーIDを追加
     * 3.取得したミマモリIDを自身のミマモリリストに追加
     * 4.自身の情報が更新されるのでデータが自動更新される
     */
    public fun entryMimamoriUser(mimamoriId: String, userId:String, completion: (Boolean) -> Unit) {
        databaseRepository.updateMamorareIDList(userId, mimamoriId) { result ->
            if (result) {
                databaseRepository.updateNotifyMimamoriList(userId, mimamoriId) {
                    result ->
                    if (result) {
                        completion(true)
                    } else {
                        completion(false)
                    }
                }
            } else {
                completion(false)
            }
        }
    }
}