package com.amefure.mimamori.Model


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
    var currentMimamoriList: List<AppUser>,

    // ---- ミマモリ(自分)側が参照 ----
    // 設定対象のユーザーID
    var currentMamorareId: String,
    // 設定対象のユーザークラス(これはクラウドに格納しない)
    var currentMamorareList: List<AppUser>,
    // マモラレ対象のユーザーIDリスト
    var mamorareIdList:  List<String>
) {
    companion object {
        public val TABLE_NAME = "tests"
        // 共通
        public val ID_KEY = "id"
        public val NAME_KEY = "name"
        public val FCM_TOKEN_KEY = "fcm_token"
        public val IS_MAMORARE_KEY = "is_mamorare"
        public val NOTIFICATIONS_KEY = "notifications"

        // マモラレ(自分)側が参照
        public val MIMAMORI_ID_LIST_KEY = "mimamori_id_list"

        // ミマモリ(自分)側が参照
        public val CURRENT_MAMORARE_ID = "current_mamorare_id"
        public val MAMORARE_ID_LIST_KEY = "mamorare_id_list"
    }
}