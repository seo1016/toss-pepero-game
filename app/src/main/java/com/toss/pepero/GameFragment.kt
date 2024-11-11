package com.toss.pepero

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.animation.AlphaAnimation
import androidx.core.animation.addListener
import com.toss.pepero.databinding.FragmentGameBinding
import kotlin.random.Random

class GameFragment : Fragment() {

    private lateinit var binding: FragmentGameBinding
    private lateinit var progressAnimator: ValueAnimator
    private val totalTimeInMillis = 20000L // 20초
    private var collectedPepero = 0
    private val itemsMap = mutableMapOf<ImageView, Boolean>()
    private val itemResources = listOf(
        R.drawable.ic_bat,
        R.drawable.ic_break_through,
        R.drawable.ic_chopstick,
        R.drawable.ic_pepero,
        R.drawable.ic_tanghulu,
        R.drawable.ic_wooden_stick
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)

        setupSeekBar()
        startSmoothTimer()

        binding.root.post {
            startItemGeneration()
        }

        return binding.root
    }

    private fun setupSeekBar() {
        binding.timerSeekBar.apply {
            isEnabled = false
            progress = 1000
        }
    }

    private fun startSmoothTimer() {
        progressAnimator = ValueAnimator.ofFloat(1000f, 0f).apply {
            duration = totalTimeInMillis
            interpolator = LinearInterpolator()

            addUpdateListener { animation ->
                val progress = (animation.animatedValue as Float).toInt()
                binding.timerSeekBar.progress = progress
            }

            addListener(onEnd = {
                if (collectedPepero < 3) {
                    // 게임오버
                }
            })
        }

        progressAnimator.start()
    }

    private fun startItemGeneration() {
        generateItem()
    }

    private fun generateItem() {
        if (!isAdded) return

        val containerLayout = binding.root

        if (containerLayout.width <= 0 || containerLayout.height <= 0) {
            containerLayout.post { generateItem() }
            return
        }

        val imageView = ImageView(requireContext()).apply {
            val size = dpToPx(60)
            layoutParams = ConstraintLayout.LayoutParams(size, size)
        }

        val maxX = maxOf(containerLayout.width - dpToPx(60), dpToPx(30))
        val maxY = maxOf(containerLayout.height - dpToPx(60), dpToPx(100))
        val randomX = Random.nextInt(dpToPx(30), maxX)
        val randomY = Random.nextInt(dpToPx(100), maxY)

        imageView.x = randomX.toFloat()
        imageView.y = randomY.toFloat()

        // 랜덤
        val randomResource = itemResources.random()
        imageView.setImageResource(randomResource)

        itemsMap[imageView] = (randomResource == R.drawable.ic_pepero)

        imageView.setOnClickListener {
            if (itemsMap[imageView] == true) {
                collectedPepero++
                showPlusOne(imageView.x, imageView.y)
                updateCollectedCount()

                if (collectedPepero >= 3) {
                    progressAnimator.cancel()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SuccessFragment())
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
                }
            }
        }

        containerLayout.addView(imageView)

        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 300
        imageView.startAnimation(fadeIn)

        imageView.postDelayed({
            if (isAdded) {
                val fadeOut = AlphaAnimation(1f, 0f)
                fadeOut.duration = 300
                fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                    override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                    override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                        containerLayout.removeView(imageView)
                        itemsMap.remove(imageView)
                    }
                    override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                })
                imageView.startAnimation(fadeOut)
            }
        }, 2500)

        val nextDelay = Random.nextLong(800, 1500)
        containerLayout.postDelayed({ generateItem() }, nextDelay)
    }

    private fun showPlusOne(x: Float, y: Float) {
        val plusOneText = TextView(requireContext()).apply {
            text = "+1"
            textSize = 24f
            setTextColor(resources.getColor(android.R.color.black, null))
            translationX = x
            translationY = y
        }

        val containerLayout = binding.root
        containerLayout.addView(plusOneText)

        ValueAnimator.ofFloat(0f, -100f).apply {
            duration = 1000
            addUpdateListener {
                plusOneText.translationY = y + animatedValue as Float
                plusOneText.alpha = 1 - (animatedValue as Float / -100f)
            }
            addListener(onEnd = {
                containerLayout.removeView(plusOneText)
            })
            start()
        }
    }

    private fun updateCollectedCount() {
        binding.collectedCountText.text = "$collectedPepero"
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::progressAnimator.isInitialized) {
            progressAnimator.cancel()
        }
    }
}