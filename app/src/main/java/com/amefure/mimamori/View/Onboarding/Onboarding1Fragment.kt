package com.amefure.mimamori.View.Onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.amefure.mimamori.R

class Onboarding1Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nextButton: Button = view.findViewById(R.id.next_button)

        nextButton.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.main_frame, Onboarding2Fragment())
                commit()
            }
        }
    }
}