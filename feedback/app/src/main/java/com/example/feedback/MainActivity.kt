package com.example.feedback

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import android.widget.Toast


class MainActivity : AppCompatActivity() {

   // var spRRole: Spinner = findViewById(R.id.menu)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       /* val role = arrayOf<String>("Select Role", "Customer", "Staff")
        var roleAdapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            role
        )



        spRRole.adapter = roleAdapter
        spRRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == 0) {
                    Toast.makeText(
                        this@MainActivity,
                        "Please Select Role!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
//                TODO("Not yet implemented")
            }
        }
*/


    }
}