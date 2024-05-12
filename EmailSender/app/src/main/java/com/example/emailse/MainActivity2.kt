package com.example.emailse

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


open class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btn = findViewById<Button>(R.id.button)
        btn.setOnClickListener {
            buttonSendEmail(listOf("alvfcoc@gmail.com","bansibhimani99@gmail.com"))
        }

    }

    fun buttonSendEmail( recipients: List<String>) {
        try {
            val stringSenderEmail = "thecinemacub@gmail.com"
            val stringPasswordSenderEmail = "gxfx oybn edba vtlc"

            val stringHost = "smtp.gmail.com"

            val properties: Properties = System.getProperties()

            properties["mail.smtp.host"] = stringHost
            properties["mail.smtp.port"] = "465"
            properties["mail.smtp.ssl.enable"] = "true"
            properties["mail.smtp.auth"] = "true"

            val session: Session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail)
                }
            })

            val mimeMessage = MimeMessage(session)
            for (recipient in recipients) {
                mimeMessage.addRecipient(Message.RecipientType.TO, InternetAddress(recipient))
            }

            mimeMessage.subject = "Subject: Android App email"
            mimeMessage.setText("Hello Programmer, \n\nProgrammer World has sent you this 2nd email. \n\n Cheers!\nProgrammer World")

            val thread = Thread {
                try {
                    Transport.send(mimeMessage)
                } catch (e: MessagingException) {
                    e.printStackTrace()
                }
            }
            thread.start()
        } catch (e: AddressException) {
            e.printStackTrace()
        } catch (e: MessagingException) {
            e.printStackTrace()
        }
    }
}