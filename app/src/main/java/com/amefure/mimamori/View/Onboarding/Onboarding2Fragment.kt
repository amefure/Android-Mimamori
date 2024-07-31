package com.amefure.mimamori.View.Onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.amefure.mimamori.R

class Onboarding2Fragment : Fragment() {

    private var isMamorare = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnClickListener(view)
    }

    override fun onResume() {
        super.onResume()
        isMamorare = true
    }

    /**
     * ボタンクリックイベント登録
     * ユーザー情報観測
     */
    private fun setOnClickListener(view: View) {
        val selectMamorareButton: Button = view.findViewById(R.id.select_mamorare_button)
        val selectMimamoriButton: Button = view.findViewById(R.id.select_mimamori_button)
        val selectMamorareLayout: LinearLayout = view.findViewById(R.id.select_mamorare_layout)
        val selectMimamoriLayout: LinearLayout = view.findViewById(R.id.select_mimamori_layout)

        val selectModeDesc: TextView = view.findViewById(R.id.select_mode_desc)

        selectMamorareButton.setOnClickListener {
            selectMamorareButton.setTextColor(this.requireContext().getColor(R.color.white))
            selectMimamoriButton.setTextColor(this.requireContext().getColor(R.color.ex_text))
            selectMamorareLayout.backgroundTintList =  ContextCompat.getColorStateList(this.requireContext(), R.color.ex_thema)
            selectMimamoriLayout.backgroundTintList =  ContextCompat.getColorStateList(this.requireContext(), R.color.ex_gray)
            selectModeDesc.text = getString(R.string.onboarding2_mamorare_msg)
            isMamorare = true
        }

        selectMimamoriButton.setOnClickListener {
            selectMamorareButton.setTextColor(this.requireContext().getColor(R.color.ex_text))
            selectMimamoriButton.setTextColor(this.requireContext().getColor(R.color.white))
            selectMamorareLayout.backgroundTintList =  ContextCompat.getColorStateList(this.requireContext(), R.color.ex_gray)
            selectMimamoriLayout.backgroundTintList =  ContextCompat.getColorStateList(this.requireContext(), R.color.ex_thema)
            selectModeDesc.text = getString(R.string.onboarding2_mimamori_msg)
            isMamorare = false
        }

        val nextButton: Button = view.findViewById(R.id.next_button)

        nextButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.main_frame, Onboarding3Fragment.newInstance(isMamorare))
                addToBackStack(null)
                commit()
            }
        }
    }
}