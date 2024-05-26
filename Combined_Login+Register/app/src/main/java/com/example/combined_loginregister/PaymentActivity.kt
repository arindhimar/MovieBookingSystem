package com.example.combined_loginregister

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONException
import org.json.JSONObject

class PaymentActivity : AppCompatActivity(), PaymentResultListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val checkout = Checkout()
        checkout.setKeyID("rzp_test_bsU4eAerb7lXGD")

        val amount = intent.getIntExtra("key_amount", 0)


        val paymentOptions = JSONObject().apply {
            put("name", "TheCinemaCub")
            put("description", "Payment for the movie")
            put("currency", "INR")
            put("amount", amount*100)
        }

        try {
            checkout.open(this, paymentOptions)
        } catch (e: JSONException) {
            Log.e("PaymentActivity", "Error in starting Razorpay Checkout", e)
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {

        val firebaseRestManager

        Toast.makeText(this, "Payment Successful: $razorpayPaymentID", Toast.LENGTH_LONG).show()


    }

    override fun onPaymentError(code: Int, description: String?) {
        Toast.makeText(this, "Payment failed: $description", Toast.LENGTH_LONG).show()
    }
}
