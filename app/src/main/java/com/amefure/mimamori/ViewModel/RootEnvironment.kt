package com.amefure.mimamori.ViewModel

import android.app.Application
import android.util.Log
import com.amefure.mimamori.Repository.AppEnvironmentStore
import com.amefure.mimamori.Repository.DataStore.DataStoreRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.flow.Flow

class RootEnvironment(app: Application) : RootViewModel(app) {

    private var disposable = CompositeDisposable()
    private var isObserve = false

    /**
     * クラウドから取得したAppUser情報を観測開始
     */
    public fun observeMyUserData() {
        if (!isObserve) {
            // クラウドから取得したAppUser情報を観測
            databaseRepository.myAppUser.subscribe { user ->
                Log.d("Realtime Database", "USER：${user}")
                // 全体参照できるユーザー情報を公開
                AppEnvironmentStore.instance.myAppUser.onNext(user)
            }.addTo(disposable)

            // ローカルにあるユーザーIDを使用してクラウドを観測開始
            authRepository.getCurrentUser()?.uid?.let { userId ->
                Log.d("Realtime Database", "USERID：${userId}")
                databaseRepository.observeMyUserData(userId)
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