package com.example.registeruser
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.example.registeruser.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var front_animation: AnimatorSet
    var isFront = true
    lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val scale = applicationContext.resources.displayMetrics.density
        val front: CardView = findViewById(R.id.card_front)
        val flip = findViewById<Button>(R.id.loginbtn)

        front.cameraDistance = 8000 * scale

        // Load the rotate_360 animation for the front card
        front_animation = AnimatorInflater.loadAnimator(applicationContext, R.animator.animation360) as AnimatorSet

        // Set the event listener for the button
        flip.setOnClickListener {
            // Set the animation target to the front card
            front_animation.setTarget(front)

            // Start the animation
            front_animation.start()

            // Toggle the card state
            isFront = !isFront

            switchControls()

        }

        binding.loginbtn2.setOnClickListener {
            front_animation.setTarget(front)

            // Start the animation
            front_animation.start()

            // Toggle the card state
            isFront = !isFront

            switchControls()
        }

        // Trigger the animation once to show the back of the card initially
        flip.performClick()
        flip.performClick()
    }


    fun switchControls() {
        val currentVisibility = binding.textInputLayout1.isVisible

        binding.textInputLayout1.isVisible = !currentVisibility
        binding.textInputLayout2.isVisible = !currentVisibility
        binding.textInputLayout3.isVisible = !currentVisibility
        binding.textInputLayout4.isVisible = currentVisibility
        binding.textInputLayout5.isVisible = currentVisibility
        binding.textInputLayout6.isVisible = currentVisibility
        binding.loginbtn.isVisible = !currentVisibility
        binding.loginbtn2.isVisible = currentVisibility
        binding.loginbtn3.isVisible = currentVisibility
    }


}
