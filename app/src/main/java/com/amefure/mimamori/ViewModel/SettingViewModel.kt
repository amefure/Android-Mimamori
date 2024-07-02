package com.amefure.mimamori.ViewModel

import android.app.Application
import com.amefure.mimamori.Model.AppUser

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



    /** マモラレ削除 */
    public fun deleteMamorare(mamorareId: String, completion: (Boolean) -> Unit) {
        authRepository.getCurrentUser()?.uid?.let { userId ->
            databaseRepository.deleteMamorareList(userId, mamorareId) {
                completion(it)
            }
        }
    }

    /** ユーザー削除 */
    public fun deleteMyUser(myAppUser: AppUser) {
        databaseRepository.deleteMyUser(myAppUser)
    }
}