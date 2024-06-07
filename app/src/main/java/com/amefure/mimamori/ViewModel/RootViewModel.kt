package com.amefure.mimamori.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel

abstract class RootViewModel(application: Application) : AndroidViewModel(application) {
    protected val authRepository = (application as RootApplication).authRepository
}