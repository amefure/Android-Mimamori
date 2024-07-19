package com.amefure.mimamori.View.Main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amefure.mimamori.Managers.AppNotifyManager
import com.amefure.mimamori.Model.AppUser
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.AppEnvironmentStore
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.View.Setting.RecycleViewSetting.UserListAdapter
import com.amefure.mimamori.View.Setting.SettingFragment
import com.amefure.mimamori.View.Utility.OneTouchHelperCallback
import com.amefure.mimamori.ViewModel.AuthEnvironment
import com.amefure.mimamori.ViewModel.MamorareMainViewModel
import com.amefure.mimamori.ViewModel.RootEnvironment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy


class MamorareFragment : Fragment() {

    private val rootEnvironment: RootEnvironment by viewModels()
    private val viewModel: MamorareMainViewModel by viewModels()

    private var disposable = CompositeDisposable()

    private lateinit var recyclerView: RecyclerView
    private lateinit var notifyCountLabel: TextView
    private lateinit var mimamoriCountLabel: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mamorare, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootEnvironment.observeMyUserData()
        setUpHeaderAction(view)
        setUpView(view)
        subscribeMyAppUser()
    }

    /** Viewセットアップ */
    private fun setUpView(view: View) {
        // 通知リストビュー
        recyclerView = view.findViewById(R.id.notify_list_view)
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())

        // 通知送信ボタン
        val notifySendButton: Button = view.findViewById(R.id.notify_send_button)
        notifySendButton.setOnClickListener {
            viewModel.sendNotification { result ->
                activity?.runOnUiThread {
                    showNotifyResultDialog(result)
                }
            }
        }

        // 通知回数/ミマモリ人数
        val settingContainer: ConstraintLayout = view.findViewById(R.id.setting_container)
        notifyCountLabel = settingContainer.findViewById(R.id.notify_count_label)
        mimamoriCountLabel = settingContainer.findViewById(R.id.mimamori_count_label)
    }

    /** ユーザー情報観測 */
    private fun subscribeMyAppUser() {
        AppEnvironmentStore.instance.myAppUser
            .subscribeBy { user ->
                // 通知リストビュー
                val adapter = NotifyListAdapter(AppUser.sectionNotifications(user.notifications))
                OneTouchHelperCallback(recyclerView).build()
                recyclerView.adapter = adapter

                // 通知回数/ミマモリ人数
                notifyCountLabel.text = AppUser.getTodayNotifyCount(user.notifications).toString()
                mimamoriCountLabel.text = user.mimamoriIdList.size.toString()
            }.addTo(disposable)
    }


    /** プッシュ通知結果ダイアログ表示 */
    private fun showNotifyResultDialog(success: Boolean) {
        val msg = if (success) getString(R.string.dialog_success_send_notify) else getString(R.string.dialog_failed_send_notify)
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = msg,
            showPositive = true,
            showNegative = false,
        )
        dialog.showNow(parentFragmentManager, "NotifyResultDialog")
    }


    /**
     * ヘッダーボタンセットアップ
     * [LeftButton]：非表示(GONE)
     * [RightButton]：設定画面遷移ボタン
     */
    private fun setUpHeaderAction(view: View) {
        val headerView: ConstraintLayout = view.findViewById(R.id.include_header)
        val leftButton: ImageButton = headerView.findViewById(R.id.left_button)
        leftButton.visibility = View.GONE

        val rightButton: ImageButton = headerView.findViewById(R.id.right_button)
        rightButton.setImageResource(R.drawable.button_settings)
        rightButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, SettingFragment())
                addToBackStack(null)
                commit()
            }
        }
    }
}