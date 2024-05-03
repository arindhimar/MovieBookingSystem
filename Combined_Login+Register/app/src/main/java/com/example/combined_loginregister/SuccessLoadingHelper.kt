package com.example.combined_loginregister

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible

class SuccessLoadingHelper {
    private lateinit var dialog: AlertDialog
    private lateinit var view:View
    fun showLoadingDialog(context: Context) {
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.success_dialog, null)

        val builder = AlertDialog.Builder(context)
        builder.setView(view)
        builder.setCancelable(false)

        dialog = builder.create()
        dialog.show()
    }

    fun hideButtons(){
        val btnLayout = view.findViewById<LinearLayout>(R.id.CustomDialogButtonLayout)
        btnLayout.isVisible = false
    }

    fun dismissLoadingDialog() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}