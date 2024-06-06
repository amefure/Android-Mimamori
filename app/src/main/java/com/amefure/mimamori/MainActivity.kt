package com.amefure.mimamori

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
class MainActivity : AppCompatActivity() {

    private lateinit var repository: FirebaseAuthRepository
    private var disposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        repository = FirebaseAuthRepository(this)

        val createButton: Button = findViewById(R.id.create)
        createButton.setOnClickListener {
            repository.createUserWithEmailAndPassword("ame8network@gmail.com", "12345678")
                .subscribeBy(
                    onComplete = {
                        Log.d("Auth", "新規登録成功")
                    },
                    onError = { error ->
                        Log.e("Auth", error.toString())
                    }
                )
                .addTo(disposable)
        }
        val signInButton: Button = findViewById(R.id.signIn)
        signInButton.setOnClickListener {
            val currentUser = repository.getCurrentUser()
            if (currentUser == null) {
                repository.signInWithEmailAndPassword("ame8network@gmail.com", "12345678")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onComplete = {
                            Log.d("Auth", "サインイン成功")
                        },
                        onError = { error ->
                            Log.e("Auth", error.toString())
                        }
                    )
                    .addTo(disposable)

            } else {
                Log.d("Auth", "サインインしてるよ")
                repository.updateProfileName(currentUser, "Test")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onComplete = {
                            Log.d("Auth", "名前変更")
                        },
                        onError = { error ->
                            Log.e("Auth", error.toString())
                        }
                    )
                    .addTo(disposable)

            }

        }

        val signOutButton: Button = findViewById(R.id.signOut)
        signOutButton.setOnClickListener {
            repository.signOut()
//            val currentUser = repository.getCurrentUser()
//            currentUser?.let {
//                repository.withdrawal(it)
//                    .subscribeBy(
//                        onComplete = {},
//                        onError = {}
//                    )
//                    .addTo(disposable)
//            }
        }

        val googleSignInButton: SignInButton = findViewById(R.id.google_sign_in_button)
        googleSignInButton.setOnClickListener {
            val signInIntent = repository.mGoogleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private var googleSignInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { repository.googleSignIn(it) }
        } else {
            Log.w("Auth", "サインインキャンセルまたは失敗")
        }
    }
}

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
    public fun updateProfileName(user: FirebaseUser, name: String): Completable {
        val profileUpdates = userProfileChangeRequest {
            displayName = name
        }

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