package com.amefure.mimamori.Model.Domain

import com.amefure.mimamori.Utility.DateFormatUtility
import java.util.Calendar
import java.util.UUID


data class AppUser(
    // 一意のID
    var id: String,
    // ユーザー名
    var name: String,
    // FCM登録トークン(Firebase Cloud Message)
    var fcmToken: String,
    // マモラレ or ミマモリ 設定
    var isMamorare: Boolean,
    //  通知(1週間分の通知情報を保持)クラウドへはJSONでアップロード
    var notifications: List<AppNotify>,

    // ---- マモラレ(自分)側が参照 ----
    // ミマモリ対象のユーザーIDリスト
    var mimamoriIdList: List<String>,
    // 設定対象のユーザークラス(これはクラウドに格納しない)
    var currentMimamoriList: List<AppUser> = emptyList(),

    // ---- ミマモリ(自分)側が参照 ----
    // 設定対象のユーザーID
    var currentMamorareId: String,
    // 設定対象のユーザークラス(これはクラウドに格納しない)
    var currentMamorareList: List<AppUser> = emptyList(),
    // マモラレ対象のユーザーIDリスト
    var mamorareIdList:  List<String>

) {
    companion object {
        public const val TABLE_NAME = "users"
        // 共通
        public const val ID_KEY = "id"
        public const val NAME_KEY = "name"
        public const val FCM_TOKEN_KEY = "fcm_token"
        public const val IS_MAMORARE_KEY = "is_mamorare"
        public const val NOTIFICATIONS_KEY = "notifications"

        // マモラレ(自分)側が参照
        public const val MIMAMORI_ID_LIST_KEY = "mimamori_id_list"

        // ミマモリ(自分)側が参照
        public const val CURRENT_MAMORARE_ID = "current_mamorare_id"
        public const val MAMORARE_ID_LIST_KEY = "mamorare_id_list"

        public fun demoUser(): AppUser = AppUser(
                id = UUID.randomUUID().toString(),
                name =  "ミマモリデモユーザー",
                fcmToken = "",
                isMamorare = false,
                notifications = emptyList(),
                mimamoriIdList = emptyList(),
                currentMamorareId = "",
                mamorareIdList = emptyList()
            )

        /** 通知に年月日のセクションヘッダーを追加 */
        public fun sectionNotifications(notifications: List<AppNotify>): List<AppNotifyBase> {
            val sectionNotify = notifications.map { notify ->
                AppNotifySection(
                    dayStr = notify.getTimeString("yyyy年M月d日"),
                    time = DateFormatUtility.resetEndTime(notify.time)
                )
            }.distinctBy { it.dayStr }
            val list = mutableListOf<AppNotifyBase>()
            list.addAll(0, notifications)
            list.addAll(0, sectionNotify)
            return list.sortedBy { it.time }.reversed()
        }

        /** 本日の通知の個数 */
        public fun getTodayNotifyCount(notifications: List<AppNotify>): Int {
            val count = notifications.filter { notify ->
                DateFormatUtility.isSameDate(notify.time, Calendar.getInstance().time)
            }.size
            return count
        }

    }
}