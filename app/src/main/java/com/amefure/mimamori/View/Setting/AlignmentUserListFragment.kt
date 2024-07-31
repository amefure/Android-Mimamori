package com.amefure.mimamori.View.Setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amefure.mimamori.Model.Domain.AppUser
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.AppEnvironmentStore
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.View.Setting.RecycleViewSetting.UserListAdapter
import com.amefure.mimamori.View.Setting.RecycleViewSetting.UserListItemTouchListener
import com.amefure.mimamori.View.Utility.OneTouchHelperCallback
import com.amefure.mimamori.ViewModel.RootEnvironment
import com.amefure.mimamori.ViewModel.SettingViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

class AlignmentUserListFragment : Fragment() {

    private val rootEnvironment: RootEnvironment by viewModels()
    private val viewModel: SettingViewModel by viewModels()

    private var isMamorare = false

    private var disposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alignment_user_list, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHeaderAction(view)
        setUpRecycleView(view)
    }

    /**
     * ユーザーリスト
     * リサイクルビューセットアップ
     */
    private fun setUpRecycleView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.user_list)
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())

        // アイテムタップ：対象マモラレIDの変更機能
        // ミマモリ選択時のみ
        // myAppUserの観測に含めると複数発火してしまうため
        val itemTouchListener = UserListItemTouchListener()
        itemTouchListener.setOnTappedListener(
            object : UserListItemTouchListener.onTappedListener {
                override fun onChangedMamorareTapped(user: AppUser) {
                    changedMamorareUser(user)
                }
            }
        )
        recyclerView.addOnItemTouchListener(itemTouchListener)

        AppEnvironmentStore.instance.myAppUser.subscribeBy { user ->
            isMamorare = user.isMamorare
            if (isMamorare) {
                // マモラレの場合はリスト表示するだけ
                val adapter = UserListAdapter(user.currentMimamoriList, "")
                recyclerView.adapter = adapter
            } else {
                // ミマモリの場合はスワイプ削除とタップで対象マモラレIDの変更機能を実装
                val adapter = UserListAdapter(user.currentMamorareList, user.currentMamorareId)
                adapter.setOnTappedListener(
                    object : UserListAdapter.onTappedListener {
                        override fun onDeleteTapped(user: AppUser) {
                            showConfirmDeleteMamorareDialog(user)
                        }
                    }
                )
                // アイテムスワイプ：マモラレユーザー削除機能
                OneTouchHelperCallback(recyclerView).build()
                recyclerView.adapter = adapter
            }
        }.addTo(disposable)

    }

    /** ミマモリ対象のマモラレユーザーを変更 */
    private fun changedMamorareUser(user: AppUser) {
        // ミマモリ選択時のみ
        if (!isMamorare) {
            // 現在のマモラレ観測を停止
            rootEnvironment.resetObserveMamorareId()
            // 対象マモラレIDを変更
            viewModel.changeMamorare(user.id)
        }
    }

    /** マモラレ削除確認ダイアログ表示 */
    private fun showConfirmDeleteMamorareDialog(user: AppUser) {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_confirm_delete_mamorare, user.name),
            showPositive = true,
            showNegative = true
        )
        dialog.setOnTappedListener(
            object : CustomNotifyDialogFragment.setOnTappedListener {
                override fun onPositiveButtonTapped() {
                    // 観測しているのが削除対象なら
                    if (AppEnvironmentStore.instance.observeMamorareId == user.id) {
                        // 現在のマモラレ観測を停止
                        rootEnvironment.resetObserveMamorareId()
                    }
                    viewModel.deleteMamorareUser(user.id)
                }
                override fun onNegativeButtonTapped() { }
            }
        )
        dialog.showNow(parentFragmentManager, "ConfirmDeleteMamorareDialog")
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

}