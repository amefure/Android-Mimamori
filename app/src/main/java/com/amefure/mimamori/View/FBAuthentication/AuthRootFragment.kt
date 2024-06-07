package com.amefure.mimamori.View.FBAuthentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Space
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.amefure.mimamori.R
import com.amefure.mimamori.ViewModel.AuthViewModel
import com.google.android.gms.common.SignInButton
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

class AuthRootFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private var disposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auth_root, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputName: EditText = view.findViewById(R.id.input_name)
        val inputEmail: EditText = view.findViewById(R.id.input_email)
        val inputPass: EditText = view.findViewById(R.id.input_password)

        val inputNameHiddenSpace: Space = view.findViewById(R.id.input_name_hidden_space)

        val createButton: Button = view.findViewById(R.id.email_sign_in_button)
        val forgetPasswordButton: Button = view.findViewById(R.id.forget_password_button)
        val switchButton: Button = view.findViewById(R.id.switch_button)

        // パスワードを忘れた画面へ遷移
        forgetPasswordButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.auth_main_frame, ForgetPasswordFragment())
                addToBackStack(null)
                commit()
            }
        }

        // 新規登録/サインイン画面の切り替え
        switchButton.setOnClickListener {
            authViewModel.isShowEntryViewFlag = !authViewModel.isShowEntryViewFlag

            if (authViewModel.isShowEntryViewFlag) {
                inputName.visibility = View.VISIBLE
                inputNameHiddenSpace.visibility = View.GONE
                val newEntryStr = this.resources.getString(R.string.auth_new_entry)
                createButton.setText(newEntryStr)
            } else {
                inputName.visibility = View.GONE
                inputNameHiddenSpace.visibility = View.VISIBLE
                val signInStr = this.resources.getString(R.string.auth_sign_in)
                createButton.setText(signInStr)
            }

        }



        createButton.setOnClickListener {
            val email = inputEmail.text.toString()
            val pass = inputPass.text.toString()
            authViewModel.createUserWithEmailAndPassword(email, pass)
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
//        val signInButton: Button = findViewById(R.id)
//        signInButton.setOnClickListener {
//            val currentUser = repository.getCurrentUser()
//            if (currentUser == null) {
//                repository.signInWithEmailAndPassword("ame8network@gmail.com", "12345678")
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribeBy(
//                        onComplete = {
//                            Log.d("Auth", "サインイン成功")
//                        },
//                        onError = { error ->
//                            Log.e("Auth", error.toString())
//                        }
//                    )
//                    .addTo(disposable)
//
//            } else {
//                Log.d("Auth", "サインインしてるよ")
//                repository.updateProfileName(currentUser, "Test")
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribeBy(
//                        onComplete = {
//                            Log.d("Auth", "名前変更")
//                        },
//                        onError = { error ->
//                            Log.e("Auth", error.toString())
//                        }
//                    )
//                    .addTo(disposable)
//
//            }
//
//        }

//        val signOutButton: Button = findViewById(R.id.signOut)
//        signOutButton.setOnClickListener {
//            repository.signOut()
//            val currentUser = repository.getCurrentUser()
//            currentUser?.let {
//                repository.withdrawal(it)
//                    .subscribeBy(
//                        onComplete = {},
//                        onError = {}
//                    )
//                    .addTo(disposable)
//            }
//        }

        val googleSignInButton: SignInButton = view.findViewById(R.id.google_sign_in_button)
        googleSignInButton.setOnClickListener {
            val signInIntent = authViewModel.getGoogleSignInClient().signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private var googleSignInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { authViewModel.googleSignIn(it) }
        } else {
            Log.w("Auth", "サインインキャンセルまたは失敗")
        }
    }
}