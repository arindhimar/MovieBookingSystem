package com.example.instamojo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.instamojo.android.Instamojo
import com.instamojo.android.InstamojoApplication
import com.instamojo.android.callbacks.*
import com.instamojo.android.models.*

class MainActivity : AppCompatActivity(), PaymentResponseCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize Instamojo with your API key
        Instamojo.init(this, "your_api_key")

        // Create a payment request
        val paymentRequest = PaymentRequest()
        paymentRequest.purpose = "Test Payment"
        paymentRequest.amount = 1000 // amount in paise
        paymentRequest.buyerName = "John Doe"
        paymentRequest.email = "johndoe@example.com"
        paymentRequest.phone = "9876543210"
        paymentRequest.redirectUrl = "https://your-redirect-url.com"
        paymentRequest.webhookUrl = "https://your-webhook-url.com"

        // Start the payment flow
        Instamojo.startPayment(this, paymentRequest, this)
    }

    // Implement the PaymentResponseCallback interface
    override fun onPaymentSuccess(paymentResponse: PaymentResponse) {
        // Handle successful payment
    }

    override fun onPaymentFailure(error: Error) {
        // Handle payment failure
    }

    override fun onPaymentCancel() {
        // Handle payment cancellation
    }
}