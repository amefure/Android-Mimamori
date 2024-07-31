package com.amefure.mimamori.View.Setting.RecycleViewSetting

import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.amefure.mimamori.Model.Domain.AppUser

class UserListItemTouchListener: RecyclerView.SimpleOnItemTouchListener() {

    private lateinit var listener: onTappedListener
    interface onTappedListener {
        fun onChangedMamorareTapped(user: AppUser)
    }

    /**
     * リスナーのセットは使用するFragmentから呼び出して行う
     * リスナーオブジェクトの中に処理が含まれて渡される
     */
    public fun setOnTappedListener(listener: onTappedListener) {
        // 定義した変数listenerに実行したい処理を引数で渡す（CategoryListFragmentで渡している）
        this.listener = listener
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        // タッチイベントの処理
        if (e.action == MotionEvent.ACTION_DOWN) {
            // タッチされた位置のViewを取得
            val childView: View? = rv.findChildViewUnder(e.x, e.y)
            if (childView != null) {
                // 要素番号を取得
                val position = rv.getChildAdapterPosition(childView)
                if (position != RecyclerView.NO_POSITION) {
                    val adapter = rv.adapter
                    if (adapter is UserListAdapter) {
                        val user: AppUser? = adapter.getItemAtPosition(position)
                        if (user != null) {
                            listener.onChangedMamorareTapped(user)
                        }
                    }
                }
            }
        }
        return false // 通常のタッチイベント処理を維持
    }
}
