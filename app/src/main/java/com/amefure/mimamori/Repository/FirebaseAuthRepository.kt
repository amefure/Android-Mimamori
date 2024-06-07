package com.amefure.mimamori.Repository

import android.content.Context
import android.content.Intent
import android.util.Log
import com.amefure.mimamori.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.userProfileChangeRequest
import io.reactivex.Completable

class FirebaseAuthRepository(context: Context) {
    private val mAuth = FirebaseAuth.getInstance()
    public val mGoogleSignInClient: GoogleSignInClient

    init {
        mAuth.setLanguageCode("ja")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.FIREBASE_CLIENT_ID)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    // ------ Common ------

    /**
     *  カレントユーザー取得
     */
    public fun getCurrentUser(): FirebaseUser? {
        return mAuth.currentUser
    }

    /**
     *  サインアウト
     *  これはユーザーセッション情報をクリアするだけなので常に成功する
     */
    public fun signOut() {
        mAuth.signOut()
    }

    /**
     *  ユーザー情報編集
     */
    public fun updateProfile(user: FirebaseUser, profileUpdates: UserProfileChangeRequest): Completable {
        return Completable.create { emitter ->
            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        emitter.onComplete()
                    }
                }.addOnFailureListener { exception ->
                    emitter.onError(exception)
                }
        }

    }

    /**
     *  退会 & Appleアカウントは直呼び出し
     */
    public fun withdrawal(user: FirebaseUser): Completable {
        return Completable.create { emitter ->
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        emitter.onComplete()
                    }
                }.addOnFailureListener { exception ->
                    emitter.onError(exception)
                }
        }
    }

    // ------ Email/Password ------

    /**
     *  Email/Password
     *  新規登録
     */
    public fun createUserWithEmailAndPassword(email: String, pass: String): Completable {
        return Completable.create { emitter ->
            mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // アカウント作成成功
                        emitter.onComplete()
                    }
                    // ここでのエラー発火はRxのクラッシュになる(重複したonErrorの発火)
                    // taskが失敗に入る際はaddOnFailureListenerも呼ばれるため
                    // × emitter.onError(task.exception ?: Exception("Unknown error"))
                }
                .addOnFailureListener { exception ->
                    // アカウント作成失敗
                    emitter.onError(exception)
                }
        }
    }

    /**
     *  Email/Password
     *  サインイン
     */
    public fun signInWithEmailAndPassword(email: String, pass: String): Completable {
        return Completable.create { emitter ->
            mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        emitter.onComplete()
                    }
                }.addOnFailureListener { exception ->
                    emitter.onError(exception)
                }

        }
    }


    // ------ Google ------

    /**
     *  Google新規登録&サインイン
     *  IntentからGoogleアカウント情報を取得し
     *  クレデンシャルサインインを実行
     */
    public fun googleSignIn(data: Intent) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                credentialGoogleSignIn(token)
            }
        } catch (e: ApiException) {
            Log.d("Auth", "アカウント情報取得失敗 エラー：", e)
        }
    }

    /**
     *  クレデンシャル(認証情報)でサインインを試みる
     */
    private fun credentialGoogleSignIn(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Auth", "Googleサインイン成功")
                } else {
                    Log.d("Auth", "Googleサインイン失敗", task.exception)
                }
            }

    }
}