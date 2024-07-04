package com.amefure.mimamori.Model

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
)