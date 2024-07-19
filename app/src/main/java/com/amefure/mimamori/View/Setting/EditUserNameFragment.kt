package com.amefure.mimamori.View.Setting

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.amefure.mimamori.Model.AuthProviderModel
import com.amefure.mimamori.Model.Key.AppArgKey
import com.amefure.mimamori.R
import com.amefure.mimamori.View.BaseFragment.BaseAuthFragment
import com.amefure.mimamori.View.BaseFragment.BaseInputFragment
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.ViewModel.AuthEnvironment
import com.amefure.mimamori.ViewModel.RootEnvironment
import com.amefure.mimamori.ViewModel.SettingViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class EditUserNameFragment : BaseAuthFragment() {

    private val rootEnvironment: RootEnvironment by viewModels()
    private val authEnvironment: AuthEnvironment by viewModels()
    private val viewModel: SettingViewModel by viewModels()
    private val disposable = CompositeDisposable()

    private var userId: String = ""
    private var userName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(AppArgKey.ARG_SIGN_IN_USER_ID_KEY) ?: ""
            userName = it.getString(AppArgKey.ARG_SIGN_IN_USER_NAME_KEY) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_user_name, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHeaderAction(view)

        val inputUserName: EditText = view.findViewById(R.id.input_user_name)
        val updateUserNameButton: Button = view.findViewById(R.id.update_user_name_button)
        // 初期値にユーザー名をセット
        inputUserName.setText(userName)

        updateUserNameButton.setOnClickListener {
            closedKeyBoard()
            val name = inputUserName.text.toString()
            if (name.isEmpty()) {
                showValidateUserNameDialog()
            } else {
                authEnvironment.updateProfileName(name)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onComplete = {
                            viewModel.updateUserName(userId, name)
                            showSuccessUserNameDialog()
                        },
                        onError = { error ->
                            Log.e("Auth", error.toString())
                            showFailedAuthDialog(getString(R.string.dialog_auth_failed_update_user_info))
                        }
                    )
                    .addTo(disposable)
            }
        }
    }


    /** ユーザー名変更成功ダイアログ */
    private fun showSuccessUserNameDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_auth_success_update_user_info),
            showPositive = true,
            showNegative = false
        )
        dialog.showNow(parentFragmentManager, "SuccessUserNameDialog")
    }

    /** ユーザー名変更失敗ダイアログ */
    private fun showValidateUserNameDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_edit_user_name_validation_input),
            showPositive = true,
            showNegative = false
        )
        dialog.showNow(parentFragmentManager, "ValidateUserNameDialog")
    }

    /**
     * ヘッダーボタンセットアップ
     * [LeftButton]：backButton
     * [RightButton]：非表示(GONE)
     */
    private fun setUpHeaderAction(view: View) {
        val headerView: ConstraintLayout = view.findViewById(R.id.include_header)

        val isMamorare = rootEnvironment.getIsMamorare()
        val headerTitle: TextView = view.findViewById(R.id.header_title)
        headerTitle.text = if (isMamorare) getString(R.string.mamorare) else getString(R.string.mimamori)

        val leftButton: ImageButton = headerView.findViewById(R.id.left_button)
        leftButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val rightButton: ImageButton = headerView.findViewById(R.id.right_button)
        rightButton.visibility = View.GONE
    }

    companion object {
        @JvmStatic
        fun newInstance(userId: String, userName: String) =
            EditUserNameFragment().apply {
                arguments = Bundle().apply {
                    putString(AppArgKey.ARG_SIGN_IN_USER_ID_KEY, userId)
                    putString(AppArgKey.ARG_SIGN_IN_USER_NAME_KEY, userName)
                }
            }
    }
}