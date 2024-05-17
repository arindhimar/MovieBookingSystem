package com.example.combined_loginregister

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide

class LoadingDialogHelper {
    private var dialog: AlertDialog? = null

    fun showLoadingDialog(context: Context) {
        if (dialog == null) {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.loading_screen, null)

            val imageView = view.findViewById<ImageView>(R.id.imageView)

            Glide.with(context)
                .load(R.drawable.app_logo_loading_animation)
                .into(imageView)

            val builder = AlertDialog.Builder(context)
            builder.setView(view)
            builder.setCancelable(false)

            dialog = builder.create()
        }
        dialog?.show()
    }

    fun dismissLoadingDialog() {
        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }
}
