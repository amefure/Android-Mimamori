package com.amefure.mimamori.View.Main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amefure.mimamori.Model.AppNotify
import com.amefure.mimamori.R

class NotifyListAdapter(
    private var notifications: List<AppNotify>
): RecyclerView.Adapter<NotifyListAdapter.MainViewHolder>() {

    private val _notifications: MutableList<AppNotify> = notifications.toMutableList()

    override fun getItemCount(): Int = _notifications.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.component_notify_row, parent, false)
        )
    }


    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val notify = _notifications[position]

        holder.title.text = notify.title
        holder.msg.text = notify.msg
        holder.time.text = notify.getTimeString()
    }

    class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.notify_title)
        val msg: TextView = itemView.findViewById(R.id.notify_msg)
        val time: TextView = itemView.findViewById(R.id.notify_time)
    }
}