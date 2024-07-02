package com.amefure.mimamori.View.Setting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.amefure.mimamori.Model.AppUser
import com.amefure.mimamori.R
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.View.FBAuthentication.AuthActivity
import com.amefure.mimamori.ViewModel.AuthEnvironment
import com.amefure.mimamori.ViewModel.RootEnvironment
import com.amefure.mimamori.ViewModel.SettingViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class WithdrawalFragment : Fragment() {

    private val rootEnvironment: RootEnvironment by viewModels()
    private val viewModel: SettingViewModel by viewModels()
    private val authEnvironment: AuthEnvironment by viewModels()

    private var myAppUser: AppUser? = null
    private val disposable = CompositeDisposable()

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

        rootEnvironment.myAppUser.subscribeBy { user ->
            myAppUser = user
        }.addTo(disposable)
    }
    /**
     * ボタンクリックイベント登録
     * ユーザー情報観測
     */
    private fun setOnClickListener(view: View) {
        val withdrawalButton: Button = view.findViewById(R.id.withdrawal_button)
        withdrawalButton.setOnClickListener {
            showConfirmWithdrawalDialog()
        }
    }


    /** 最終退会確認ダイアログ表示 */
    private fun showConfirmWithdrawalDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_confirm_withdrawal),
            showPositive = true,
            showNegative = false,
            positionText = getString(R.string.setting_withdrawal_button)
        )
        dialog.setOnTappedListener(
            object : CustomNotifyDialogFragment.setOnTappedListener {
                override fun onPositiveButtonTapped() {
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
                override fun onNegativeButtonTapped() { }
            }
        )
        dialog.showNow(parentFragmentManager, "ConfirmWithdrawalDialog")
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
}

