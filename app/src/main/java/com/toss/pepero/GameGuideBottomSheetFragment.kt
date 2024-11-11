package com.toss.pepero

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toss.pepero.databinding.FragmentGameGuideBottomSheetBinding

class GameGuideBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentGameGuideBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameGuideBottomSheetBinding.inflate(inflater, container, false)

        return binding.root
    }
}