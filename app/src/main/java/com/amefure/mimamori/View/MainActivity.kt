package com.amefure.mimamori.View

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.amefure.mimamori.Model.AppUser
import com.amefure.mimamori.Model.myFcmToken
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.AppEnvironmentStore
import com.amefure.mimamori.Repository.FirebaseAuthRepository
import com.amefure.mimamori.View.FBAuthentication.AuthActivity
import com.amefure.mimamori.View.Main.MamorareFragment
import com.amefure.mimamori.View.Main.MimamoriFragment
import com.amefure.mimamori.View.Main.NotifyListAdapter
import com.amefure.mimamori.View.Utility.OneTouchHelperCallback
import com.amefure.mimamori.ViewModel.RootEnvironment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

class MainActivity : AppCompatActivity() {
    private var disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FCMトークン取得
        getFCMToken()

        // 許可ダイアログを表示
        permissionRequestLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

        //　サインイン済みなら
        if (FirebaseAuthRepository(this).getCurrentUser() == null) {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            subscribeMyAppUser()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    /** ユーザー情報観測 */
    private fun subscribeMyAppUser() {
        AppEnvironmentStore.instance.myAppUser
            .distinctUntilChanged()
            .subscribeBy { user ->
                Log.d("000000変化しよ","000000変化しよ" )
                val fragment = if (user.isMamorare) {
                    MamorareFragment()
                } else {
                    MimamoriFragment()
                }
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.main_frame, fragment)
                    commit()
                }
            }.addTo(disposable)
    }



    /**
     * パーミッション許可申請ダイアログを表示
     */
    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            // ダイアログの結果で処理を分岐
        }

    /**
     * Firebase Cloud Messagingのデバイス登録トークンを取得する
     */
    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d("FCM Token", "Error fetching FCM registration token: ${task.exception}")
                return@OnCompleteListener
            }
            val token = task.result
            myFcmToken = token
            Log.d("FCM Token", "FCM registration token:${token}")
        })
    }
}
