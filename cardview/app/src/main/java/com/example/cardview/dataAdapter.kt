package com.example.cardview


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class dataAdapter(private var list: ArrayList<datalist>) :RecyclerView.Adapter<dataAdapter.MyViewholder>() {

    class MyViewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name)
        val description: TextView = itemView.findViewById(R.id.des)
        val image: ImageView = itemView.findViewById(R.id.imageview)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): dataAdapter.MyViewholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cardview_layout,
            parent,false)
        return MyViewholder(itemView);
    }

    override fun getItemCount(): Int {
      return list.count();
    }

    override fun onBindViewHolder(holder: dataAdapter.MyViewholder, position: Int) {
        val dlist=list[position]
        holder.name.setText(dlist.name);
        holder.description.setText(dlist.desc);
        holder.image.setImageResource(dlist.dataimage);
    }
}