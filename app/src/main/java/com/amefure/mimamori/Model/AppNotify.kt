package com.amefure.mimamori.Model

import com.amefure.mimamori.Utility.DateFormatUtility
import java.util.Date
import java.util.UUID

data class AppNotify(
    // 一意のID
    var id: String = UUID.randomUUID().toString(),
    // タイトル
    var title: String,
    // 通知メッセージ
    var msg: String,
    // 通知時間
    var time: Date = Date(),
) {
    /** 通知時間を[HH:mm:ss]形式で取得 */
    public fun getTimeString(): String {
        return DateFormatUtility("HH:mm:ss").getString(time)
    }
}