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
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.amefure.mimamori.Model.Domain.AppUser
import com.amefure.mimamori.Model.Domain.AuthProviderModel
import com.amefure.mimamori.Model.Config.AppURL
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.AppEnvironmentStore
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.View.FBAuthentication.AuthActivity
import com.amefure.mimamori.ViewModel.AuthEnvironment
import com.amefure.mimamori.ViewModel.RootEnvironment
import com.amefure.mimamori.ViewModel.SettingViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingFragment : Fragment() {

    private val viewModel: SettingViewModel by viewModels()
    private val rootEnvironment: RootEnvironment by viewModels()
    private val authEnvironment: AuthEnvironment by viewModels()

    private var disposable = CompositeDisposable()
    private var myAppUser: AppUser? = AppEnvironmentStore.instance.myAppUser.value
    private var provider: String = AuthProviderModel.NONE.name

    private lateinit var mimamoriListRow: LinearLayout
    private lateinit var mimamoriEntryRow: LinearLayout
    private lateinit var mimamoriIdRow: LinearLayout
    private lateinit var mamorareListRow: LinearLayout
    private lateinit var notifyMsgRow: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpView(view)
        setUpHeaderAction(view)
        setOnClickListener(view)

        AppEnvironmentStore.instance.myAppUser.subscribeBy { user ->
            myAppUser = user
            showSwitchIsMamorareRow(user.isMamorare)
        }.addTo(disposable)

        provider = viewModel.getSignInProvider()
    }


    /** マモラレフラグによる設定行の表示/非表示切り替え */
    private fun setUpView(view: View) {
        mimamoriListRow = view.findViewById(R.id.app_mimamori_list_row)
        mimamoriEntryRow = view.findViewById(R.id.app_mimamori_entry_row)
        mimamoriIdRow = view.findViewById(R.id.app_mimamori_id_row)
        mamorareListRow = view.findViewById(R.id.app_mamorare_list_row)
        notifyMsgRow = view.findViewById(R.id.app_notify_msg_row)
    }

    /** マモラレフラグによる設定行の表示/非表示切り替え */
    private fun showSwitchIsMamorareRow(isMamorare: Boolean) {
        if (isMamorare) {
            mimamoriListRow.visibility = View.VISIBLE
            mimamoriEntryRow.visibility = View.VISIBLE
            mimamoriIdRow.visibility = View.GONE
            mamorareListRow.visibility = View.GONE
            notifyMsgRow.visibility = View.VISIBLE
        } else {
            mimamoriListRow.visibility = View.GONE
            mimamoriEntryRow.visibility = View.GONE
            mimamoriIdRow.visibility = View.VISIBLE
            mamorareListRow.visibility = View.VISIBLE
            notifyMsgRow.visibility = View.GONE
        }
    }

    /**　ボタンクリックイベント登録　*/
    private fun setOnClickListener(view: View) {
        val currentModeRow: LinearLayout = view.findViewById(R.id.app_current_mode_row)
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
            parentFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, AlignmentUserListFragment())
                addToBackStack(null)
                commit()
            }
        }
        // ミマモリ登録
        mimamoriEntryRow.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, EntryMimamoriIdFragment())
                addToBackStack(null)
                commit()
            }
        }
        // ミマモリID
        mimamoriIdRow.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, ConfirmMimamoriIdFragment())
                addToBackStack(null)
                commit()
            }
        }
        // マモラレリスト
        mamorareListRow.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, AlignmentUserListFragment())
                addToBackStack(null)
                commit()
            }
        }
        // 通知メッセージ変更
        notifyMsgRow.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, EditNotifyMessageFragment())
                addToBackStack(null)
                commit()
            }
        }
        // アプリの使い方
        howToUseRow.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, HowToUseFragment())
                addToBackStack(null)
                commit()
            }
        }


        // ユーザー情報編集
        authEditInfoRow.setOnClickListener {
            myAppUser?.let {
                parentFragmentManager.beginTransaction().apply {
                    add(R.id.main_frame, EditUserNameFragment.newInstance(it.id, it.name))
                    addToBackStack(null)
                    commit()
                }
            }
        }
        // サインアウト
        authSignOutRow.setOnClickListener {
            showConfirmSignOutDialog()
        }
        // 退会
        authWithdrawalRow.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, WithdrawalFragment.newInstance(provider))
                addToBackStack(null)
                commit()
            }
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

    /** ログアウトダイアログ表示 */
    private fun showConfirmSignOutDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_sign_out),
            showPositive = true,
            showNegative = true,
            positionText = getString(R.string.dialog_sign_out_positive_text),
        )
        dialog.setOnTappedListener(
            object : CustomNotifyDialogFragment.setOnTappedListener {
                override fun onPositiveButtonTapped() {
                    authEnvironment.signOut()
                    startAuthRootView()
                }
                override fun onNegativeButtonTapped() { }
            }
        )
        dialog.showNow(parentFragmentManager, "showConfirmSignOutDialog")
    }

    /**
     * ヘッダーボタンセットアップ
     * [LeftButton]：backButton
     * [RightButton]：非表示(GONE)
     */
    private fun setUpHeaderAction(view: View) {
        val headerView: ConstraintLayout = view.findViewById(R.id.include_header)
        // アプリ設定の行に表示するモード名
        val currentModeLabel: TextView = view.findViewById(R.id.current_mode_row_label)

        // 一瞬デフォルト値が表示されてしまうため
        // 明示的にここで更新しておく
        val isMamorare = rootEnvironment.getIsMamorare()
        val headerTitle: TextView = view.findViewById(R.id.header_title)
        headerTitle.text = if (isMamorare) getString(R.string.mamorare) else getString(R.string.mimamori)

        lifecycleScope.launch(Dispatchers.Main) {
            rootEnvironment.observeIsMamorare().collect { isMamorare ->
                isMamorare ?: return@collect
                headerTitle.text = if (isMamorare) getString(R.string.mamorare) else getString(R.string.mimamori)
                currentModeLabel.text =  if (isMamorare) getString(R.string.setting_app_current_mode_mamorare) else getString(R.string.setting_app_current_mode_mimamori)
            }
        }

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