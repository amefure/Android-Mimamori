package com.amefure.mimamori.View.BaseFragment

import com.amefure.mimamori.R
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment

/** Authの認証絡みのFragmentが継承するオープンクラス
 *  内部的に[BaseInputFragment]を継承
 */
open class BaseAuthFragment: BaseInputFragment() {

    /** 入力バリデーションダイアログ表示 */
    public fun showFailedValidationDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_auth_validation_input),
            showPositive = true,
            showNegative = false
        )
        dialog.show(parentFragmentManager, "FailedAuthValidationInput")
    }


    /** 認証系の失敗エラーダイアログ */
    public fun showFailedAuthDialog(msg: String) {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = msg,
            showPositive = true,
        )
        dialog.showNow(parentFragmentManager, "FailedAuthDialog")
    }

}