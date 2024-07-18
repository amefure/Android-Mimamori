package com.amefure.mimamori.View.Main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amefure.mimamori.Model.AppUser
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.AppEnvironmentStore
import com.amefure.mimamori.View.Setting.SettingFragment
import com.amefure.mimamori.View.Utility.OneTouchHelperCallback
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy


class MimamoriFragment : Fragment() {

    private var disposable = CompositeDisposable()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mimamori, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHeaderAction(view)
        setUpRecycleView(view)
    }

    /**
     * 通知リスト
     * リサイクルビューセットアップ
     */
    private fun setUpRecycleView(view: View) {
        var recyclerView: RecyclerView = view.findViewById(R.id.notify_list_view)
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())

        AppEnvironmentStore.instance.myAppUser.subscribeBy { user ->
            val adapter = NotifyListAdapter(AppUser.sectionNotifications(user.notifications))
            OneTouchHelperCallback(recyclerView).build()
            recyclerView.adapter = adapter
        }.addTo(disposable)
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