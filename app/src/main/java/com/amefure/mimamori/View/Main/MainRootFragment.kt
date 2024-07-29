package com.amefure.mimamori.View.Main

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amefure.mimamori.Model.AppUser
import com.amefure.mimamori.Model.AuthProviderModel
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.AppEnvironmentStore
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.View.Setting.SettingFragment
import com.amefure.mimamori.View.Utility.OneTouchHelperCallback
import com.amefure.mimamori.ViewModel.MainRootViewModel
import com.amefure.mimamori.ViewModel.RootEnvironment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.w3c.dom.Text


/**
 * マモラレ/ミマモリメイン画面
 * Viewを都度切り替えて表示する
 */
class MainRootFragment : Fragment() {

    private val rootEnvironment: RootEnvironment by viewModels()
    private val viewModel: MainRootViewModel by viewModels()

    private var disposable = CompositeDisposable()

    private var isOffline = false

    /** マモラレ　*/
    private lateinit var recyclerView: RecyclerView
    private lateinit var notifyCountLabel: TextView
    private lateinit var mimamoriCountLabel: TextView

    /** ミマモリ　*/
    private lateinit var mimamoriNameLabel: TextView
    private lateinit var recyclerView_mima: RecyclerView
    private lateinit var notifyCountLabel_mima: TextView
    private lateinit var mimamoriCountLabel_mima: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_root, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootEnvironment.observeMyUserData()

        setUpHeaderAction(view)
        setUpMimamoriView(view)
        setUpMamorareView(view)
        subscribeMyAppUser(view)
    }

    /** 【マモラレ】Viewセットアップ */
    private fun setUpMamorareView(view: View) {
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


    /** 【ミマモリ】Viewセットアップ */
    private fun setUpMimamoriView(view: View) {
        mimamoriNameLabel = view.findViewById(R.id.mimamori_name_label)

        mimamoriNameLabel.text = getString(R.string.main_root_mimamori_name, "未設定")

        // 通知リストビュー
        recyclerView_mima = view.findViewById(R.id.notify_list_view_mima)
        recyclerView_mima.layoutManager = LinearLayoutManager(this.requireContext())

        // 通知回数/ミマモリ人数
        val settingContainer_mima: ConstraintLayout = view.findViewById(R.id.setting_container_mima)
        notifyCountLabel_mima = settingContainer_mima.findViewById(R.id.notify_count_label)
        mimamoriCountLabel_mima = settingContainer_mima.findViewById(R.id.mimamori_count_label)
    }


    /**
     * ユーザー情報観測してメイン画面を切り替え
     * ネットワーク状態も取得してオフラインならオフラインViewに切り替える
     */
    private fun subscribeMyAppUser(view: View) {
        // マモラレ/ミマモリレイアウト
        val mamorareLayout: LinearLayout = view.findViewById(R.id.mamorare_layout)
        val mimamoriLayout: LinearLayout = view.findViewById(R.id.mimamori_layout)

        AppEnvironmentStore.instance.myAppUser
            .subscribeBy { user ->
                val isMamorare = rootEnvironment.getIsMamorare()
                if (isMamorare) {
                    // マモラレ側を表示
                    mamorareLayout.visibility = View.VISIBLE
                    mimamoriLayout.visibility = View.GONE
                    // 通知リストビュー
                    val adapter = NotifyListAdapter(AppUser.sectionNotifications(user.notifications))
                    OneTouchHelperCallback(recyclerView).build()
                    recyclerView.adapter = adapter

                    // 通知回数/マモラレ人数
                    notifyCountLabel.text = AppUser.getTodayNotifyCount(user.notifications).toString()
                    mimamoriCountLabel.text = user.mamorareIdList.size.toString()
                } else {
                    // ミマモリ側を表示
                    mamorareLayout.visibility = View.GONE
                    mimamoriLayout.visibility = View.VISIBLE

                    val currentMamorareUser = user.currentMamorareList.firstOrNull {
                        it.id == user.currentMamorareId
                    } ?: return@subscribeBy

                    mimamoriNameLabel.text = getString(R.string.main_root_mimamori_name, currentMamorareUser.name)

                    // 通知リストビュー
                    val adapter = NotifyListAdapter(AppUser.sectionNotifications(currentMamorareUser.notifications))
                    OneTouchHelperCallback(recyclerView_mima).build()
                    recyclerView_mima.adapter = adapter

                    // 通知回数/ミマモリ人数
                    notifyCountLabel_mima.text = AppUser.getTodayNotifyCount(currentMamorareUser.notifications).toString()
                    mimamoriCountLabel_mima.text = currentMamorareUser.mimamoriIdList.size.toString()
                }
            }.addTo(disposable)

        val offlineView: LinearLayout = view.findViewById(R.id.offline_layout)

        val connectivityManager = this.requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (networkCapabilities != null) {
            val state = when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                    "Wi-Fiに接続中"
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                    "キャリア通信に接続中"
                else -> "その他のネットワークに接続中"
            }
            Log.d("NetWork", state)
            offlineView.visibility = View.GONE
        } else {
            isOffline = true
            Log.d("NetWork", "オフライン状態")
            offlineView.visibility = View.VISIBLE
            mamorareLayout.visibility = View.GONE
            mimamoriLayout.visibility = View.GONE
        }
    }


    /** 【マモラレ】プッシュ通知結果ダイアログ表示 */
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

        lifecycleScope.launch(Dispatchers.Main) {
            rootEnvironment.observeIsMamorare().collect { isMamorare ->
                isMamorare ?: return@collect
                val headerTitle: TextView = view.findViewById(R.id.header_title)
                headerTitle.text = if (isMamorare) getString(R.string.mamorare) else getString(R.string.mimamori)
            }
        }

        val leftButton: ImageButton = headerView.findViewById(R.id.left_button)
        leftButton.visibility = View.GONE

        val rightButton: ImageButton = headerView.findViewById(R.id.right_button)
        rightButton.setImageResource(R.drawable.button_settings)
        rightButton.setOnClickListener {
            // オンラインでのみ遷移可能にする
            if (!isOffline) {
                parentFragmentManager.beginTransaction().apply {
                    add(R.id.main_frame, SettingFragment())
                    addToBackStack(null)
                    commit()
                }
            }
        }
    }
}