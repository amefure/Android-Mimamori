package com.amefure.mimamori.View.Dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.amefure.mimamori.Model.Key.AppArgKey
import com.amefure.mimamori.R

class CustomNotifyDialogFragment : DialogFragment() {

    /**
     * 引数で受け取るデータ
     */
    private var title: String = ""
    private var msg: String = ""
    private var showPositive: Boolean = true
    private var showNegative: Boolean = true

    /**
     * 引数で受け取る処理
     */
    private var listener: onTappedListner? = null

    /**
     * ポジティブボタンアクション
     * ネガティブボタンアクション
     */
    interface onTappedListner {
        fun onPositiveButtonTapped()
        fun onNegativeButtonTapped()
    }

    /**
     * リスナーのセットは使用するFragmentから呼び出して行う
     * リスナーオブジェクトの中に処理が含まれて渡される
     */
    public fun setOnTappedListner(listener: onTappedListner? = null) {
        // 定義した変数listenerに実行したい処理を引数で渡す（CategoryListFragmentで渡している）
        this.listener = listener
    }

    /**
     * インスタンス化メソッド
     */
    companion object {
        @JvmStatic
        public fun newInstance(title: String, msg: String, showPositive: Boolean = true, showNegative: Boolean = true) =
            CustomNotifyDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(AppArgKey.ARG_DIALOG_TITLE_KEY, title)
                    putString(AppArgKey.ARG_DIALOG_MSG_KEY, msg)
                    putBoolean(AppArgKey.ARG_DIALOG_SHOW_POSITIVE_KEY, showPositive)
                    putBoolean(AppArgKey.ARG_DIALOG_SHOW_NEGATIVE_KEY, showNegative)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(AppArgKey.ARG_DIALOG_TITLE_KEY).toString()
            msg = it.getString(AppArgKey.ARG_DIALOG_MSG_KEY).toString()
            showPositive = it.getBoolean(AppArgKey.ARG_DIALOG_SHOW_POSITIVE_KEY)
            showNegative = it.getBoolean(AppArgKey.ARG_DIALOG_SHOW_NEGATIVE_KEY)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(this.requireContext())
        val inflater = this.requireActivity().layoutInflater
        val dialog = inflater.inflate(R.layout.fragment_custom_notify_dialog,null)

        val title: TextView = dialog.findViewById(R.id.dialog_title)
        val msg: TextView = dialog.findViewById(R.id.dialog_msg)
        val positiveButton: Button = dialog.findViewById(R.id.positive_button)
        val negativeButton: Button = dialog.findViewById(R.id.negative_button)
        val divider: View = dialog.findViewById(R.id.button_vertical_divider)

        if (!showPositive) {
            positiveButton.visibility = View.GONE
            divider.visibility = View.GONE
        }

        if (!showNegative) {
            negativeButton.visibility = View.GONE
            divider.visibility = View.GONE
        }

        title.text = this.title
        msg.text = this.msg

        // ポジティブボタンアクション実装
        positiveButton.setOnClickListener {
            listener?.onPositiveButtonTapped()
            dismiss()
        }

        // ネガティブボタンアクション実装
        negativeButton.setOnClickListener {
            listener?.onNegativeButtonTapped()
            dismiss()
        }

        builder.setView(dialog)
        return builder.create()
    }
}