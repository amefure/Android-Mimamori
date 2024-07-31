package com.amefure.mimamori.View.FBAuthentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.amefure.mimamori.Utility.ValidationUtility
import com.amefure.mimamori.View.BaseFragment.BaseAuthFragment
import com.amefure.mimamori.View.BaseFragment.BaseInputFragment
import com.amefure.mimamori.View.Dialog.CustomLoadingDialogFragment.Companion.dismissLoadingDialog
import com.amefure.mimamori.View.Dialog.CustomLoadingDialogFragment.Companion.presentLoadingDialog
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.View.MainActivity
import com.amefure.mimamori.ViewModel.AuthEnvironment
import com.amefure.mimamori.ViewModel.RootEnvironment
import com.google.android.gms.common.SignInButton
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay

class AuthRootFragment : BaseAuthFragment() {

    private val authEnvironment: AuthEnvironment by viewModels()

    private var disposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auth_root, container, false)
    }


    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputName: EditText = view.findViewById(R.id.input_name)
        val inputEmail: EditText = view.findViewById(R.id.input_email)
        val inputPass: EditText = view.findViewById(R.id.input_password)

        val inputNameHiddenSpace: Space = view.findViewById(R.id.input_name_hidden_space)
        val forgetPasswordButtonHiddenSpace: Space = view.findViewById(R.id.forget_password_button_hidden_space)


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
            authEnvironment.isShowEntryViewFlag = !authEnvironment.isShowEntryViewFlag

            if (authEnvironment.isShowEntryViewFlag) {
                inputName.visibility = View.VISIBLE
                inputNameHiddenSpace.visibility = View.GONE
                forgetPasswordButton.visibility = View.GONE
                forgetPasswordButtonHiddenSpace.visibility = View.VISIBLE
                val newEntryStr = this.resources.getString(R.string.auth_new_entry)
                createButton.setText(newEntryStr)
            } else {
                inputName.visibility = View.GONE
                inputNameHiddenSpace.visibility = View.VISIBLE
                forgetPasswordButton.visibility = View.VISIBLE
                forgetPasswordButtonHiddenSpace.visibility = View.GONE
                val signInStr = this.resources.getString(R.string.auth_sign_in)
                createButton.setText(signInStr)
            }
        }

        createButton.setOnClickListener {
            closedKeyBoard()
            parentFragmentManager.presentLoadingDialog()
            val handler = Handler(Looper.getMainLooper())
            // 擬似的に1秒遅延
            handler.postDelayed({
                val name = inputName.text.toString()
                val email = inputEmail.text.toString()
                val pass = inputPass.text.toString()

                if (authEnvironment.isShowEntryViewFlag) {
                    // 新規作成
                    if (name.isEmpty() || !ValidationUtility.validateEmail(email) || pass.isEmpty()) {
                        parentFragmentManager.dismissLoadingDialog()
                        showFailedValidationDialog()
                        return@postDelayed
                    }
                    authEnvironment.createUserWithEmailAndPassword(name, email, pass)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                            onComplete = {
                                Log.d("Auth", "新規登録成功")
                                parentFragmentManager.dismissLoadingDialog()
                                // アプリメイン画面起動
                                startAppMainView()
                            },
                            onError = { error ->
                                parentFragmentManager.dismissLoadingDialog()
                                Log.e("Auth", "新規登録失敗" + error.toString())
                                showFailedAuthDialog(error.message.toString())
                            }
                        )
                        .addTo(disposable)
                } else {
                    // サインイン
                    if (!ValidationUtility.validateEmail(email) || pass.isEmpty()) {
                        parentFragmentManager.dismissLoadingDialog()
                        showFailedValidationDialog()
                        return@postDelayed
                    }
                    authEnvironment.signInWithEmailAndPassword(email,pass)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                            onComplete = {
                                Log.d("Auth", "サインイン成功")
                                parentFragmentManager.dismissLoadingDialog()
                                // アプリメイン画面起動
                                startAppMainView()
                            },
                            onError = { error ->
                                parentFragmentManager.dismissLoadingDialog()
                                Log.e("Auth", "サインイン失敗" + error.toString())
                                showFailedAuthDialog(error.message.toString())
                            }
                        )
                        .addTo(disposable)
                }
            }, 1000)

        }

        val googleSignInButton: SignInButton = view.findViewById(R.id.google_sign_in_button)
        googleSignInButton.setOnClickListener {
            val signInIntent = authEnvironment.getGoogleSignInClient().signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    /**
     *  アプリメイン画面起動
     */
    private fun startAppMainView() {
        val intent = Intent(this.requireContext(), MainActivity::class.java)
        startActivity(intent)
        this.requireActivity().finish()
    }


    /**
     *  Googleサインインランチャー
     */
    private var googleSignInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                authEnvironment.googleSignIn(it) {
                    if (it) {
                        // アプリメイン画面起動
                        startAppMainView()
                    }
                }
            }
        } else {
            Log.w("Auth", "サインインキャンセルまたは失敗")
        }
    }
}