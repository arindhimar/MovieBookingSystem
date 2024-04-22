package com.example.internetstatus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    lateinit var no_internet_bar:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        no_internet_bar=findViewById(R.id.no_internet_bar)
        setupNetworkListener()
    }
    private fun setupNetworkListener() {
        NetworkReceiver.networkReceiverListener = object : NetworkReceiver.NetworkReceiverListener {
            override fun onNetworkConnectionChanged(isConnected: Boolean) {
                toggleNoInternetBar(!isConnected)
            }


            private fun toggleNoInternetBar(display: Boolean) {
                if (display) {
                    val enterAnim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.enter_from_bottom)
                    no_internet_bar.startAnimation(enterAnim)
                } else {
                    val exitAnim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.exit_to_bottom)
                    no_internet_bar.startAnimation(exitAnim)
                }
                no_internet_bar.visibility = if (display) View.VISIBLE else View.GONE
            }
            private var networkReceiver: NetworkReceiver? = null

            fun onStart() {
               // super.onStart()
                networkReceiver = NetworkReceiver()
                registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
            }

            fun onStop() {
               // super.onStop()
                networkReceiver?.let { unregisterReceiver(it) }
            }




        }
    }
}