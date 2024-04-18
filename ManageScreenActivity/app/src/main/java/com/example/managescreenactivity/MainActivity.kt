package com.example.managescreenactivity



import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cardView: MaterialCardView = findViewById(R.id.MainLabelCinema)

        // Initially set the card view to be invisible and positioned below the screen
        cardView.visibility = View.INVISIBLE
        cardView.translationY = cardView.height.toFloat()

        // Create an ObjectAnimator to animate the translationY property of the cardView
        val animator = ObjectAnimator.ofFloat(cardView, "translationY", cardView.height.toFloat(), 0f).apply {
            duration = 1200 // Set the duration of the animation
            startDelay = 200 // Optional delay before starting the animation
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {
                    cardView.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(p0: Animator) {
                    TODO("Not yet implemented")
                }

                override fun onAnimationCancel(p0: Animator) {
                    TODO("Not yet implemented")
                }

                override fun onAnimationRepeat(p0: Animator) {
                    TODO("Not yet implemented")
                }

            })
        }

        // Start the animation
        animator.start()
    }
}
