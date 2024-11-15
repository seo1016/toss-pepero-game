package com.toss.pepero

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toss.pepero.databinding.FragmentSuccessBinding

class SuccessFragment : Fragment() {

    private lateinit var binding: FragmentSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSuccessBinding.inflate(inflater, container, false)

        binding.btnRetry.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GameFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        binding.tvGuide.setOnClickListener {
            val bottomSheet = GameGuideBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }

        return binding.root
    }
}