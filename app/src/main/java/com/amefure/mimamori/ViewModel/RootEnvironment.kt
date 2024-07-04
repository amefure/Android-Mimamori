package com.amefure.mimamori.ViewModel

import android.app.Application
import android.util.Log
import com.amefure.mimamori.Repository.AppEnvironmentStore
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

class RootEnvironment(app: Application) : RootViewModel(app) {

    private var disposable = CompositeDisposable()

    /**
     * クラウドから取得したAppUser情報を観測開始
     */
    public fun observeMyUserData() {
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
                databaseRepository.updateNotifyMimamoriList(userId, mimamoriId) {
                    result ->
                    if (result) {
                        completion(true)
                    } else {
                        completion(false)
                    }
                }
            } else {
                completion(false)
            }
        }
    }
}