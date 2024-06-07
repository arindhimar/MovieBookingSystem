package com.example.combined_loginregister

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible

class RegisterOrForgotPasswordHelper {
    private lateinit var dialog: AlertDialog
    private lateinit var view:View
    fun showRegisterOrForgotPasswordDialog(context: Context) {
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.register_or_forget_password_dialog, null)

        val builder = AlertDialog.Builder(context)
        builder.setView(view)
        builder.setCancelable(true)

        dialog = builder.create()
        dialog.show()



    }




    fun getView():View{
        return view
    }


    fun dismissRegisterOrForgotPasswordDialog() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}