package com.amefure.mimamori.Service

import com.amefure.mimamori.BuildConfig
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * FCM通知発行API
 */
interface NotifyApiService {
    @POST(BuildConfig.NOTIFY_END_POINT + "{token}/{title}/{msg}")
    suspend fun sendNotification(
        @Path("token") token: String,
        @Path("title") title: String,
        @Path("msg") msg: String
    ): Response<Unit>
}