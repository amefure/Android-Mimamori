package com.amefure.mimamori.View.Setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.AppEnvironmentStore
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.ViewModel.RootEnvironment
import com.amefure.mimamori.ViewModel.SettingViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy

class SelectAppMainModeFragment : Fragment() {

    private val rootEnvironment: RootEnvironment by viewModels()
    private val viewModel: SettingViewModel by viewModels()
    private var compositeDisposable = CompositeDisposable()
    private var isMamorare = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_app_main_mode, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHeaderAction(view)
        setOnClickListener(view)
    }

    /**
     * ボタンクリックイベント登録
     * ユーザー情報観測
     */
    private fun setOnClickListener(view: View) {
        val selectMamorareButton: Button = view.findViewById(R.id.select_mamorare_button)
        val selectMimamoriButton: Button = view.findViewById(R.id.select_mimamori_button)
        val selectMamorareLayout: LinearLayout = view.findViewById(R.id.select_mamorare_layout)
        val selectMimamoriLayout: LinearLayout = view.findViewById(R.id.select_mimamori_layout)

        val selectModeDesc: TextView = view.findViewById(R.id.select_mode_desc)

        selectMamorareButton.setOnClickListener {
            selectMamorareButton.setTextColor(this.requireContext().getColor(R.color.white))
            selectMimamoriButton.setTextColor(this.requireContext().getColor(R.color.ex_text))
            selectMamorareLayout.backgroundTintList =  ContextCompat.getColorStateList(this.requireContext(), R.color.ex_thema)
            selectMimamoriLayout.backgroundTintList =  ContextCompat.getColorStateList(this.requireContext(), R.color.ex_gray)
            selectModeDesc.text = getString(R.string.onboarding2_mamorare_msg)
            isMamorare = true
        }

        selectMimamoriButton.setOnClickListener {
            selectMamorareButton.setTextColor(this.requireContext().getColor(R.color.ex_text))
            selectMimamoriButton.setTextColor(this.requireContext().getColor(R.color.white))
            selectMamorareLayout.backgroundTintList =  ContextCompat.getColorStateList(this.requireContext(), R.color.ex_gray)
            selectMimamoriLayout.backgroundTintList =  ContextCompat.getColorStateList(this.requireContext(), R.color.ex_thema)
            selectModeDesc.text = getString(R.string.onboarding2_mimamori_msg)
            isMamorare = false
        }

        val updateModeButton: Button = view.findViewById(R.id.update_mode_button)

        updateModeButton.setOnClickListener {
            if (isMamorare) {
                viewModel.selectMamorare()
            } else {
                viewModel.selectMimamori()
            }
            showSuccessUpdateModeDialog()
        }

        AppEnvironmentStore.instance.myAppUser.subscribeBy { user ->
            isMamorare = user.isMamorare
            if (isMamorare) {
                selectMamorareButton.performClick()
            } else {
                selectMimamoriButton.performClick()
            }
        }.addTo(compositeDisposable)
    }

    /** モード変更ダイアログ表示 */
    private fun showSuccessUpdateModeDialog() {
        var mode = if (isMamorare) getString(R.string.mamorare) else getString(R.string.mimamori)
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_success_mode, mode),
            showPositive = true,
            showNegative = false
        )
        dialog.showNow(parentFragmentManager, "SuccessUpdateModeDialog")
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