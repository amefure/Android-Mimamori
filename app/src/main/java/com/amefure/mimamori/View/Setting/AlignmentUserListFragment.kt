package com.amefure.mimamori.View.Setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amefure.mimamori.Model.AppUser
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.AppEnvironmentStore
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.View.Setting.RecycleViewSetting.UserListAdapter
import com.amefure.mimamori.View.Utility.OneTouchHelperCallback
import com.amefure.mimamori.ViewModel.RootEnvironment
import com.amefure.mimamori.ViewModel.SettingViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

class AlignmentUserListFragment : Fragment() {

    private val rootEnvironment: RootEnvironment by viewModels()
    private val viewModel: SettingViewModel by viewModels()

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
        var recyclerView: RecyclerView = view.findViewById(R.id.user_list)
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        recyclerView.addItemDecoration(
            DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL)
        )

        AppEnvironmentStore.instance.myAppUser.subscribeBy { user ->
            if (user.isMamorare) {
                var adapter = UserListAdapter(user.currentMimamoriList, user.currentMamorareId)
                recyclerView.adapter = adapter
            } else {
                var adapter = UserListAdapter(user.currentMamorareList,"")
                adapter.setOnTappedListener(
                    object : UserListAdapter.onTappedListener {
                        override fun onDeleteTapped(user: AppUser) {
                            showConfirmDeleteMamorareDialog(user)
                        }
                    }
                )
                OneTouchHelperCallback(recyclerView).build()
                recyclerView.adapter = adapter
            }
        }.addTo(disposable)

    }

    /**
     *  マモラレ削除確認ダイアログ表示
     */
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
                    viewModel.deleteMamorare(user.id) {

                    }
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

        val leftButton: ImageButton = headerView.findViewById(R.id.left_button)
        leftButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val rightButton: ImageButton = headerView.findViewById(R.id.right_button)
        rightButton.visibility = View.GONE
    }

}