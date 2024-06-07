package com.amefure.mimamori.View

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amefure.mimamori.R
import com.amefure.mimamori.View.FBAuthentication.AuthActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, AuthActivity::class.java)
        // intent.putExtra("key", "Test")
        startActivity(intent)


    }
}
