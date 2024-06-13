package com.amefure.mimamori.View.Setting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.amefure.mimamori.Model.Config.AppURL
import com.amefure.mimamori.R
import com.amefure.mimamori.View.FBAuthentication.AuthActivity
import com.amefure.mimamori.ViewModel.AuthEnvironment

class SettingFragment : Fragment() {

    private val authEnvironment: AuthEnvironment by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHeaderAction(view)
        setOnClickListener(view)
    }

    /**
     * ボタンクリックイベント登録
     */
    private fun setOnClickListener(view: View) {
        val currentModeRow: LinearLayout = view.findViewById(R.id.app_current_mode_row)
        val mimamoriListRow: LinearLayout = view.findViewById(R.id.app_mimamori_list_row)
        val mimamoriEntryRow: LinearLayout = view.findViewById(R.id.app_mimamori_entry_row)
        val mimamoriIdRow: LinearLayout = view.findViewById(R.id.app_mimamori_id_row)
        val mamorareListRow: LinearLayout = view.findViewById(R.id.app_mamorare_list_row)
        val notifyMsgRow: LinearLayout = view.findViewById(R.id.app_notify_msg_row)
        val howToUseRow: LinearLayout = view.findViewById(R.id.app_how_to_use_row)

        val authEditInfoRow: LinearLayout = view.findViewById(R.id.auth_edit_info_row)
        val authSignOutRow: LinearLayout = view.findViewById(R.id.auth_sign_out_row)
        val authWithdrawalRow: LinearLayout = view.findViewById(R.id.auth_withdrawal_row)

        val inquiryRow: LinearLayout = view.findViewById(R.id.link_inquiry_row)
        val termsOfServiceRow: LinearLayout = view.findViewById(R.id.link_terms_of_service_row)

        // カレントモード
        currentModeRow.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, SelectAppMainModeFragment())
                addToBackStack(null)
                commit()
            }
        }
        // ミマモリリスト
        mimamoriListRow.setOnClickListener {

        }
        // ミマモリ登録
        mimamoriEntryRow.setOnClickListener {

        }
        // ミマモリID
        mimamoriIdRow.setOnClickListener {

        }
        // マモラレリスト
        mamorareListRow.setOnClickListener {

        }
        // 通知メッセージ変更
        notifyMsgRow.setOnClickListener {

        }
        // アプリの使い方
        howToUseRow.setOnClickListener {

        }


        // ユーザー情報編集
        authEditInfoRow.setOnClickListener {
        }
        // サインアウト
        authSignOutRow.setOnClickListener {
            authEnvironment.signOut()
            startAuthRootView()
        }
        // 退会
        authWithdrawalRow.setOnClickListener {
            authEnvironment.withdrawal()
        }

        // お問い合わせリンク
        inquiryRow.setOnClickListener {
            val uri = Uri.parse(AppURL.INQUIRY_URL)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        // お問い合わせリンク
        termsOfServiceRow.setOnClickListener {
            val uri = Uri.parse(AppURL.TERMS_OF_SERVICE_URL)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
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
     *  サインイン画面起動
     */
    private fun startAuthRootView() {
        val intent = Intent(this.requireContext(), AuthActivity::class.java)
        startActivity(intent)
        this.requireActivity().finish()
    }
}