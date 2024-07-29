package com.amefure.mimamori.ViewModel

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.viewModelScope
import com.amefure.mimamori.Repository.AppEnvironmentStore
import com.amefure.mimamori.Repository.DataStore.DataStoreRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** シングルトン設計ではないのでプロパティに保持している値は呼び出している画面ごとに異なる */
class RootEnvironment(app: Application) : RootViewModel(app) {

    private var disposable = CompositeDisposable()
    private var isObserve = false

    /**
     * マモラレ対象を変更した際にマモラレ新規観測を開始するため
     * 観測を停止して次回の観測対象になるようにnullにする
     */
    public fun resetObserveMamorareId() {
        AppEnvironmentStore.instance.observeMamorareId = null
        databaseRepository.stopMamorareObservers()
    }

    /**
     * クラウドから取得したAppUser情報を観測開始
     */
    public fun observeMyUserData() {
        if (!isObserve) {
            // クラウドから取得したAppUser情報を観測
            databaseRepository.myAppUser.subscribe { user ->
                Log.d("Mimamori", "USER：${user}")
                // 全体参照できるユーザー情報を公開
                AppEnvironmentStore.instance.myAppUser.onNext(user)

                // 対象のマモラレIDが存在するならマモラレユーザー情報も観測
                if (!user.isMamorare && !user.currentMamorareId.isEmpty() && AppEnvironmentStore.instance.observeMamorareId == null) {
                    Log.d("Mimamori","マモラレ観測ID：${user.currentMamorareId}")
                    AppEnvironmentStore.instance.observeMamorareId = user.currentMamorareId
                    databaseRepository.observeMamorareData(user.currentMamorareId)
                }
            }.addTo(disposable)


            // ローカルに保持しているユーザーIDを使用してクラウドを観測開始
            // SIGNIN_USER_IDが変更されたことを検知する
            // 1. サインアウト時
            // 2. 別アカウントでサインイン時
            viewModelScope.launch {
                dataStoreRepository
                    .observePreference(DataStoreRepository.SIGNIN_USER_ID)
                    .collect { userId ->
                        userId ?: run {
                            // 空かつログインしているならローカル情報を更新
                            authRepository.getCurrentUser()?.let { user ->
                                viewModelScope.launch(Dispatchers.IO) {
                                    dataStoreRepository.savePreference(DataStoreRepository.SIGNIN_USER_ID, user.uid)
                                    dataStoreRepository.savePreference(DataStoreRepository.SIGNIN_USER_NAME, user.displayName ?: "")
                                }
                            }
                            return@collect
                        }
                        Log.d("Mimamori", "ローカルサインインIDが変化：${userId}")
                        if (!userId.isEmpty()) {
                            databaseRepository.getUserInfo(userId)
                            databaseRepository.observeMyUserData(userId)
                        } else {
                            databaseRepository.stopAllObservers()
                        }
                    }
            }
            isObserve = true
        }
    }

    /**
     * ミマモリIDを元に対象のミマモリユーザーをクラウドに登録
     * 1.自身のユーザーIDを取得
     * 2.ミマモリIDのユーザーのマモラレリストに自身のユーザーIDを追加
     * 3.取得したミマモリIDを自身のミマモリリストに追加
     * 4.自身の情報が更新されるのでデータが自動更新される
     */
    public fun entryMimamoriUser(mimamoriId: String, userId:String, completion: (Boolean) -> Unit) {
        databaseRepository.updateMamorareIDList(userId, mimamoriId) { result ->
            if (result) {
                databaseRepository.updateNotifyMimamoriList(userId, mimamoriId) { result ->
                    completion(result)
                }
            } else {
                completion(false)
            }
        }
    }

    /** ローカルに保存しているマモラレかどうかを取得 */
    public fun getIsMamorare(): Boolean = dataStoreRepository.getPreference(DataStoreRepository.IS_MAMORARE, true)

    /** ローカルに保存しているマモラレかどうかを観測 */
    public fun observeIsMamorare(): Flow<Boolean?> = dataStoreRepository.observePreference(DataStoreRepository.IS_MAMORARE)
}