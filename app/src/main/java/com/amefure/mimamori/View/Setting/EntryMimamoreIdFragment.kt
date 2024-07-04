package com.amefure.mimamori.View.Setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.AppEnvironmentStore
import com.amefure.mimamori.View.BaseFragment.BaseInputFragment
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.ViewModel.RootEnvironment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

class EntryMimamoreIdFragment : BaseInputFragment() {

    private val rootEnvironment: RootEnvironment by viewModels()
    private var compositeDisposable = CompositeDisposable()
    private var userId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_entry_mimamore_id, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHeaderAction(view)

        var inputMimamoriId: EditText = view.findViewById(R.id.input_mimamori_id)
        var entryIdButton: Button = view.findViewById(R.id.entry_id_button)

        AppEnvironmentStore.instance.myAppUser.subscribeBy { user ->
            userId = user.id
        }.addTo(compositeDisposable)

        // ミマモリID登録
        entryIdButton.setOnClickListener {
            closedKeyBoard()
            if (inputMimamoriId.text.toString().isEmpty()) {
                showIdValidationDialog()
                return@setOnClickListener
            } else {
                // 入力されたミマモリIDのユーザーを取得して登録
                rootEnvironment.entryMimamoriUser(inputMimamoriId.text.toString(), userId) { result ->
                    if (result) {
                        showSuccessEntryDialog()
                    } else {
                        showIdValidationDialog()
                    }
                }
            }
        }
    }

    /**
     *  ミマモリID登録成功ダイアログ表示
     */
    private fun showSuccessEntryDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_success_entry_mimamori),
            showPositive = true,
            showNegative = false
        )
        dialog.show(parentFragmentManager, "SuccessEntryDialog")
    }

    /**
     *  ID入力バリデーションダイアログ表示
     */
    private fun showIdValidationDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_not_exist_mimamoriid),
            showPositive = true,
            showNegative = false
        )
        dialog.show(parentFragmentManager, "IdValidationDialog")
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