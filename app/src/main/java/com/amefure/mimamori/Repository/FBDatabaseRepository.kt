package com.amefure.mimamori.Repository

import android.util.Log
import com.amefure.mimamori.Model.Domain.AppNotify
import com.amefure.mimamori.Model.Domain.AppUser
import com.amefure.mimamori.Utility.JsonFormatterUtility
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.atomic.AtomicInteger

class FBDatabaseRepository {
    companion object {
        public val instance = FBDatabaseRepository()
    }

    private val ref = Firebase.database.reference

    // 自分のユーザー情報
    private val _myAppUser = BehaviorSubject.createDefault(AppUser.demoUser())
    val myAppUser: Observable<AppUser> = _myAppUser.hide()

    private var oldNotifications: Int = 0

    private var observeMyUserDataListener: ValueEventListener? = null
    private var observeMamorareDataListener: ValueEventListener? = null

    /**
     * User登録処理
     * 新規作成 or サインインした際に実行
     */
    public fun createUser(
        userId: String,
        name: String,
        token: String,
    ) {
        val usersInfo = mapOf<String,Any>(
            AppUser.NAME_KEY to name,
            AppUser.FCM_TOKEN_KEY to token,
            AppUser.IS_MAMORARE_KEY to true,

        )
        ref.child(AppUser.TABLE_NAME).child(userId).updateChildren(usersInfo)
    }

    /**
     * User情報更新処理
     */
    public fun updateUserInfo(
        userId: String,
        name: String = "",
        token: String = "",
        isMamorare: Boolean? = null,
        notifications: List<AppNotify> = emptyList(),
        currentMamorareId: String = ""
    ) {
        val usersInfo: MutableMap<String, Any> = mutableMapOf()
        if (!name.isEmpty()) {
            usersInfo.put(AppUser.NAME_KEY, name)
        }
        if (!token.isEmpty()) {
            usersInfo.put(AppUser.FCM_TOKEN_KEY, token)
        }
        isMamorare?.let {
            usersInfo.put(AppUser.IS_MAMORARE_KEY, isMamorare)
        }
        if (!notifications.isEmpty()) {
            val json = JsonFormatterUtility.toJson(notifications)
            usersInfo.put(AppUser.NOTIFICATIONS_KEY, json)
        }
        if (!currentMamorareId.isEmpty()) {
            usersInfo.put(AppUser.CURRENT_MAMORARE_ID, currentMamorareId)
        }
        if (usersInfo.count() == 0) return
        ref.child(AppUser.TABLE_NAME).child(userId).updateChildren(usersInfo)
    }

    /**
     * 通知送信対象の読み取れるFCM登録トークンとミマモリIDを更新
     * 更新対象；自分のユーザー情報
     * 更新値：相手のユーザーID
     */
    public fun updateNotifyMimamoriList(
        userId: String,
        mimamoriId: String,
        completion: (Boolean) -> Unit) {
        val userRef = ref.child(AppUser.TABLE_NAME).child(userId)
        userRef.get()
            .addOnSuccessListener {
                val result = it.value ?: run { completion(false); return@addOnSuccessListener }
                val userDic = result as? Map <String, Any> ?: run {completion(false); return@addOnSuccessListener }

                val ids = userDic[AppUser.MIMAMORI_ID_LIST_KEY] as? MutableList<String>
                ids?.let {
                    it.add(mimamoriId)
                    val value = mapOf(
                        // 重複したIDの場合は格納しないようにdistinct
                        AppUser.MIMAMORI_ID_LIST_KEY to it.distinct().toList()
                    )
                    userRef.updateChildren(value)
                }?: run {
                    val value = mapOf(
                        AppUser.MIMAMORI_ID_LIST_KEY to listOf(mimamoriId)
                    )
                    userRef.updateChildren(value)
                }
                completion(true)
            }.addOnFailureListener {
                Log.d("Realtime Database", "データ取得エラー${it}")
                completion(false)
            }
    }


    /**
     * 通知送信対象の読み取れるFCM登録トークンとミマモリIDを削除
     * 削除対象；相手のユーザー情報
     * 削除値：自分のユーザーID
     */
    public fun deleteNotifyMimamoriList(
        userId: String,
        mimamoriId: String,
        completion: (Boolean) -> Unit) {
        val userRef = ref.child(AppUser.TABLE_NAME).child(userId)
        userRef.get()
            .addOnSuccessListener {
                val result = it.value ?: run { completion(false); return@addOnSuccessListener }
                val userDic = result as? Map <String, Any> ?: run {completion(false); return@addOnSuccessListener }

                val ids = userDic[AppUser.MIMAMORI_ID_LIST_KEY] as? MutableList<String>
                ids?.let {
                    it.removeAll { it == mimamoriId }
                    val value = mapOf(
                        // 重複したIDの場合は格納しないようにdistinct
                        AppUser.MIMAMORI_ID_LIST_KEY to it.toList()
                    )
                    userRef.updateChildren(value)
                }
                completion(true)
            }.addOnFailureListener {
                Log.d("Realtime Database", "データ取得エラー${it}")
                completion(false)
            }
    }


    /**
     * ミマモリ側のマモラレ情報を更新
     * 更新対象；相手のユーザー情報
     * 更新値：自分のユーザーIDをリストに追加 / CURRENT_MAMORAREを更新
     */
    public fun updateMamorareIDList(
        userId: String,
        mimamoriId: String,
        completion: (Boolean) -> Unit
    ) {
        // Firebaseに許可されていないパス文字列を除去
        val path = sanitizeFirebasePath(mimamoriId)
        val userRef = ref.child(AppUser.TABLE_NAME).child(path)
        userRef.get()
            .addOnSuccessListener {
                val result = it.value ?: run { completion(false); return@addOnSuccessListener }
                val userDic = result as? Map <String, Any> ?: run { completion(false); return@addOnSuccessListener }
                val ids = userDic[AppUser.MAMORARE_ID_LIST_KEY] as? MutableList<String>
                val value = mutableMapOf<String, Any>()
                ids?.let {
                    it.add(userId)
                    value.put(AppUser.MAMORARE_ID_LIST_KEY, it.distinct().toList())
                }?: run {
                    Log.d("Realtime Database", "新規追加")
                    value.put(AppUser.MAMORARE_ID_LIST_KEY, listOf(userId))
                }
                value.put(AppUser.CURRENT_MAMORARE_ID, userId)
                userRef.updateChildren(value)
                completion(true)
            }.addOnFailureListener {
                Log.d("Realtime Database", "データ取得エラー${it}")
                completion(false)
            }
    }

    /**
     * Firebase Database paths must not contain '.', '#', '$', '[', or ']'
     */
    private fun sanitizeFirebasePath(path: String): String {
        return path.replace(Regex("[.#$\\[\\]]"), "")
    }

    /**
     * ミマモリモード時に
     * 通知受信対象のマモラレIDを削除
     * 更新対象；自分のユーザー情報
     * 更新値
     *  1.カレントマモラレID
     *  2.マモラレIDリスト
     *  3.ミマモリIDリスト
     */
    public fun deleteMamorareList(
        userId: String,
        mamorareId: String,
        completion: (Boolean) -> Unit) {
        val userRef = ref.child(AppUser.TABLE_NAME).child(userId)
        userRef.get()
            .addOnSuccessListener {
                val result = it.value ?: run { completion(false); return@addOnSuccessListener }
                val userDic = result as? Map <String, Any> ?: run { completion(false); return@addOnSuccessListener }

                val ids = userDic[AppUser.MAMORARE_ID_LIST_KEY] as? MutableList<String>
                val currentMamorareId = userDic[AppUser.CURRENT_MAMORARE_ID] as? String
                ids?.let {
                    // マモラレIDリストから対象のIDを削除
                    ids.removeAll { it == mamorareId }

                    val value = mutableMapOf<String, Any?>()
                    // 現在設定済みのカレントマモラレIDが削除対象のマモラレIDと同じなら
                    if (currentMamorareId != null && currentMamorareId == mamorareId) {
                        // カレントマモラレIDを更新
                        val first = ids.firstOrNull()
                        first?.let {
                            // 一番先頭をセット
                            value.put(AppUser.CURRENT_MAMORARE_ID, first)
                        }?: run {
                            // 存在しないなら削除
                            value.put(AppUser.CURRENT_MAMORARE_ID, null)
                        }
                    }
                    // マモラレIDリストから対象のIDを削除
                    // 重複したIDの場合は格納しないようにdistinct
                    value.put(AppUser.MAMORARE_ID_LIST_KEY, ids.distinct().toList())
                    userRef.updateChildren(value)
                    completion(true)
                }

            }.addOnFailureListener {
                Log.d("Realtime Database", "データ取得エラー${it}")
                completion(false)
            }
    }


    /**
     * ユーザー情報取得
     */
    public fun getUserInfo(userId: String) {
        val userRef =  ref.child(AppUser.TABLE_NAME).child(userId)
        userRef.get()
            .addOnSuccessListener {
                val result = it.value ?: return@addOnSuccessListener
                val userDic = result as? Map <String, Any> ?: return@addOnSuccessListener

                val user = createAppUser(userDic, userId)
                storeMamorareUser(user)

            }.addOnFailureListener {
                Log.d("Realtime Database", "データ取得エラー${it}")
            }
    }


    /**
     * snapshotからAppUserを作成
     */
    private fun createAppUser(dic: Map<String, Any>, userId: String): AppUser {

        val json = dic[AppUser.NOTIFICATIONS_KEY] as? String ?: ""
        // JSONをList<AppNotify>に変換
        val notifications = JsonFormatterUtility.fromJson(json).sortedBy { it.time }.reversed()

        val user = AppUser(
            id = userId,
            name = dic[AppUser.NAME_KEY] as? String ?: "",
            fcmToken = dic[AppUser.FCM_TOKEN_KEY] as? String ?: "",
            isMamorare = dic[AppUser.IS_MAMORARE_KEY] as? Boolean ?: false,
            notifications = notifications,
            mimamoriIdList = dic[AppUser.MIMAMORI_ID_LIST_KEY] as? List<String> ?: emptyList(),
            currentMamorareId = dic[AppUser.CURRENT_MAMORARE_ID] as? String ?: "",
            mamorareIdList = dic[AppUser.MAMORARE_ID_LIST_KEY] as? List<String> ?: emptyList()
        )
        return user
    }


    /**
     * ユーザーが退会した際にユーザー情報を削除する
     */
    public fun deleteMyUser(myUser: AppUser) {
        val userRef = ref.child(AppUser.TABLE_NAME)
        userRef.child(myUser.id).removeValue()
        val removeItems: MutableMap<String, Any?> = mutableMapOf()
        // マモラレリストから自分のユーザー情報を削除する
        myUser.currentMimamoriList.forEach { user ->
            val mamorareIdList = user.mamorareIdList.toMutableList()
            mamorareIdList.removeAll { it == myUser.id  }
            // 自身のIDを観測対象にしている場合は削除
            if (user.currentMamorareId == myUser.id) {
                removeItems.put(user.id + "/" + AppUser.CURRENT_MAMORARE_ID, null)
            }
            // 自身のIDをリストから削除
            removeItems.put(user.id + "/" + AppUser.MAMORARE_ID_LIST_KEY, mamorareIdList)
        }

        // ミマモリリストから自分のユーザー情報を削除する
        myUser.currentMamorareList.forEach { user ->
            val index = user.mimamoriIdList.indexOfFirst { it == myUser.id }
            val mimamoriIdList = user.mimamoriIdList.toMutableList()
            if (index != -1) {
                mimamoriIdList.removeAt(index)
            }
            // 自身のIDをリストから削除
            removeItems.put(user.id + "/" + AppUser.MIMAMORI_ID_LIST_KEY, mimamoriIdList)
        }
        userRef.updateChildren(removeItems)
    }


    /**
     * マモラレの場合に自分のユーザー情報を観測する
     */
    public fun observeMyUserData(userId: String) {
        // 観測前に存在すれば停止
        stopMyUserObservers()

        val userRef = ref.child(AppUser.TABLE_NAME).child(userId)

        observeMyUserDataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val result = dataSnapshot.value ?: return
                val userDic = result as? Map <String, Any> ?: return
                val user = createAppUser(userDic, userId)
                storeMamorareUser(user)
                Log.d("Realtime Database", "観測MyUser情報変化： $user")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("Realtime Database", "キャンセル： ${databaseError.toException()}")
            }
        }
        observeMyUserDataListener?.let {
            userRef.addValueEventListener(it)
        }
    }

    /**
     * ミマモリの場合にマモラレ側のデータを観測
     * ここで観測するのはマモラレ側のデータだが更新するのは自身のユーザー情報
     */
    public fun observeMamorareData(mamorareId: String) {
        // 観測前に存在すれば停止
        stopMamorareObservers()

        val userRef = ref.child(AppUser.TABLE_NAME).child(mamorareId)

        observeMamorareDataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val result = dataSnapshot.value ?: return
                val userDic = result as? Map <String, Any> ?: return
                val mamorareUser = createAppUser(userDic, mamorareId)
                val myUser = _myAppUser.value ?: return
                val index =  myUser.currentMamorareList.indexOfFirst { it.id == mamorareId }
                oldNotifications = myUser.currentMamorareList.firstOrNull { it.id == mamorareId }?.notifications?.size ?: 0
                // カレントマモラレリストの中に存在するなら上書き格納
                if (index > -1 && index in myUser.currentMamorareList.indices) {
                    myUser.currentMamorareList.toMutableList()[index] = mamorareUser
                }
                // 自身のユーザーIDをマモラレとして観測対象にしている場合は更新しない
                if (myUser.id != mamorareId) {
                    _myAppUser.onNext(myUser)
                    Log.d("Realtime Database", "観測Mamorare情報変化： $myUser")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("Realtime Database", "キャンセル： ${databaseError.toException()}")
            }
        }
        observeMamorareDataListener?.let {
            userRef.addValueEventListener(it)
        }

    }

    /**
     * マモラレ/ミマモリリストの中にあるIDを元に[AppUser]をAppUserに格納
     */
    private fun storeMamorareUser(user: AppUser) {
        val userRef = ref.child(AppUser.TABLE_NAME)
        val users = mutableListOf<AppUser>()
        if (user.isMamorare) {
            // マモラレならミマモリリストを取得してAppUserに反映
            if (user.mimamoriIdList.isEmpty()) { _myAppUser.onNext(user); return }
            val count = AtomicInteger(user.mimamoriIdList.size)
            user.mimamoriIdList.forEach { mimamoriId ->
                userRef.child(mimamoriId).get()
                    .addOnSuccessListener {
                        val result = it.value ?: run { _myAppUser.onNext(user); return@addOnSuccessListener }
                        val userDic = result as? Map <String, Any> ?: run { _myAppUser.onNext(user); return@addOnSuccessListener }
                        val addUser = createAppUser(userDic, mimamoriId)
                        users.add(addUser)
                        if (count.decrementAndGet() == 0) {
                            user.currentMimamoriList = users
                            _myAppUser.onNext(user)
                        }
                    }.addOnFailureListener {
                        Log.d("Realtime Database", "データ取得エラー${it}")
                        _myAppUser.onNext(user)
                    }
                }
        } else {
            // ミマモリならマモラレリストを取得してAppUserに反映
            if (user.mamorareIdList.isEmpty()) { _myAppUser.onNext(user); return }
            val count = AtomicInteger(user.mamorareIdList.size)
            user.mamorareIdList.forEach { mamorareId ->
                userRef.child(mamorareId).get()
                    .addOnSuccessListener {
                        val result = it.value ?: run { _myAppUser.onNext(user); return@addOnSuccessListener }
                        val userDic = result as? Map <String, Any> ?: run { _myAppUser.onNext(user); return@addOnSuccessListener }
                        val addUser = createAppUser(userDic, mamorareId)
                        users.add(addUser)
                        if (count.decrementAndGet() == 0) {
                            user.currentMamorareList = users
                            _myAppUser.onNext(user)
                        }
                    }.addOnFailureListener {
                        Log.d("Realtime Database", "データ取得エラー${it}")
                        _myAppUser.onNext(user)
                    }
            }
        }
    }

    /** 全ての観測を停止 */
    public fun stopAllObservers() {
        // マモラレ対象の観測を停止
        stopMamorareObservers()
        // 自身の観測を停止
        stopMyUserObservers()
    }

    /**  自身の観測を停止 */
    private fun stopMyUserObservers() {
        observeMyUserDataListener?.let {
            ref.removeEventListener(it)
            observeMyUserDataListener = null
        }
    }

    /** マモラレ対象の観測を停止 */
    public fun stopMamorareObservers() {
        observeMamorareDataListener?.let {
            ref.removeEventListener(it)
            observeMamorareDataListener = null
        }
    }
}