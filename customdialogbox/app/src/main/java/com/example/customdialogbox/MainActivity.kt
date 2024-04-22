package com.example.customdialogbox

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnlogout:Button= findViewById(R.id.dialog_btn)

        btnlogout.setOnClickListener{
            val message: String ="Congratulations! you have successfully create a custom dialog."
            val dialog=Dialog(this)
            dialog.window?.setWindowAnimations(R.style.AnimationsForDailog)
            showcustomdialogbox(message)
        }
    }

    private fun showcustomdialogbox(message:String?){
        val dialog=Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog_layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setWindowAnimations(R.style.AnimationsForDailog)


        val tvmessage:TextView=dialog.findViewById((R.id.textView2))
        val btnyes:Button=dialog.findViewById(R.id.btn_yes)
        val btnno:Button=dialog.findViewById(R.id.btn_no)

        tvmessage.text=message

        btnyes.setOnClickListener{
            Toast.makeText(this,"click on yes",Toast.LENGTH_SHORT).show()
        }

        btnno.setOnClickListener{
            //Toast.makeText(this,"click on no",Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}