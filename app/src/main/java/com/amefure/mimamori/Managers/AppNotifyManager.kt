package com.amefure.mimamori.Managers

import android.util.Log
import com.amefure.mimamori.Model.Config.AppURL
import com.amefure.mimamori.Service.NotifyApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class AppNotifyManager {

    companion object {

        /**
         * FCMリモートプッシュ通知発火
         */
        public fun sendNotification(token: String, title: String, msg: String) {

            val retrofit = Retrofit.Builder()
                .baseUrl(AppURL.MASTER_URL)
                .build()

            val apiService = retrofit.create(NotifyApiService::class.java)
            // Coroutine内で呼び出す
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = apiService.sendNotification(token, title, msg)
                    if (response.isSuccessful) {
                        Log.d("FCM Notify", "プッシュ通知リクエスト成功")
                    } else {
                        Log.d("FCM Notify", "Failed to send notification: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.d("FCM Notify", "Error: ${e.message}")
                }
            }
        }
    }
}