package com.amefure.mimamori.View.Utility

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

/** Viewの輪郭を角丸にする */
class RoundedCornerOutlineProvider(private val cornerRadius: Float) : ViewOutlineProvider() {
    override fun getOutline(view: View?, outline: Outline?) {
        view?.let { v ->
            outline?.setRoundRect(0, 0, v.width, v.height, cornerRadius)
        }
    }
}