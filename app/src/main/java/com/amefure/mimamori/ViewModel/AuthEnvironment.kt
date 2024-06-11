package com.amefure.mimamori.ViewModel

import android.app.Application
import android.content.Intent
import android.util.Log
import com.amefure.mimamori.Model.myFcmToken
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class AuthEnvironment(app: Application) : RootViewModel(app) {

    //　表示しているのが新規登録画面かどうか
    public var isShowEntryViewFlag = true

    private var disposable: CompositeDisposable = CompositeDisposable()

    /**
     *  カレントユーザー取得
     */
    public fun getGoogleSignInClient(): GoogleSignInClient {
        return authRepository.mGoogleSignInClient
    }


    // ------ Common ------

    /**
     *  カレントユーザー取得
     */
    public fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

    /**
     *  サインアウト
     *  これはユーザーセッション情報をクリアするだけなので常に成功する
     */
    public fun signOut() {
        authRepository.signOut()
    }

    /**
     *  ユーザー情報編集
     */
    public fun updateProfileName(name: String): Completable {
        val user = getCurrentUser() ?: return Completable.error(Error("User NotFound"))
        val profileUpdates = userProfileChangeRequest {
            displayName = name
        }
        return authRepository.updateProfile(user, profileUpdates)
    }

    /**
     *  退会 & Appleアカウントは直呼び出し
     */
    public fun withdrawal(): Completable {
        val user = getCurrentUser() ?: return Completable.error(Error("User NotFound"))
        return authRepository.withdrawal(user)

    }

    // ------ Email/Password ------

    /**
     *  Email/Password
     *  新規登録 & 名前設定
     */
    public fun createUserWithEmailAndPassword(name: String, email: String, pass: String): Completable {
        return Completable.create { emitter ->
            authRepository.createUserWithEmailAndPassword(email, pass)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onComplete = {
                        updateProfileName(name)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                onComplete = {
                                    createUserForCloud()
                                    emitter.onComplete()
                                },
                                onError = { error ->
                                    // ユーザー情報の編集に失敗しても成功を返す
                                    Log.e("Auth", "ユーザー情報編集失敗")
                                    createUserForCloud()
                                    emitter.onComplete()
                                }
                            )
                            .addTo(disposable)
                    },
                    onError = { error ->
                        emitter.onError(Error("作成失敗"))
                    }
                )
                .addTo(disposable)
        }
    }

    /**
     *  Email/Password
     *  サインイン
     */
    public fun signInWithEmailAndPassword(email: String, pass: String): Completable {
        return Completable.create { emitter ->
            authRepository.signInWithEmailAndPassword(email, pass)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onComplete = {
                        createUserForCloud()
                        emitter.onComplete()
                    },
                    onError = { error ->
                        emitter.onError(Error("サインイン失敗"))
                    }
                )
                .addTo(disposable)
        }
    }

    /**
     *  Email/Password
     *  パスワード忘れのための救済メール送信
     */
    public fun sendPasswordReset(email: String): Completable {
        return authRepository.sendPasswordReset(email)
    }


    // ------ Google ------

    /**
     *  Google新規登録&サインイン
     *  IntentからGoogleアカウント情報を取得し
     *  クレデンシャルサインインを実行
     */
    public fun googleSignIn(data: Intent) {
        authRepository.googleSignIn(data)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    createUserForCloud()
                },
                onError = { error ->
                    Log.e("Auth", "ユーザー情報編集失敗${error}")
                }
            )
            .addTo(disposable)

    }


    /**
     * クラウドにユーザー初期情報を登録する
     */
    private fun createUserForCloud() {
        val user = getCurrentUser() ?: return
        databaseRepository.createUser(
            userId = user.uid,
            name = user.displayName ?: "",
            myFcmToken
        )
    }

}