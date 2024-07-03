package com.amefure.mimamori.View.BaseFragment

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

/** 入力操作を行うFragmentが継承するオープンクラス */
open class BaseInputFragment : Fragment() {

    /** キーボードを閉じる */
    public fun closedKeyBoard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}