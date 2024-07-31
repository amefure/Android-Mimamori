package com.amefure.mimamori.View.Setting.RecycleViewSetting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amefure.mimamori.Model.Domain.AppUser
import com.amefure.mimamori.R
import com.amefure.mimamori.View.Utility.OneTouchHelperCallback

class UserListAdapter(
    dateList: List<AppUser>,
    private val selectUserId: String
) : RecyclerView.Adapter<UserListAdapter.MainViewHolder>(), OneTouchHelperCallback.DragAdapter {

    private val _dateList: MutableList<AppUser> = dateList.toMutableList()
    override fun getItemCount(): Int = _dateList.size

//    public fun deleteItem(position: Int) {
//        if (position < 0 || position >= _dateList.size) {
//            return
//        }
//        _dateList.removeAt(position)
//        notifyItemRemoved(position)
//    }
//
    public fun getItemAtPosition(position: Int) : AppUser? {
        if (position < 0 || position >= _dateList.size) {
            return null
        }
        val item = _dateList[position]
        return item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.component_user_row, parent, false)
        return MainViewHolder(view).also { viewHolder ->
            viewHolder.foregroundKnobLayout.setOnClickListener {
                val position = viewHolder.adapterPosition
            }
        }
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val user = _dateList[position]
        holder.userNameLabel.setText(user.name)
        if (user.id == selectUserId) {
            holder.iconWatch.visibility = View.VISIBLE
        } else {
            holder.iconWatch.visibility = View.GONE
        }

        holder.deleteButton.setOnClickListener {
            listener.onDeleteTapped(user)
        }
    }
    class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), OneTouchHelperCallback.SwipeViewHolder {

        override val foregroundKnobLayout: ViewGroup = itemView.findViewById(R.id.foregroundKnobLayout)
        override val backgroundLeftButtonLayout: ViewGroup = itemView.findViewById(R.id.backgroundLeftButtonLayout)
        override val backgroundRightButtonLayout: ViewGroup = itemView.findViewById(R.id.backgroundRightButtonLayout)
        override val canRemoveOnSwipingFromRight: Boolean get() = false

        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        val userNameLabel: TextView = itemView.findViewById(R.id.user_name_label)
        val iconWatch: ImageView = itemView.findViewById(R.id.icon_watch)
    }

    private lateinit var listener: onTappedListener
    interface onTappedListener {
        fun onDeleteTapped(user: AppUser)
    }

    /**
     * リスナーのセットは使用するFragmentから呼び出して行う
     * リスナーオブジェクトの中に処理が含まれて渡される
     */
    public fun setOnTappedListener(listener: onTappedListener) {
        // 定義した変数listenerに実行したい処理を引数で渡す（CategoryListFragmentで渡している）
        this.listener = listener
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        // 未使用
    }
}