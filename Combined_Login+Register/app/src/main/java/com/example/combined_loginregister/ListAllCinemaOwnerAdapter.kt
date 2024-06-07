package com.example.combined_loginregister

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ListAllUserAdapter(private var userList: List<UserTb>) : RecyclerView.Adapter<ListAllUserAdapter.UserViewHolder>() {

    private lateinit var onItemClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(user: UserTb)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_list_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        Log.d("TAG", "onBindViewHolder: ${user.uid}")

        // Only bind data if user type is "cinemaowner"
        if (user.utype == "cinemaowner") {
            holder.SubHeading1.text = "Name : ${user.uname}"
            holder.SubHeading3.text = "Mobile : ${user.umobile}"

            Glide.with(holder.itemView.context)
                .load(R.drawable.account)
                .into(holder.ProfilePicture)

            holder.itemView.setOnClickListener {
                onItemClickListener.onItemClick(user)
            }
        } else {
            // Hide views if not cinemaowner
//            holder.itemView.visibility = View.GONE
            // or
             holder.itemView.visibility = View.INVISIBLE
        }
    }
    override fun getItemCount(): Int {
        // Return the count of cinemaowners only
//        return userList.count { it.utype == "cinemaowner" }
        return userList.count()
    }

    fun updateList(newList: List<UserTb>) {
        userList = ArrayList(newList)
        notifyDataSetChanged()
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ProfilePicture: ImageView = itemView.findViewById(R.id.MainPoster)
        val SubHeading1: TextView = itemView.findViewById(R.id.subheading1)
        val SubHeading2: TextView = itemView.findViewById(R.id.subheading2)
        val SubHeading3: TextView = itemView.findViewById(R.id.subheading3)
    }
}
