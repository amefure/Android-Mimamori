package com.amefure.mimamori.View.Dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.amefure.mimamori.R

class CustomLoadingDialogFragment : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            val inflater = layoutInflater
            val view = inflater.inflate(R.layout.fragment_custom_loading_dialog,null)

            setContentView(view)
            // ダイアログ以外のタッチを禁止
            dialog?.setCanceledOnTouchOutside(false)
            // ダイアログの外をタップしてもキャンセルされないようにする
            isCancelable = false

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            window?.setDimAmount(0f)
        }
    }


    companion object {
        // デフォルトタグ
        const val CUSTOM_LOADING_FRAGMENT_TAG = "CustomLoadingDialogFragmentTag"

        fun FragmentManager.presentLoadingDialog(
            fragmentTag: String = CUSTOM_LOADING_FRAGMENT_TAG
        ) {
            // 既に表示中である場合、多重に表示しない。
            if (findFragmentByTag(fragmentTag) != null) {
                return
            }
            val fragment = CustomLoadingDialogFragment()
            if (!fragment.isStateSaved) {
                try {
                    fragment.showNow(this, fragmentTag)
                } catch (e: Exception) {
                    // IllegalStateException: FragmentManager has been destroyed
                    // オンライン → オフラインを繰り返し切り替えると上記エラーが発生することがある
                    Log.e("", "ローディング表示エラー" + e)
                }
            }
        }

        fun FragmentManager.dismissLoadingDialog(
            fragmentTag: String = CUSTOM_LOADING_FRAGMENT_TAG
        ) {
            val fragment = findFragmentByTag(fragmentTag) as? CustomLoadingDialogFragment ?: return
            if (!fragment.isStateSaved) {
                try {
                    fragment.dismiss()
                } catch (e: Exception) {
                    Log.e("", "ローディング表示エラー" + e)
                }
            }
        }
    }
}
