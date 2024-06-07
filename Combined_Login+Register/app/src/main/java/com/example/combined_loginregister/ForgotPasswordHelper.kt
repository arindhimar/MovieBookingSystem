package com.example.combined_loginregister

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordHelper {
    private lateinit var dialog: AlertDialog
    private lateinit var view: View

    fun showForgotPasswordDialog(context: Context) {
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.forgot_password, null)

        val builder = AlertDialog.Builder(context)
        builder.setView(view)
        builder.setCancelable(true)

        dialog = builder.create()
        dialog.show()

        setupViews(context)
    }

    private fun setupViews(context: Context) {
        val txtEmailId = view.findViewById<TextInputLayout>(R.id.txtEmailId)
        val resetEmailIdBtn = view.findViewById<Button>(R.id.resetEmailIdBtn)

        resetEmailIdBtn.setOnClickListener {
            val email = txtEmailId.editText?.text.toString().trim()
            if (email.isNotEmpty()) {
                if (isValidEmail(email)) {
                    checkIfEmailExistsAndSendReset(context, email)
                } else {
                    showWarning(context, "Enter a valid email")
                }
            } else {
                showWarning(context, "Email field cannot be empty")
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun checkIfEmailExistsAndSendReset(context: Context, email: String) {

        val loadingDialogHelper = LoadingDialogHelper()
        loadingDialogHelper.showLoadingDialog(context)


        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        loadingDialogHelper.dismissLoadingDialog()
                        showWarning(context, "No account found with this email")
                    } else {
                        loadingDialogHelper.dismissLoadingDialog()
                        sendPasswordResetEmail(context, email)
                    }
                } else {
                    loadingDialogHelper.dismissLoadingDialog()
                    showWarning(context, "Error checking email: ${task.exception?.message}")
                }
            }
    }

    private fun sendPasswordResetEmail(context: Context, email: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val successLoadingHelper = SuccessLoadingHelper()
                    successLoadingHelper.showLoadingDialog(context)
                    successLoadingHelper.updateText("Password reset email sent.")
                    successLoadingHelper.hideButtons()

                    val handler = Handler()
                    handler.postDelayed({
                        successLoadingHelper.dismissLoadingDialog()
                    }, 3000)


                    dismissForgotPasswordDialog()
                } else {
                    showWarning(context, "Failed to send reset email.")
                }
            }
    }

    private fun showWarning(context: Context, message: String) {
        val warningLoadingHelper = WarningLoadingHelper()
        warningLoadingHelper.showLoadingDialog(context)
        warningLoadingHelper.hideButtons()
        warningLoadingHelper.updateText(message)

        val handler = Handler()
        handler.postDelayed({
            warningLoadingHelper.dismissLoadingDialog()
        }, 3000)
    }

    fun getView(): View {
        return view
    }

    private fun dismissForgotPasswordDialog() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
