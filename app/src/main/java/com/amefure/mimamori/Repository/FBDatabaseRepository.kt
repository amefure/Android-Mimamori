package com.amefure.mimamori.Repository

import android.content.Context
import com.amefure.mimamori.Model.AppUser

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FBDatabaseRepository(context: Context) {
    private val ref = Firebase.database.reference

    public fun createUser(
        userId: String,
        name: String,
        token: String,
    ) {
        val usersInfo = mapOf<String,Any>(
            AppUser.NAME_KEY to name,
            AppUser.FCM_TOKEN_KEY to token,
            AppUser.IS_MAMORARE_KEY to true,

        )
        ref.child(AppUser.TABLE_NAME).child(userId).updateChildren(usersInfo)
    }
}