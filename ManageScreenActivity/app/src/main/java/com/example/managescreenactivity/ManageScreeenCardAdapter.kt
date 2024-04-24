package com.example.managescreenactivity

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ManageScreeenCardAdapter  : RecyclerView.Adapter<ManageScreeenCardAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.dislaycard, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        // Implement slide-up animation
        val slideUp = ObjectAnimator.ofFloat(holder.itemView, "translationY", 1000f, 0f)
        slideUp.duration = 500
        slideUp.start()
    }

    override fun getItemCount(): Int {
        // Return the number of card views you want to display
        return 10 // For example, display 10 card views
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}