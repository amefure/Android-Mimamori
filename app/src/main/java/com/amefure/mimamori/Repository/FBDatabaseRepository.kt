package com.amefure.mimamori.Repository

import android.util.Log
import com.amefure.mimamori.Model.AppNotify
import com.amefure.mimamori.Model.AppUser
import com.amefure.mimamori.Utility.JsonFormatterUtility
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FBDatabaseRepository() {
    private val ref = Firebase.database.reference

    // 自分のユーザー情報
    private val _myAppUser = BehaviorSubject.createDefault(AppUser.demoUser())
    val myAppUser: Observable<AppUser> = _myAppUser.hide()

    private var oldNotifications: Int = 0

    private var observeMyUserData: ValueEventListener? = null
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
        var usersInfo: MutableMap<String, Any> = mutableMapOf()
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

                var ids = userDic[AppUser.MIMAMORI_ID_LIST_KEY] as? MutableList<String>
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
     * 削除対象；自分のユーザー情報
     * 削除値：相手のユーザーID
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

                var ids = userDic[AppUser.MIMAMORI_ID_LIST_KEY] as? MutableList<String>
                ids?.let {
                    it.removeAll { it == mimamoriId }
                    val value = mapOf(
                        // 重複したIDの場合は格納しないようにdistinct
                        AppUser.MIMAMORI_ID_LIST_KEY to it.toList()
                    )
                    userRef.updateChildren(value)
                }
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
        var path = sanitizeFirebasePath(mimamoriId)
        val userRef = ref.child(AppUser.TABLE_NAME).child(path)
        userRef.get()
            .addOnSuccessListener {
                val result = it.value ?: run { completion(false); return@addOnSuccessListener }
                val userDic = result as? Map <String, Any> ?: run { completion(false); return@addOnSuccessListener }
                var ids = userDic[AppUser.MAMORARE_ID_LIST_KEY] as? MutableList<String>
                ids?.let {
                    it.add(userId)
                    val value = mapOf(
                        // 重複したIDの場合は格納しないようにdistinct
                        AppUser.MAMORARE_ID_LIST_KEY to it.distinct().toList()
                    )
                    userRef.updateChildren(value)
                }?: run {
                    Log.d("Realtime Database", "追加")
                    val value = mapOf(
                        AppUser.MAMORARE_ID_LIST_KEY to listOf(userId)
                    )
                    userRef.updateChildren(value)
                }
                userRef.child(AppUser.CURRENT_MAMORARE_ID).setValue(userId)
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
     * 通知送信対象の読み取れるFCM登録トークンとミマモリIDを削除
     * 更新対象；自分のユーザー情報
     * 更新値：相手のユーザーIDとFCMトークン
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

                var ids = userDic[AppUser.MAMORARE_ID_LIST_KEY] as? MutableList<String>
                ids?.let {
                    it.removeAll { it == mamorareId }
                    val value = mapOf(
                        // 重複したIDの場合は格納しないようにdistinct
                        AppUser.MAMORARE_ID_LIST_KEY to it.toList()
                    )
                    userRef.updateChildren(value)

                    val first = it.firstOrNull()
                    first?.let {
                        // 一番先頭をセット
                        userRef.child(AppUser.CURRENT_MAMORARE_ID).setValue(first)
                    }?: run {
                        // 存在しないなら削除
                        userRef.child(AppUser.CURRENT_MAMORARE_ID).removeValue()
                    }
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
        var removeItems: MutableMap<String, Any?> = mutableMapOf()
        // マモラレリストから自分のユーザー情報を削除する
        myUser.currentMimamoriList.forEach { user ->
            var mamorareIdList = user.mamorareIdList.toMutableList()
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
            var mimamoriIdList = user.mimamoriIdList.toMutableList()
            mimamoriIdList.removeAt(index)
            // 自身のIDをリストから削除
            removeItems.put(user.id + "/" + AppUser.MIMAMORI_ID_LIST_KEY, mimamoriIdList)

        }
        userRef.updateChildren(removeItems)
    }


    /**
     * マモラレの場合に自分のユーザー情報を観測する
     */
    public fun observeMyUserData(userId: String) {
        var userRef = ref.child(AppUser.TABLE_NAME).child(userId)

        observeMyUserData = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val result = dataSnapshot.value ?: return
                val userDic = result as? Map <String, Any> ?: return
                val user = createAppUser(userDic, userId)
                storeMamorareUser(user)
                Log.d("Realtime Database", "取得した値： $user")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("Realtime Database", "キャンセル： ${databaseError.toException()}")
            }
        }
        observeMyUserData?.let {
            userRef.addValueEventListener(it)
        }
    }

    /**
     * ミマモリの場合にマモラレ側のデータを観測
     * ここで観測するのはマモラレ側のデータだが更新するのは自身のユーザー情報
     */
    public fun observeMamorareData(mamorareId: String) {
        val userRef = ref.child(AppUser.TABLE_NAME).child(mamorareId)

        observeMamorareDataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val result = dataSnapshot.value ?: return
                val userDic = result as? Map <String, Any> ?: return
                var mamorareUser = createAppUser(userDic, mamorareId)
                val myUser = _myAppUser.value?:return
                val index =  myUser.currentMamorareList.indexOfFirst { it.id == mamorareId }
                oldNotifications = myUser.currentMamorareList.firstOrNull { it.id == mamorareId }?.notifications?.size ?: 0
                myUser.currentMamorareList.toMutableList()[index] = mamorareUser
                _myAppUser.onNext(myUser)
                Log.d("Realtime Database", "取得した値： $myUser")
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
        var users = mutableListOf<AppUser>()
        var index = 0
        if (user.isMamorare) {
            if (user.mimamoriIdList.isEmpty()) { _myAppUser.onNext(user); return }
            user.mimamoriIdList.forEach { mimamoriId ->
                userRef.child(mimamoriId).get()
                    .addOnSuccessListener {
                        index += 1
                        val result = it.value ?: run { _myAppUser.onNext(user); return@addOnSuccessListener }
                        val userDic = result as? Map <String, Any> ?: run { _myAppUser.onNext(user); return@addOnSuccessListener }
                        val addUser = createAppUser(userDic, mimamoriId)
                        users.add(addUser)
                        if (index == user.mimamoriIdList.size) {
                            user.currentMimamoriList = users
                            _myAppUser.onNext(user)
                        }
                    }.addOnFailureListener {
                        Log.d("Realtime Database", "データ取得エラー${it}")
                        _myAppUser.onNext(user)
                    }
                }
        } else {
        if (user.mamorareIdList.isEmpty()) { _myAppUser.onNext(user); return }
            user.mamorareIdList.forEach { mamorareId ->
                userRef.child(mamorareId).get()
                    .addOnSuccessListener {
                        index += 1
                        val result = it.value ?: run { _myAppUser.onNext(user); return@addOnSuccessListener }
                        val userDic = result as? Map <String, Any> ?: run { _myAppUser.onNext(user); return@addOnSuccessListener }
                        val addUser = createAppUser(userDic, mamorareId)
                        users.add(addUser)
                        if (index == user.mamorareIdList.size) {
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

    /**
     * 全ての観測を停止
     */
    public fun stopObservers() {
        observeMamorareDataListener?.let {
            ref.removeEventListener(it)
        }

        observeMyUserData?.let {
            ref.removeEventListener(it)
        }
    }
}