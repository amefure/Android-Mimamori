package com.amefure.mimamori.View.Setting

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.amefure.mimamori.Model.AppUser
import com.amefure.mimamori.Model.AuthProviderModel
import com.amefure.mimamori.Model.Key.AppArgKey
import com.amefure.mimamori.R
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.View.FBAuthentication.AuthActivity
import com.amefure.mimamori.ViewModel.AuthEnvironment
import com.amefure.mimamori.ViewModel.RootEnvironment
import com.amefure.mimamori.ViewModel.SettingViewModel
import com.google.android.gms.common.SignInButton
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class WithdrawalFragment : Fragment() {

    private val rootEnvironment: RootEnvironment by viewModels()
    private val viewModel: SettingViewModel by viewModels()
    private val authEnvironment: AuthEnvironment by viewModels()

    private var myAppUser: AppUser? = null
    private var provider: AuthProviderModel = AuthProviderModel.NONE
    private val disposable = CompositeDisposable()

    private lateinit var inputPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val name = it.getString(AppArgKey.ARG_SIGN_IN_PROVIDER_KEY) ?: AuthProviderModel.NONE.name
            try {
                provider = AuthProviderModel.valueOf(name)
            } catch (e: IllegalArgumentException) {

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_withdrawal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHeaderAction(view)
        setOnClickListener(view)

        inputPassword = view.findViewById(R.id.input_password)

        when(provider) {
            AuthProviderModel.NONE -> { }
            AuthProviderModel.EMAIL -> {
                val inputPasswordDesc: TextView = view.findViewById(R.id.input_password_desc)
                inputPasswordDesc.visibility = View.VISIBLE
                inputPassword.visibility = View.VISIBLE
            }
            AuthProviderModel.APPLE -> { }
            AuthProviderModel.GOOGLE -> { }
        }

        rootEnvironment.myAppUser.subscribeBy { user ->
            myAppUser = user
        }.addTo(disposable)

        val googleSignInButton: SignInButton = view.findViewById(R.id.google_sign_in_button)
        googleSignInButton.setOnClickListener {
            val signInIntent = authEnvironment.getGoogleSignInClient().signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }
    /**
     * ボタンクリックイベント登録
     * ユーザー情報観測
     */
    private fun setOnClickListener(view: View) {
        val withdrawalButton: Button = view.findViewById(R.id.withdrawal_button)
        val bottomSheetLayout: LinearLayout = view.findViewById(R.id.bottom_sheet_layout)
        val behavior = BottomSheetBehavior.from(bottomSheetLayout)

        withdrawalButton.setOnClickListener {
            // showConfirmWithdrawalDialog()
            when(provider) {
                AuthProviderModel.NONE -> { }
                AuthProviderModel.EMAIL -> {
                    if (!inputPassword.text.isEmpty()) {
                        authEnvironment.reAuthUser(inputPassword.text.toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                onComplete = {
                                    // 退会処理
                                    // withdrawal()
                                },
                                onError = { error ->
                                    Log.e("Auth", error.toString())
                                }
                            )
                            .addTo(disposable)
                    }
                }
                AuthProviderModel.APPLE -> {
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                AuthProviderModel.GOOGLE -> {
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }

        // 最初は非表示に設定する
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    /** 退会処理 */
    private fun withdrawal() {
        myAppUser?.let {
            authEnvironment.withdrawal()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onComplete = {
                        // アプリメイン画面起動
                        viewModel.deleteMyUser(it)
                        startAuthRootView()
                    },
                    onError = { error ->
                        Log.e("Auth", error.toString())
                    }
                )
                .addTo(disposable)
        }
    }

    /** Googleサインインランチャー */
    private var googleSignInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                authEnvironment.googleReAuthUser(it) {
                    if (it) {
                        // 退会
                        withdrawal()
                    }
                }
            }
        } else {
            Log.w("Auth", "再認証キャンセルまたは失敗")
        }
    }

    /**
     *  サインイン画面起動
     */
    private fun startAuthRootView() {
        val intent = Intent(this.requireContext(), AuthActivity::class.java)
        startActivity(intent)
        this.requireActivity().finish()
    }

    /**
     * ヘッダーボタンセットアップ
     * [LeftButton]：backButton
     * [RightButton]：非表示(GONE)
     */
    private fun setUpHeaderAction(view: View) {
        val headerView: ConstraintLayout = view.findViewById(R.id.include_header)

        val leftButton: ImageButton = headerView.findViewById(R.id.left_button)
        leftButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val rightButton: ImageButton = headerView.findViewById(R.id.right_button)
        rightButton.visibility = View.GONE
    }

    /**
     * 引数を渡すため
     * シングルトンインスタンス生成
     */
    companion object {
        @JvmStatic
        fun newInstance(provider: String) =
            WithdrawalFragment().apply {
                arguments = Bundle().apply {
                    putString(AppArgKey.ARG_SIGN_IN_PROVIDER_KEY, provider)
                }
            }
    }
}

