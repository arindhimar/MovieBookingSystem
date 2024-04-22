package com.example.nointernet

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nointernet.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.pendulum.NoInternetDialogPendulum
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal
import org.imaginativeworld.oopsnointernet.snackbars.fire.NoInternetSnackbarFire

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // No Internet Snackbar: Fire
        NoInternetSnackbarFire.Builder(
            binding.mainContainer,
            lifecycle
        ).apply {
            snackbarProperties.apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        // ...
                    }
                }

                duration = Snackbar.LENGTH_INDEFINITE // Optional
                noInternetConnectionMessage = "No active Internet connection!" // Optional
                onAirplaneModeMessage = "You have turned on the airplane mode!" // Optional
                snackbarActionText = "Settings" // Optional
                showActionToDismiss = false // Optional
                snackbarDismissActionText = "OK" // Optional
            }
        }.build()

//        NoInternetDialogPendulum.Builder(
//            this,
//            lifecycle
//        ).apply {
//            dialogProperties.apply {
//                connectionCallback = object : ConnectionCallback { // Optional
//                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
//                        // ...
//                    }
//                }
//
//                cancelable = false // Optional
//                noInternetConnectionTitle = "No Internet" // Optional
//                noInternetConnectionMessage =
//                    "Check your Internet connection and try again." // Optional
//                showInternetOnButtons = true // Optional
//                pleaseTurnOnText = "Please turn on" // Optional
//                wifiOnButtonText = "Wifi" // Optional
//                mobileDataOnButtonText = "Mobile data" // Optional
//
//                onAirplaneModeTitle = "No Internet" // Optional
//                onAirplaneModeMessage = "You have turned on the airplane mode." // Optional
//                pleaseTurnOffText = "Please turn off" // Optional
//                airplaneModeOffButtonText = "Airplane mode" // Optional
//                showAirplaneModeOffButtons = true // Optional
//            }
//        }.build()
//
//        // No Internet Dialog: Signal
//        NoInternetDialogSignal.Builder(
//            this,
//            lifecycle
//        ).apply {
//            dialogProperties.apply {
//                connectionCallback = object : ConnectionCallback { // Optional
//                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
//                        // ...
//                    }
//                }
//
//                cancelable = false // Optional
//                noInternetConnectionTitle = "No Internet" // Optional
//                noInternetConnectionMessage =
//                    "Check your Internet connection and try again." // Optional
//                showInternetOnButtons = true // Optional
//                pleaseTurnOnText = "Please turn on" // Optional
//                wifiOnButtonText = "Wifi" // Optional
//                mobileDataOnButtonText = "Mobile data" // Optional
//
//                onAirplaneModeTitle = "No Internet" // Optional
//                onAirplaneModeMessage = "You have turned on the airplane mode." // Optional
//                pleaseTurnOffText = "Please turn off" // Optional
//                airplaneModeOffButtonText = "Airplane mode" // Optional
//                showAirplaneModeOffButtons = true // Optional
//            }
//        }.build()

    }
}