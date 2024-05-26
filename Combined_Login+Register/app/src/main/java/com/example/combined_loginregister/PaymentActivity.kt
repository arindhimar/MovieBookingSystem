package com.example.combined_loginregister

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class PaymentActivity : AppCompatActivity(), PaymentResultListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        Checkout.preload(applicationContext)

        // Initialize Razorpay checkout
        val checkout = Checkout()

        // Set payment options
        val options = JSONObject()
        options.put("name", "TheCinemaCub")
        options.put("description", "Payment for seats")
        // You can get the amount from your server
        options.put("amount", 500 * 100) // 500 rupees
        options.put("image", R.drawable.application_logo)
        options.put("theme.color", "#3399ff")
        options.put("prefill.contact", "8888888888")
        options.put("prefill.email", "test@example.com")

        // Open checkout activity
        checkout.open(this, options)
    }

    override fun onPaymentSuccess(p0: String?) {
        // Payment successful, handle the success case
        Log.d("PaymentSuccess", "Payment Successful")
        // You can send the payment ID and order ID to your server for verification
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        // Payment failed, handle the error case
        Log.d("PaymentError", "Payment Error: $p1")
    }
}