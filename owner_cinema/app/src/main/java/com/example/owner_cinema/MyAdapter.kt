package com.example.owner_cinema

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val ulist:ArrayList<User> ):RecyclerView.Adapter<MyAdapter.MyViewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewholder {
       val itemView=LayoutInflater.from(parent.context).inflate(R.layout.user_item,parent,false)
        return  MyViewholder(itemView)
    }

    override fun getItemCount(): Int {
     return ulist.size
    }

    override fun onBindViewHolder(holder: MyViewholder, position: Int) {

        val currentitem=ulist[position]
        holder.name.text=currentitem.uname
        holder.email.text=currentitem.uemail
        holder.mno.text= currentitem.umobile.toString()
    }
    class MyViewholder(itemView :View) :RecyclerView.ViewHolder(itemView){
    var name :TextView=itemView.findViewById(R.id.tname)
        var email :TextView=itemView.findViewById(R.id.temail)
        var mno :TextView=itemView.findViewById(R.id.mno)
    }
}