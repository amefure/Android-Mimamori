package com.amefure.mimamori.View.Setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.amefure.mimamori.R
import com.amefure.mimamori.View.Setting.RecycleViewSetting.HowToUseAdapter
import com.amefure.mimamori.View.Utility.RoundedCornerOutlineProvider

class HowToUseFragment : Fragment() {

    private lateinit var adapter: HowToUseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_how_to_use, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpHeaderAction(view)
        setUpExpandableList(view)
    }

    /**
     * ヘッダーボタンセットアップ
     * [LeftButton]：backButton
     * [RightButton]：非表示(GONE)
     */
    private fun setUpExpandableList(view: View) {
        val expandableList: ExpandableListView = view.findViewById(R.id.expandable_list_view)
        // 角丸適応
        expandableList.outlineProvider = RoundedCornerOutlineProvider(20f)
        expandableList.clipToOutline = true

        // アダプターセット
        adapter = HowToUseAdapter(this.requireContext())
        expandableList.setAdapter(adapter)

        // 親要素をタップした際にアコーディオンアイコン切り替え
        expandableList.setOnGroupClickListener { parent, v, groupPosition, id ->
            adapter.toggleAccordionIcon(groupPosition)
            false
        }
    }

    /**
     * ヘッダーボタンセットアップ
     * [LeftButton]：backButton
     * [RightButton]：非表示(GONE)
     */
    private fun setUpHeaderAction(view: View) {
        val headerView: ConstraintLayout = view.findViewById(R.id.include_header)

        val leftButton: ImageButton = headerView.findViewById(R.id.left_button)
        leftButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val rightButton: ImageButton = headerView.findViewById(R.id.right_button)
        rightButton.visibility = View.GONE
    }
}