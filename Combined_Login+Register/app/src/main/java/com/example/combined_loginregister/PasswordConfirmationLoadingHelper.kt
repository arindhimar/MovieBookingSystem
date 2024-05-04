package com.example.combined_loginregister

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout

class PasswordConfirmationLoadingHelper {
    private lateinit var dialog: AlertDialog
    private lateinit var view:View
    fun showLoadingDialog(context: Context) {
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.password_verification_dialog, null)

        val builder = AlertDialog.Builder(context)
        builder.setView(view)
        builder.setCancelable(false)

        dialog = builder.create()
        dialog.show()
    }

    fun getPassword():String{
        val password = view.findViewById<TextInputLayout>(R.id.textInputLayout3)
        return password.editText!!.text.toString()
    }

    fun getView():View{
        return view
    }



    fun dismissLoadingDialog() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}