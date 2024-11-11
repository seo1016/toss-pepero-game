package com.toss.pepero

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toss.pepero.databinding.FragmentRetryBinding

class RetryFragment : Fragment() {

    private lateinit var binding: FragmentRetryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRetryBinding.inflate(inflater, container, false)

        return binding.root
    }
}