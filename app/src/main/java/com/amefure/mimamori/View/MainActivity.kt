package com.amefure.mimamori.View

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.amefure.mimamori.Model.myFcmToken
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.DataStore.DataStoreRepository
import com.amefure.mimamori.Repository.FirebaseAuthRepository
import com.amefure.mimamori.View.Dialog.CustomLoadingDialogFragment.Companion.presentLoadingDialog
import com.amefure.mimamori.View.FBAuthentication.AuthActivity
import com.amefure.mimamori.View.Main.MainRootFragment
import com.amefure.mimamori.View.Onboarding.Onboarding1Fragment
import com.amefure.mimamori.ViewModel.RootApplication
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.disposables.CompositeDisposable

class MainActivity : AppCompatActivity() {
    private var disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FCMトークン取得
        getFCMToken()

        // 許可ダイアログを表示
        permissionRequestLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

        if (RootApplication.instance.authRepository.getCurrentUser() == null) {
            //　未サインインなら
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            //　サインイン済みなら
            if (RootApplication.instance.dataStoreRepository.getPreference(DataStoreRepository.APP_INITIAL_BOOT_FLAG, false)) {
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.main_frame, MainRootFragment())
                    commit()
                }
            } else {
                // 初回起動されていないならオンボーディング開始
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.main_frame, Onboarding1Fragment())
                    commit()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
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
