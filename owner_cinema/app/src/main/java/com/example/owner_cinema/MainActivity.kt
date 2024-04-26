package com.example.owner_cinema

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var dbref: DatabaseReference
    private  lateinit var recyclerView: RecyclerView
    private lateinit var useral:ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)






        recyclerView=findViewById(R.id.recycleview)
        recyclerView.layoutManager= LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        useral= arrayListOf()
        dbref= FirebaseDatabase.getInstance().getReference("usertb")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    for (userSnapshot in snapshot.children){
                        val user=userSnapshot.getValue(User::class.java)
                        if (!useral.contains(user))
                        {
                            useral.add(user!!)
                        }

                    }
                    recyclerView.adapter=MyAdapter(useral)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity,"not visible data",Toast.LENGTH_LONG).show()
            }
        })


        }


}