package com.amefure.mimamori.ViewModel

import android.app.Application
import android.util.Log
import com.amefure.mimamori.Managers.AppNotifyManager
import com.amefure.mimamori.Model.AppNotify
import com.amefure.mimamori.Model.AppUser
import com.amefure.mimamori.Repository.AppEnvironmentStore
import java.util.Calendar
import java.util.Date

class MamorareMainViewModel(app: Application) : RootViewModel(app) {


    // 登録済みのミマモリユーザーに向けて発火する
    public fun sendNotification(completion:(Boolean) -> Unit) {
        var index = 0
        val myAppUser = AppEnvironmentStore.instance.myAppUser.value ?: return
        val mimamoriList = myAppUser.currentMimamoriList
        mimamoriList.forEach {
            index += 1
            AppNotifyManager.sendNotification(it.fcmToken, "test","dddd") { result ->
                if (!result) {
                    Log.d("000000" , "失敗")
                    // 最後まで送信できたら終了
                    entryNotifyAPI(myAppUser)
                    // 失敗ならそこで終了
                    completion(false)
                    return@sendNotification
                } else if (index == mimamoriList.size) {
                    // 最後まで送信できたら終了
                    entryNotifyAPI(myAppUser)
                    completion(true)
                }
            }
        }
    }

    private fun entryNotifyAPI(myAppUser: AppUser) {
        val notifications = myAppUser.notifications.toMutableList()
        val notify = AppNotify(title = "title", msg = "msg")
        notifications.add(notify)
        val today = Calendar.getInstance()
        today.add(Calendar.DATE, -7)
        /// クラウドにあげるのは1週間分のデータのみとする
        val oneWeekAgo = today.time
        val filtering = notifications.filter( { it.time <= today.time && it.time >= oneWeekAgo })
        databaseRepository.updateUserInfo(myAppUser.id, notifications = filtering)
    }

}