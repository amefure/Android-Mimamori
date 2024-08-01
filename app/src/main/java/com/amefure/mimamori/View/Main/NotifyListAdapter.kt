package com.amefure.mimamori.View.Main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.amefure.mimamori.Model.Domain.AppNotify
import com.amefure.mimamori.Model.Domain.AppNotifyBase
import com.amefure.mimamori.Model.Domain.AppNotifySection
import com.amefure.mimamori.R

class NotifyListAdapter(
    private var notifications: List<AppNotifyBase>
): RecyclerView.Adapter<NotifyListAdapter.MainViewHolder>() {

    private val _notifications: MutableList<AppNotifyBase> = notifications.toMutableList()

    override fun getItemCount(): Int = _notifications.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.component_notify_row, parent, false)
        )
    }


    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val notify = _notifications[position]

        if (notify is AppNotify) {
            holder.baseLayout.maxHeight = 180
            holder.sectionLayout.visibility = View.GONE
            holder.itemLayout.visibility = View.VISIBLE
            holder.title.text = notify.title
            holder.msg.text = notify.msg
            holder.time.text = notify.getTimeString("HH:mm:ss")
        } else if (notify is AppNotifySection) {
            holder.baseLayout.maxHeight = 60
            holder.sectionLayout.visibility = View.VISIBLE
            holder.itemLayout.visibility = View.GONE
            holder.sectionDate.text = notify.dayStr
        }
    }

    class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.notify_title)
        val msg: TextView = itemView.findViewById(R.id.notify_msg)
        val time: TextView = itemView.findViewById(R.id.notify_time)
        val sectionDate: TextView = itemView.findViewById(R.id.notify_section_date_label)

        val itemLayout: com.google.android.material.card.MaterialCardView = itemView.findViewById(R.id.notify_item_layout)
        val sectionLayout: LinearLayout = itemView.findViewById(R.id.notify_section_layout)
        val baseLayout: ConstraintLayout = itemView.findViewById(R.id.notify_base_layout)
    }
}