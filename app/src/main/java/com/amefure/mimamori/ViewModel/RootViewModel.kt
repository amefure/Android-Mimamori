package com.amefure.mimamori.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel

abstract class RootViewModel(application: Application) : AndroidViewModel(application) {
    protected val authRepository = (application as RootApplication).authRepository
    protected val databaseRepository = (application as RootApplication).databaseRepository
    protected val dataStoreRepository = (application as RootApplication).dataStoreRepository
}