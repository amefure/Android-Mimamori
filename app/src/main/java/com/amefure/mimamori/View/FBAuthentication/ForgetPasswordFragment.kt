package com.amefure.mimamori.View.FBAuthentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.viewModels
import com.amefure.mimamori.R
import com.amefure.mimamori.Utility.ValidationUtility
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.ViewModel.AuthEnvironment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers


class ForgetPasswordFragment : Fragment() {

    private val authEnvironment: AuthEnvironment by viewModels()

    private val disposable: CompositeDisposable = CompositeDisposable()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forget_password, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val inputEmail: EditText = view.findViewById(R.id.input_email)
        val sendButton: Button = view.findViewById(R.id.send_mail_button)

        sendButton.setOnClickListener {
            val email = inputEmail.text.toString()
            if (!ValidationUtility.validateEmail(email)) {
                showFailedValidationDialog()
                return@setOnClickListener
            }

            authEnvironment.sendPasswordReset(email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onComplete = {
                        // アプリメイン画面起動
                        showSuccessSendEmailDialog()
                    },
                    onError = { error ->
                        showFailedSendEmailDialog()
                    }
                )
                .addTo(disposable)
        }
    }

    /**
     *  入力バリデーションダイアログ表示
     */
    private fun showFailedValidationDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_auth_validation_input),
            showPositive = true,
            showNegative = false
        )
        dialog.show(parentFragmentManager, "FailedAuthValidationInput")
    }

    /**
     *  メール送信成功ダイアログ表示
     */
    private fun showSuccessSendEmailDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_auth_success_send_email),
            showPositive = true,
            showNegative = false
        )
        dialog.showNow(parentFragmentManager, "FailedAuthValidationInput")
    }


    /**
     *  メール送信失敗ダイアログ表示
     */
    private fun showFailedSendEmailDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_auth_failed_send_email),
            showPositive = true,
            showNegative = false
        )
        dialog.showNow(parentFragmentManager, "FailedAuthValidationInput")
    }

}