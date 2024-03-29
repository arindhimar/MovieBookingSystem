package com.example.myapplication

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoadingDialog {
    private var activity: Activity? = null
    private var dialog: AlertDialog? = null
    private lateinit var builder: AlertDialog.Builder

    fun dialog(myActivity: Activity?) {
        activity = myActivity
    }

    fun startLoadingdialog() {
        builder = AlertDialog.Builder(activity!!)
        val inflater = activity?.layoutInflater
        builder.setView(inflater?.inflate(R.layout.loading, null))
        builder.setCancelable(true)
        dialog = builder.create()
        dialog?.show()
    }

    private fun showIcon(layoutResId: Int) {
        dialog?.dismiss() // Dismiss the previous dialog, if any
        val inflater = activity?.layoutInflater
        builder = AlertDialog.Builder(activity!!)
        builder.setView(inflater?.inflate(layoutResId, null))
        builder.setCancelable(true)
        dialog = builder.create()
        dialog?.show()
    }

    fun successIconShow() {
        showIcon(R.layout.successfull_green)
    }

    fun failIconShow() {
        showIcon(R.layout.fail_red)
    }

    fun InvalidFieldIconShow() {
        showIcon(R.layout.warning_yellow_invalid_field)
    }

    fun startClosingWithDelay(delayMillis: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(delayMillis)
            dismissdialog()
        }
    }

    // Dismiss method
    fun dismissdialog() {
        dialog?.dismiss()
    }
}
