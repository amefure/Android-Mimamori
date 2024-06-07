package com.amefure.mimamori.ViewModel

import android.app.Application
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import io.reactivex.Completable

class AuthViewModel(app: Application) : RootViewModel(app) {

    //　表示しているのが新規登録画面かどうか
    public var isShowEntryViewFlag = true

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
    private fun getCurrentUser(): FirebaseUser? {
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
     *  新規登録
     */
    public fun createUserWithEmailAndPassword(email: String, pass: String): Completable {
        return authRepository.createUserWithEmailAndPassword(email, pass)
    }

    /**
     *  Email/Password
     *  サインイン
     */
    public fun signInWithEmailAndPassword(email: String, pass: String): Completable {
        return signInWithEmailAndPassword(email, pass)
    }


    // ------ Google ------

    /**
     *  Google新規登録&サインイン
     *  IntentからGoogleアカウント情報を取得し
     *  クレデンシャルサインインを実行
     */
    public fun googleSignIn(data: Intent) {
        authRepository.googleSignIn(data)
    }
}