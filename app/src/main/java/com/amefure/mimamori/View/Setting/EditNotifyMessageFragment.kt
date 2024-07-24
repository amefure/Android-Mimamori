package com.amefure.mimamori.View.Setting

import android.app.ActivityManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.DataStore.DataStoreRepository.Companion.NotifyMsgNumber
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.ViewModel.RootEnvironment
import com.amefure.mimamori.ViewModel.SettingViewModel

class EditNotifyMessageFragment : Fragment() {

    private val rootEnvironment: RootEnvironment by viewModels()
    private val viewModel: SettingViewModel by viewModels()

    private var notifyItemInputList: MutableList<EditText> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_notify_message, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHeaderAction(view)
        setUpNotifyItemView(view)

        val updateNotifyMsgButton: Button = view.findViewById(R.id.update_notify_msg_button)
        updateNotifyMsgButton.setOnClickListener {
            // 通知メッセージ入力に空白がないかチェック
            notifyItemInputList.firstOrNull { it.text.isEmpty() } ?: run {
                notifyItemInputList.forEachIndexed { index, editText ->
                    // 通知メッセージをローカルに保存
                    viewModel.saveNotifyMsg(editText.text.toString(), index + 1)
                }
                showSuccessNotifyMsgDialog()
                return@setOnClickListener
            }
            // バリデーションアウトのため警告ダイアログ表示
            showFailedNotifyMsgDialog()
        }
    }


    /**
     * 通知アイテムセットアップ
     *
     */
    private fun setUpNotifyItemView(view: View) {
        // 通知レイアウトを取得
        val notifyLayouts: List<ConstraintLayout> = listOf(
            view.findViewById(R.id.notify_item_layout1),
            view.findViewById(R.id.notify_item_layout2),
            view.findViewById(R.id.notify_item_layout3)
        )

        // チェックアイコンを取得
        val checkedIcon: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.icon_check_circle_checked, null)
        val uncheckedIcon: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.icon_check_circle_unchecked, null)

        // 通知レイアウト内のチェックボタンを取得
        val checkButtons: MutableList<ImageButton> = mutableListOf()
        notifyLayouts.forEach { layout ->
            checkButtons.add(layout.findViewById(R.id.notify_item_check_icon))
        }

        val selectNumber = viewModel.getNotifySelectNumber()
        val title = getString(R.string.notify_title, viewModel.getSignInUserName())
        val msgList: List<String> = listOf(
            viewModel.getNotifyMsg(NotifyMsgNumber.ONE),
            viewModel.getNotifyMsg(NotifyMsgNumber.TWO),
            viewModel.getNotifyMsg(NotifyMsgNumber.THREE)
        )
        notifyLayouts.forEachIndexed { index, layout ->
            // 通知タイトル/メッセージ/時間をセット
            val notifyTitle: TextView = layout.findViewById(R.id.notify_title)
            val notifyMsg: TextView = layout.findViewById(R.id.notify_msg)
            val notifyTime: TextView = layout.findViewById(R.id.notify_time)
            notifyTitle.text = title
            notifyMsg.text = msgList[index]
            notifyTime.text = "now"

            // チェックボタンアイコン初期値をセット
            val notifyCheckIcon = checkButtons[index]
            if (index + 1 == selectNumber) {
                notifyCheckIcon.setImageDrawable(checkedIcon)
            } else {
                notifyCheckIcon.setImageDrawable(uncheckedIcon)
            }

            // チェックアイコンクリックイベントの実装
            setUpCheckIconClickListener(notifyCheckIcon, checkButtons, checkedIcon, uncheckedIcon, index)

            // 編集アイコンタップイベントの実装とViewの表示切り替え
            val notifyEditIcon: ImageButton = layout.findViewById(R.id.notify_item_edit_icon)
            val notifyView: ConstraintLayout = layout.findViewById(R.id.notify_item)
            val notifyItemInput: EditText = layout.findViewById(R.id.notify_item_input)
            notifyItemInput.setText(msgList[index])
            notifyItemInputList.add(notifyItemInput)
            setUpEditIconClickListener(notifyEditIcon, notifyView, notifyItemInput, notifyMsg)
        }

    }

    /**
     * 通知チェックアイコンクリック処理
     *  1. 選択された通知番号をローカルへ保存
     *  2. チェックアイコンを切り替え
     */
    private fun setUpCheckIconClickListener(
        button: ImageButton,
        checkButtons: List<ImageButton>,
        checkedIcon: Drawable?,
        uncheckedIcon: Drawable?,
        index: Int) {
        button.setOnClickListener {
            // 選択通知番号を保存
            viewModel.saveNotifySelectNumber(index + 1)
            // 他のボタンのチェックを外す
            checkButtons.forEach { listButton ->
                listButton.setImageDrawable(uncheckedIcon)
            }
            // チェックをつける
            button.setImageDrawable(checkedIcon)
        }
    }

    /** 通知編集アイコンクリックイベント */
    private fun setUpEditIconClickListener(
        button: ImageButton,
        notifyView: ConstraintLayout,
        notifyItemInput: EditText,
        notifyMsg: TextView) {
        button.setOnClickListener {
            if (button.id == 0) {
                button.id = -1
                // 通知Viewを表示/入力欄を非表示
                notifyView.visibility = View.VISIBLE
                notifyItemInput.visibility = View.GONE
                // 通知メッセージを更新
                notifyMsg.text = notifyItemInput.text
            } else {
                button.id = 0
                // 通知Viewを非表示/入力欄を表示
                notifyView.visibility = View.GONE
                notifyItemInput.visibility = View.VISIBLE
            }
        }
    }


    /** メッセージ変更成功ダイアログ表示 */
    private fun showSuccessNotifyMsgDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_success_notify_msg),
            showPositive = true,
            showNegative = false
        )
        dialog.setOnTappedListener(
            object : CustomNotifyDialogFragment.setOnTappedListener {
                override fun onPositiveButtonTapped() {
                    parentFragmentManager.popBackStack()
                }
                override fun onNegativeButtonTapped() { }
            }
        )
        dialog.showNow(parentFragmentManager, "showSuccessNotifyMsgDialog")
    }

    /** メッセージ空白警告ダイアログ表示 */
    private fun showFailedNotifyMsgDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_failed_notify_msg),
            showPositive = true,
            showNegative = false
        )
        dialog.showNow(parentFragmentManager, "showFailedNotifyMsgDialog")
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