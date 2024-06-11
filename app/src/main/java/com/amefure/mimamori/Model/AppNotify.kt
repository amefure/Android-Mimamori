package com.amefure.mimamori.Model

import java.util.Date

data class AppNotify(
    // 一意のID
    var id: String,
    // タイトル
    var title: String,
    // 通知メッセージ
    var msg: String,
    // 通知時間
    var time: Date,
)