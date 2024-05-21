package com.example.bottomnavigationtry

import android.os.Bundle
import android.support.v7.widget.AppCompatEditText
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.yalantis.jellytoolbar.listener.JellyListener
import com.yalantis.jellytoolbar.widget.JellyToolbar


class MainActivity : AppCompatActivity() {
    private var toolbar: JellyToolbar? = null
    private var editText: AppCompatEditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        toolbar = findViewById<View>(R.id.toolbar) as JellyToolbar
        toolbar!!.toolbar?.setNavigationIcon(R.drawable.baseline_access_alarm_24)
        toolbar!!.jellyListener = jellyListener

        editText = LayoutInflater.from(this).inflate(R.layout.edit_text, null) as AppCompatEditText
        editText!!.setBackgroundResource(R.color.colorTransparent)
        toolbar!!.contentView = editText

    }

    private val jellyListener: JellyListener = object : JellyListener() {
        override fun onCancelIconClicked() {
            if (TextUtils.isEmpty(editText!!.text)) {
                toolbar!!.collapse()
            } else {
                editText!!.text.clear()
            }
        }
    }
}