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
    override var time: Date = Date(),
): AppNotifyBase {
    /** 通知時間を[HH:mm:ss]形式で取得 */
    public fun getTimeString(format: String): String {
        return DateFormatUtility(format).getString(time)
    }
}

data class AppNotifySection(
    val dayStr: String,
    override var time: Date
): AppNotifyBase

interface AppNotifyBase {
     abstract var time: Date
}