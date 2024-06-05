package com.example.combined_loginregister

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
            put("amount", amount * 100)
        }

        try {
            checkout.open(this, paymentOptions)
        } catch (e: JSONException) {
            Log.e("PaymentActivity", "Error in starting Razorpay Checkout", e)
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        val loadingDialogHelper = LoadingDialogHelper()
        loadingDialogHelper.showLoadingDialog(this)
        if (razorpayPaymentID != null) {
            // Gather data
            val showId = intent.getStringExtra("key_showId") ?: ""
            val selectedSeats = intent.getStringArrayListExtra("key_seats")?.joinToString(",") ?: ""

            // Generate bookingId
            val bookingId = FirebaseDatabase.getInstance().reference.push().key ?: ""

            // Set time zone to IST
            val timeZone = TimeZone.getTimeZone("Asia/Kolkata")

            // Get current date and time in IST
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                this.timeZone = timeZone
            }
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
                this.timeZone = timeZone
            }

            val bookingDate = dateFormat.format(Date())
            val bookingTime = timeFormat.format(Date())

            // Create BookingTb object
            val booking = BookingTb(
                bookingId = bookingId,
                paymentId = razorpayPaymentID,
                bookingDate = bookingDate,
                bookingTime = bookingTime,
                showId = showId,
                userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                bookedSeats = selectedSeats
            )

            // Push to Firebase
            val firebaseRestManager = FirebaseRestManager<BookingTb>()
            firebaseRestManager.addItemWithCustomId(booking, bookingId, FirebaseDatabase.getInstance().getReference("moviedb/bookingtb")) { success, error ->
                loadingDialogHelper.dismissLoadingDialog()
                if (success) {
                    val successLoadingHelper = SuccessLoadingHelper()
                    successLoadingHelper.showLoadingDialog(this)
                    successLoadingHelper.hideButtons()
                    successLoadingHelper.updateText("Ticket has been booked\nCheck ticket in the Booked Ticket Menu!!")

                    // Send broadcast to notify other activity
                    val broadcastIntent = Intent("com.example.BOOKING_CONFIRMED").apply {
                        putExtra("bookingId", bookingId)
                    }
                    sendBroadcast(broadcastIntent)

                    val handler = Handler()
                    handler.postDelayed({
                        successLoadingHelper.dismissLoadingDialog()
                        finish()
                    }, 2000)

                } else {
                    val warningLoadingHelper = WarningLoadingHelper()
                    warningLoadingHelper.showLoadingDialog(this)

                    val handler = Handler()
                    handler.postDelayed({
                        warningLoadingHelper.dismissLoadingDialog()
                        finish()
                    }, 2000)
                }
            }
        } else {
            Toast.makeText(this, "Payment ID is null", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentError(code: Int, description: String?) {
        Toast.makeText(this, "Payment failed: $description", Toast.LENGTH_LONG).show()
    }
}
