package com.amefure.mimamori.View.Onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.AppEnvironmentStore
import com.amefure.mimamori.Utility.ShareTextUtility
import com.amefure.mimamori.View.BaseFragment.BaseInputFragment
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.View.Main.MainRootFragment
import com.amefure.mimamori.ViewModel.RootEnvironment
import com.amefure.mimamori.ViewModel.SelectModeViewModel


class Onboarding3Fragment : BaseInputFragment() {

    private val viewModel: SelectModeViewModel by viewModels()
    private val rootEnvironment: RootEnvironment by viewModels()
    private var isMamorare: Boolean = true
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isMamorare = it.getBoolean(ARG_IS_MAMORARE_KEY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = rootEnvironment.getSignInUserId()

        showSelectIsMamorareView(view)
    }

    private fun showSelectIsMamorareView(view: View) {
        // マモラレ/ミマモリレイアウト
        val mamorareLayout: LinearLayout = view.findViewById(R.id.mamorare_layout)
        val mimamoriLayout: LinearLayout = view.findViewById(R.id.mimamori_layout)
        if (isMamorare) {
            mamorareLayout.visibility = View.VISIBLE
            mimamoriLayout.visibility = View.GONE

        } else {
            mamorareLayout.visibility = View.GONE
            mimamoriLayout.visibility = View.VISIBLE

            val id: TextView = view.findViewById(R.id.miamori_id_label)
            val copyButton: Button = view.findViewById(R.id.copy_button)
            val shareButton: Button = view.findViewById(R.id.share_button)

            id.text = userId

            copyButton.setOnClickListener {
                ShareTextUtility.copyIdToClipboard(this.requireContext(), id.text.toString())
                showSuccessCopyIdDialog()
            }

            shareButton.setOnClickListener {
                ShareTextUtility.shareUserId(id.text.toString(), this.requireContext())
            }
        }

        val startButton: Button = view.findViewById(R.id.start_button)
        startButton.setOnClickListener {

            if (isMamorare) {
                // マモラレならミマモリIDを登録
                val inputMimamoriId: EditText = view.findViewById(R.id.input_mimamori_id)
                closedKeyBoard()
                if (!inputMimamoriId.text.toString().isEmpty()) {
                    // 空ではないなら登録
                    // 入力されたミマモリIDのユーザーを取得して登録
                    rootEnvironment.entryMimamoriUser(inputMimamoriId.text.toString(), userId) { result ->
                        if (result) {
                            // ダイアログ表示後にアプリメイン画面へ
                            showSuccessEntryDialog()
                        } else {
                            showIdValidationDialog()
                        }
                    }
                } else {
                    // 空ならここでは登録せずにアプリメイン画面へ
                    translationMainView()
                }
            } else {
                // ミマモリならメイン画面へ遷移
                translationMainView()
            }
        }
    }

    /** 「はじめる」ボタン押下後の最後にアプリメイン画面へ遷移する処理 */
    private fun translationMainView() {
        // オンボーディング終了フラグ
        rootEnvironment.setInitialBootFlag()
        // 遷移する前に選択されたモードに更新
        viewModel.selectIsMamorareMode(isMamorare)
        // アプリメイン画面へ遷移
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.main_frame, MainRootFragment())
            commit()
        }
    }

    // --------マモラレ側---------
    /** ミマモリID登録成功ダイアログ表示 */
    private fun showSuccessEntryDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_success_entry_mimamori),
            showPositive = true,
            showNegative = false
        )
        dialog.setOnTappedListener(
            object : CustomNotifyDialogFragment.setOnTappedListener {
                override fun onPositiveButtonTapped() {
                    // メイン画面へ遷移
                    translationMainView()
                }
                override fun onNegativeButtonTapped() { }
            }
        )
        dialog.show(parentFragmentManager, "SuccessEntryDialog")
    }

    /** ID入力バリデーションダイアログ表示 */
    private fun showIdValidationDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_not_exist_mimamoriid),
            showPositive = true,
            showNegative = false
        )
        dialog.show(parentFragmentManager, "IdValidationDialog")
    }

    // --------ミマモリ側---------
    /** IDコピー成功ダイアログ表示 */
    private fun showSuccessCopyIdDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_success_copy),
            showPositive = true,
            showNegative = false
        )
        dialog.showNow(parentFragmentManager, "SuccessCopyIdDialog")
    }

    companion object{
        @JvmStatic
        fun newInstance(isMamorare: Boolean) =
            Onboarding3Fragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_MAMORARE_KEY, isMamorare)
                }
            }

        private const val ARG_IS_MAMORARE_KEY = "ARG_IS_MAMORARE_KEY"
    }
}