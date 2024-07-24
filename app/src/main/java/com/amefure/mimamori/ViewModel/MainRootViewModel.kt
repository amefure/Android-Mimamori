package com.amefure.mimamori.ViewModel

import android.app.Application
import android.util.Log
import com.amefure.mimamori.Managers.AppNotifyManager
import com.amefure.mimamori.Model.AppNotify
import com.amefure.mimamori.Model.AppUser
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.AppEnvironmentStore
import com.amefure.mimamori.Repository.DataStore.DataStoreRepository
import com.amefure.mimamori.Repository.DataStore.DataStoreRepository.Companion.NotifyMsgNumber
import java.util.Calendar
import java.util.concurrent.atomic.AtomicInteger

class MainRootViewModel(val app: Application) : RootViewModel(app) {


    /** 登録済みのミマモリユーザーに向けて発火する */
    public fun sendNotification(completion:(Boolean) -> Unit) {
        val myAppUser = AppEnvironmentStore.instance.myAppUser.value ?: return
        val mimamoriList = myAppUser.currentMimamoriList
        val count = AtomicInteger(mimamoriList.size)
        val title = app.getString(R.string.notify_title, myAppUser.name)
        val msg = getNotifyMsg()
        mimamoriList.forEach {
            AppNotifyManager.sendNotification(it.fcmToken, title, msg) { result ->
                if (!result) {
                    // 失敗ならクラウドへ登録せずに終了
                    Log.d("FCM Notify", "通知送信失敗")
                    // FIXME 消す
                    entryNotifyAPI(myAppUser, title, msg)
                    completion(false)
                    return@sendNotification
                } else if (count.decrementAndGet() == 0) {
                    Log.d("FCM Notify", "通知送信成功")
                    // 最後まで送信できたらクラウドへ登録して終了
                    entryNotifyAPI(myAppUser, title, msg)
                    completion(true)
                }
            }
        }
    }

    /** 通知をクラウドに登録 */
    private fun entryNotifyAPI(myAppUser: AppUser, title: String, msg: String) {
        val notifications = myAppUser.notifications.toMutableList()
        val notify = AppNotify(title = title, msg = msg)
        notifications.add(notify)
        // 現在時刻を取得
        val today = Calendar.getInstance()
        // 1週間前の時刻を取得
        val oneWeekAgo = Calendar.getInstance().apply { add(Calendar.DATE, -7) }.time
        // 直近1週間分だけにフィルタリングして日付ごとにソート
        val filtering = notifications.filter { it.time <= today.time && it.time >= oneWeekAgo }.sortedBy { it.time }.reversed()
        databaseRepository.updateUserInfo(myAppUser.id, notifications = filtering)
    }

    /** 通知メッセージ取得 */
    private fun getNotifyMsg(): String {
        val number = dataStoreRepository.getPreference(DataStoreRepository.NOTIFY_SELECT_NUMBER, NotifyMsgNumber.ONE.name)
        val key = when(number) {
            NotifyMsgNumber.ONE.name -> DataStoreRepository.NOTIFY_MSG_1
            NotifyMsgNumber.TWO.name -> DataStoreRepository.NOTIFY_MSG_2
            NotifyMsgNumber.THREE.name -> DataStoreRepository.NOTIFY_MSG_3
            else -> DataStoreRepository.NOTIFY_MSG_1
        }
        return dataStoreRepository.getPreference(key, app.getString(R.string.notify_button_msg))
    }
}