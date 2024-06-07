package com.amefure.mimamori.View

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.FirebaseAuthRepository
import com.amefure.mimamori.View.FBAuthentication.AuthActivity
import com.amefure.mimamori.View.FBAuthentication.AuthRootFragment
import com.amefure.mimamori.View.Main.MamorareFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //　サインイン済みなら
        if (FirebaseAuthRepository(this).getCurrentUser() == null) {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.main_frame, MamorareFragment())
                commit()
            }
        }
    }
}
