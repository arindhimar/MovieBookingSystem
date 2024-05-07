package com.example.combined_loginregister

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible

class YesOrNoLoadingHelper {
    private lateinit var dialog: AlertDialog
    private lateinit var view:View
    fun showLoadingDialog(context: Context) {
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.yesorno_dialog, null)

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

    fun hideCheckBox(){
        val checkboxLayout = view.findViewById<LinearLayout>(R.id.checkboxLayout)
        checkboxLayout.isVisible = false
    }
    fun updateCheckBoxText(message:String){
        val checkBoxText = view.findViewById<TextView>(R.id.checkBoxText)
        checkBoxText.text = message
    }

    fun getView():View{
        return view
    }

    fun hideText(){
        val txt = view.findViewById<TextView>(R.id.textView2)
        txt.isVisible = false
    }

    fun updateText(message:String){
        val txt = view.findViewById<TextView>(R.id.textView2)
        txt.text = message
    }
    fun dismissLoadingDialog() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}