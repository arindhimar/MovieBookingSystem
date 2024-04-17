package com.example.myapplication1

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class MainActivity : AppCompatActivity(),PaymentResultListener {
    lateinit var editamount:EditText
    lateinit var txt:TextView
    lateinit var btnpay:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editamount=findViewById(R.id.amount)
        btnpay=findViewById(R.id.btnpay)
        txt=findViewById(R.id.txt)
        btnpay.setOnClickListener{
            savepayment(editamount.text.toString().trim().toInt())
        }
            Checkout.preload(this@MainActivity)

    }
    private fun savepayment(amount:Int)
    {
        val Checkout=Checkout()
        Checkout.setKeyID("rzp_test_W8LTpPgYV93bIS")
       try {
           val options = JSONObject()

           options.put("name", "Razorpay Corp")
           options.put("description", "*************************")
           options.put("theme.color", "#FFBB86FC");
           options.put("currency", "INR");
           options.put("amount", amount * 10);


           val retryObj = JSONObject();
           retryObj.put("enabled", true);
           retryObj.put("max_count", 4);
           options.put("retry", retryObj);

           Checkout.open(this@MainActivity,options)
       }catch (e:Exception)
       {
        Toast.makeText(this@MainActivity,"Error in payment:"+e.message,Toast.LENGTH_LONG).show()
           e.printStackTrace()
       }









    }

    override fun onPaymentSuccess(p0: String?) {
       txt.text=p0
        txt.setTextColor(Color.GREEN)
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        txt.text=p1
        txt.setTextColor(Color.RED)
    }
}