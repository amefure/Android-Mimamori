package com.amefure.mimamori.Utility

import com.amefure.mimamori.Model.AppNotify
import com.amefure.mimamori.Model.GSON.AppNotifyTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class JsonFormatterUtility {
    companion object {
        private val gson: Gson = GsonBuilder()
            .registerTypeAdapter(AppNotify::class.java, AppNotifyTypeAdapter())
            .create()

        /** オブジェクトをJSONにエンコード */
        fun toJson(notifications: List<AppNotify>): String = gson.toJson(notifications)

        /** JSONをオブジェクトにデコード */
        fun fromJson(json: String):  List<AppNotify> {
            // TypeTokenを使用して、List<AppNotify>のタイプを取得
            val listType = object : TypeToken<List<AppNotify>>() {}.type
            return gson.fromJson(json, listType)
        }
    }
}