package com.amefure.mimamori.View.Main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.amefure.mimamori.R
import com.amefure.mimamori.View.FBAuthentication.AuthActivity
import com.amefure.mimamori.View.Setting.SettingFragment
import com.amefure.mimamori.ViewModel.AuthEnvironment
import com.amefure.mimamori.ViewModel.RootEnvironment


class MamorareFragment : Fragment() {

    private val authEnvironment: AuthEnvironment by viewModels()
    private val rootEnvironment: RootEnvironment by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mamorare, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootEnvironment.observeMyUserData()

        var notifySendButton: Button = view.findViewById(R.id.notify_send_button)

        setUpHeaderAction(view)
        notifySendButton.setOnClickListener {
        }
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