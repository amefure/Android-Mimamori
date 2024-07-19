package com.amefure.mimamori.Repository

import com.amefure.mimamori.Model.AppUser
import io.reactivex.subjects.BehaviorSubject


class AppEnvironmentStore {

    companion object {
       public val instance = AppEnvironmentStore()
    }

    // アプリ全体で参照できる自身のユーザー情報
    public var myAppUser: BehaviorSubject<AppUser> = BehaviorSubject.createDefault(AppUser.demoUser())
}