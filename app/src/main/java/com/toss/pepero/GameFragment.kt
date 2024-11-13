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
import android.widget.FrameLayout
import android.view.animation.AlphaAnimation
import androidx.core.animation.addListener
import com.toss.pepero.databinding.FragmentGameBinding
import kotlin.random.Random

class GameFragment : Fragment() {

    private lateinit var binding: FragmentGameBinding
    private lateinit var progressAnimator: ValueAnimator
    private lateinit var gameContainer: FrameLayout
    private val totalTimeInMillis = 10000L
    private var collectedPepero = 0
    private val itemsMap = mutableMapOf<ImageView, Boolean>()
    private val itemResources = listOf(
        R.drawable.ic_bat,
        R.drawable.ic_bat,
        R.drawable.ic_break_through,
        R.drawable.ic_break_through,
        R.drawable.ic_chopstick,
        R.drawable.ic_chopstick,
        R.drawable.ic_pepero,
        R.drawable.ic_pepero,
        R.drawable.ic_pepero,
        R.drawable.ic_tanghulu,
        R.drawable.ic_tanghulu,
        R.drawable.ic_wooden_stick,
        R.drawable.ic_wooden_stick
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        gameContainer = binding.flGame

        setupSeekBar()
        startSmoothTimer()

        gameContainer.post {
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
                    gameOver()
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

        if (gameContainer.width <= 0 || gameContainer.height <= 0) {
            gameContainer.post { generateItem() }
            return
        }

        val imageView = ImageView(requireContext()).apply {
            val size = dpToPx(115)
            layoutParams = FrameLayout.LayoutParams(size, size)
        }

        val maxX = maxOf(gameContainer.width - dpToPx(115), 0)
        val maxY = maxOf(gameContainer.height - dpToPx(115), 0)

        var randomX: Int
        var randomY: Int
        var overlap: Boolean

        do {
            randomX = Random.nextInt(0, maxX)
            randomY = Random.nextInt(0, maxY)
            overlap = false

            for ((existingView, _) in itemsMap) {
                val existingLeft = existingView.left
                val existingTop = existingView.top
                val existingRight = existingLeft + existingView.width
                val existingBottom = existingTop + existingView.height

                if (randomX < existingRight && randomX + dpToPx(115) > existingLeft &&
                    randomY < existingBottom && randomY + dpToPx(115) > existingTop) {
                    overlap = true
                    break
                }
            }
        } while (overlap)

        (imageView.layoutParams as FrameLayout.LayoutParams).apply {
            leftMargin = randomX
            topMargin = randomY
        }

        val randomResource = itemResources.random()
        imageView.setImageResource(randomResource)
        itemsMap[imageView] = (randomResource == R.drawable.ic_pepero)

        imageView.setOnClickListener {
            if (itemsMap[imageView] == true) {
                collectedPepero++
                showPlusOne(randomX.toFloat(), randomY.toFloat())
                updateCollectedCount()

                imageView.setOnClickListener(null)

                val fadeOut = AlphaAnimation(1f, 0f).apply {
                    duration = 200
                    setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                        override fun onAnimationStart(animation: android.view.animation.Animation?) {}

                        override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                            gameContainer.removeView(imageView)
                            itemsMap.remove(imageView)
                        }

                        override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                    })
                }

                imageView.startAnimation(fadeOut)

                if (collectedPepero >= 3) {
                    progressAnimator.cancel()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SuccessFragment())
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
                }
            } else {
                val failureReason = when (randomResource) {
                    R.drawable.ic_bat -> "앗, 방망이를 주워서 아쉽게 실패!"
                    R.drawable.ic_break_through -> "앗, 뚫어뻥을 주워서 아쉽게 실패!"
                    R.drawable.ic_chopstick -> "앗, 젓가락을 주워서 아쉽게 실패!"
                    R.drawable.ic_tanghulu -> "앗, 탕후루를 주워서 아쉽게 실패!"
                    R.drawable.ic_wooden_stick -> "앗, 나뭇가지를 주워서 아쉽게 실패!"
                    else -> "앗, 다른 아이템을 주워서 아쉽게 실패!"
                }
                gameOver(failureReason)
            }
        }

        gameContainer.addView(imageView)

        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 300
        imageView.startAnimation(fadeIn)

        imageView.postDelayed({
            if (isAdded) {
                val fadeOutAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
                    duration = 200

                    addUpdateListener { animator ->
                        val alphaValue = animator.animatedValue as Float
                        imageView.alpha = alphaValue

                        if (alphaValue <= 0.5f && imageView.hasOnClickListeners()) {
                            imageView.setOnClickListener(null)
                        }
                    }

                    addListener(onEnd = {
                        gameContainer.removeView(imageView)
                        itemsMap.remove(imageView)
                    })
                }

                fadeOutAnimator.start()
            }
        }, 500)

        val nextDelay = Random.nextLong(100, 300)
        gameContainer.postDelayed({ generateItem() }, nextDelay)
    }

    private fun gameOver(reason: String = "시간 초과로 아쉽게 실패!") {
        progressAnimator.cancel()
        val fragment = GameOverFragment().apply {
            arguments = Bundle().apply {
                putString("FAILURE_REASON", reason)
            }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

    private fun showPlusOne(x: Float, y: Float) {
        val plusOneText = TextView(requireContext()).apply {
            text = "+1"
            textSize = 24f
            setTextColor(resources.getColor(android.R.color.black, null))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = x.toInt()
                topMargin = y.toInt()
            }
        }

        gameContainer.addView(plusOneText)

        ValueAnimator.ofFloat(0f, -100f).apply {
            duration = 1000
            addUpdateListener {
                (plusOneText.layoutParams as FrameLayout.LayoutParams).apply {
                    topMargin = (y + (animatedValue as Float)).toInt()
                    plusOneText.layoutParams = this
                }
                plusOneText.alpha = 1 - (animatedValue as Float / -100f)
            }
            addListener(onEnd = {
                gameContainer.removeView(plusOneText)
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