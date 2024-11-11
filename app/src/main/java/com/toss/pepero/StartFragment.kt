package com.toss.pepero

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toss.pepero.databinding.FragmentStartBinding

class StartFragment : Fragment() {

    private lateinit var binding: FragmentStartBinding

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStartBinding.inflate(inflater, container, false)

        handler.postDelayed({
            binding.llPeperoThree.visibility = View.VISIBLE
            binding.tvSuccess.visibility = View.VISIBLE
        }, 1000)

        handler.postDelayed({
            binding.llPeperoThree.visibility = View.GONE
            binding.tvSuccess.visibility = View.GONE
        }, 3000)

        handler.postDelayed({
            binding.tvStart.visibility = View.VISIBLE
            startCountdown()
        }, 3000)

        return binding.root
    }

    private fun startCountdown() {
        var countdown = 3

        val countdownRunnable = object : Runnable {
            override fun run() {
                if (countdown > 0) {
                    binding.tvCount.text = countdown.toString()
                    binding.tvCount.visibility = View.VISIBLE
                    countdown--
                    handler.postDelayed(this, 1000)
                } else {
                    navigateToGameFragment()
                }
            }
        }

        handler.post(countdownRunnable)
    }

    private fun navigateToGameFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, GameFragment())
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }
}

