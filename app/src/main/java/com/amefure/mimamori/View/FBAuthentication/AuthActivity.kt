package com.amefure.mimamori.View.FBAuthentication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amefure.mimamori.R

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // 認証ルート画面を表示
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.auth_main_frame, AuthRootFragment())
            commit()
        }
    }
}