package com.amefure.mimamori.View.Setting.RecycleViewSetting

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.amefure.mimamori.R

class HowToUseAdapter (
    private val context: Context,
): BaseExpandableListAdapter() {

    // hotToUseQAList
    private val dataList: List<HowToUseQaData> =
        listOf(
            HowToUseQaData(context.getString(R.string.how_to_use_q1), context.getString(R.string.how_to_use_a1), false),
            HowToUseQaData(context.getString(R.string.how_to_use_q2), context.getString(R.string.how_to_use_a2), false),
            HowToUseQaData(context.getString(R.string.how_to_use_q3), context.getString(R.string.how_to_use_a3), false),
            HowToUseQaData(context.getString(R.string.how_to_use_q4), context.getString(R.string.how_to_use_a4), false),
        )


    // 親要素の数
    override fun getGroupCount(): Int {
        if (!dataList.isNullOrEmpty()) {
            return dataList.size
        }
        return 0
    }

    // 子要素の数
    override fun getChildrenCount(p0: Int): Int {
        return 1
    }

    // 親要素を取得
    override fun getGroup(p0: Int): Any {
        if (!dataList.isNullOrEmpty()) {
            return dataList.elementAt(p0).question
        }
        return ""
    }

    // 子要素を取得
    override fun getChild(p0: Int, p1: Int): Any {
        if (!dataList.isNullOrEmpty()) {
            return dataList.elementAt(p0).answer
        }
        return ""
    }

    // 親要素の固有ID
    override fun getGroupId(p0: Int): Long {
        return 0
    }

    // 子要素の固有ID
    override fun getChildId(p0: Int, p1: Int): Long {
        return 0
    }

    // 固有IDを持つかどうか
    override fun hasStableIds(): Boolean {
        return false
    }

    // 親要素のViewを構築
    override fun getGroupView(p0: Int, p1: Boolean, p2: View?, p3: ViewGroup?): View {
        val title = if (!dataList.isNullOrEmpty()) dataList.elementAt(p0).question else ""
        var convertView = p2

        if (convertView == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.component_how_to_question_row, null)
        }

        val titleTextView: TextView = convertView!!.findViewById(R.id.question_title_label)
        val accordionIcon: ImageView = convertView!!.findViewById(R.id.accordion_icon)
        if (dataList.elementAt(p0).expanded) {
            var icon: Drawable? = ResourcesCompat.getDrawable(context.resources, R.drawable.icon_minus, null)
            accordionIcon.setImageDrawable(icon)
        } else {
            var icon: Drawable? = ResourcesCompat.getDrawable(context.resources, R.drawable.icon_plus, null)
            accordionIcon.setImageDrawable(icon)
        }
        titleTextView.text = title

        return convertView
    }

    // 子要素のViewを構築
    override fun getChildView(p0: Int, p1: Int, p2: Boolean, p3: View?, p4: ViewGroup?): View {
        val title = dataList.elementAt(p0).answer
        var convertView = p3

        if (convertView == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.component_how_to_answer_row, null)
        }

        val titleTextView = convertView!!.findViewById<TextView>(R.id.answer_title_label)
        titleTextView.text = title

        return convertView
    }

    // 子要素がタップ可能かどうか
    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }

    /** アコーディオンアイコンを切り替える */
    public fun toggleAccordionIcon(index: Int) {
        dataList[index].expanded = !dataList[index].expanded
        notifyDataSetChanged()
    }

}

data class HowToUseQaData(
    val question: String,
    val answer: String,
    var expanded: Boolean
)