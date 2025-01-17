package com.amefure.mimamori.ViewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.amefure.mimamori.Model.Domain.AppUser
import com.amefure.mimamori.Model.Domain.AuthProviderModel
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.DataStore.DataStoreRepository
import com.amefure.mimamori.Repository.DataStore.DataStoreRepository.Companion.NotifyMsgNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingViewModel(val app: Application) : RootViewModel(app) {


    /** マモラレ対象変更 */
    public fun changeMamorare(mamorareId: String) {
        authRepository.getCurrentUser()?.uid?.let { userId ->
            databaseRepository.updateUserInfo(userId, currentMamorareId = mamorareId)
        }
    }


    /** マモラレ削除 */
    public fun deleteMamorareUser(mamorareId: String) {
        authRepository.getCurrentUser()?.uid?.let { userId ->
            // マモラレ側のミマモリリストから自身のユーザーIDを削除
            databaseRepository.deleteNotifyMimamoriList(mamorareId, userId) {
                // 自身のマモラレリストから相手のユーザーIDを削除
                databaseRepository.deleteMamorareList(userId, mamorareId) { }
            }
        }
    }

    /** ユーザー削除 */
    public fun deleteMyUser(myAppUser: AppUser) {
        databaseRepository.deleteMyUser(myAppUser)
        resetUserForLocal()
    }

    /**　ローカルのユーザー情報をリセットする　*/
    private fun resetUserForLocal() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.savePreference(DataStoreRepository.SIGNIN_USER_ID, "")
            dataStoreRepository.savePreference(DataStoreRepository.SIGNIN_USER_NAME, "")
            dataStoreRepository.savePreference(DataStoreRepository.SIGNIN_USER_PROVIDER, AuthProviderModel.NONE.name)
        }
    }

    /** サインインプロバイダ取得 */
    public fun getSignInProvider(): String {
        return dataStoreRepository.getPreference(DataStoreRepository.SIGNIN_USER_PROVIDER, AuthProviderModel.NONE.name)
    }

    /** ユーザー名変更 */
    public fun updateUserName(id: String, name: String) {
        databaseRepository.updateUserInfo(id, name)
        viewModelScope.launch {
            dataStoreRepository.savePreference(DataStoreRepository.SIGNIN_USER_NAME, name)
        }
    }


    /** ユーザー名取得 */
    public fun getSignInUserName(): String {
        return dataStoreRepository.getPreference(DataStoreRepository.SIGNIN_USER_NAME, "未設定")
    }



    /** 通知メッセージ選択ナンバー保存 */
    public fun saveNotifySelectNumber(number: Int) {
        val number = when (number) {
            1 -> NotifyMsgNumber.ONE
            2 -> NotifyMsgNumber.TWO
            3 -> NotifyMsgNumber.THREE
            else -> NotifyMsgNumber.ONE
        }
        viewModelScope.launch {
            dataStoreRepository.savePreference(DataStoreRepository.NOTIFY_SELECT_NUMBER, number.name)
        }
    }

    /** 通知メッセージ選択ナンバー取得 */
    public fun getNotifySelectNumber(): Int {
        val number = dataStoreRepository.getPreference(DataStoreRepository.NOTIFY_SELECT_NUMBER, NotifyMsgNumber.ONE.name)
        return when (number) {
            NotifyMsgNumber.ONE.name -> 1
            NotifyMsgNumber.TWO.name -> 2
            NotifyMsgNumber.THREE.name -> 3
            else -> 1
        }
    }

    /** 通知メッセージ保存 */
    public fun saveNotifyMsg(msg: String, number: Int) {
        viewModelScope.launch {
            val key = when (number) {
                1 -> DataStoreRepository.NOTIFY_MSG_1
                2 -> DataStoreRepository.NOTIFY_MSG_2
                3 -> DataStoreRepository.NOTIFY_MSG_3
                else -> DataStoreRepository.NOTIFY_MSG_1
            }
            dataStoreRepository.savePreference(key, msg)
        }
    }

    /** 通知メッセージ取得 */
    public fun getNotifyMsg(number: NotifyMsgNumber): String {
        val key = when(number) {
            NotifyMsgNumber.ONE -> DataStoreRepository.NOTIFY_MSG_1
            NotifyMsgNumber.TWO -> DataStoreRepository.NOTIFY_MSG_2
            NotifyMsgNumber.THREE -> DataStoreRepository.NOTIFY_MSG_3
        }
        return dataStoreRepository.getPreference(key, app.getString(R.string.notify_button_msg))
    }
}