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
        savepayment(100)
    }

    private fun savepayment(amount:Int) {
        val Checkout = Checkout()
        Checkout.setKeyID("rzp_test_W8LTpPgYV93bIS")
        try {
            val options = JSONObject()

            options.put("name", "Razorpay Corp")
            options.put("description", "*************************")
            options.put("theme.color", "#FFBB86FC");
            options.put("currency", "INR");
            options.put("amount", amount * 100)


            val retryObj = JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            Checkout.open(this, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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