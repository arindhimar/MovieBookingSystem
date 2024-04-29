package com.example.combined_loginregister

import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView

class SlideUpAnimator : RecyclerView.ItemAnimator() {

    override fun animateDisappearance(
        viewHolder: RecyclerView.ViewHolder,
        preLayoutInfo: ItemHolderInfo,
        postLayoutInfo: ItemHolderInfo?
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun animateAppearance(
        viewHolder: RecyclerView.ViewHolder,
        preLayoutInfo: ItemHolderInfo?,
        postLayoutInfo: ItemHolderInfo
    ): Boolean {
        viewHolder.itemView.translationY = viewHolder.itemView.height.toFloat()
        viewHolder.itemView.alpha = 0f
        viewHolder.itemView.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(DecelerateInterpolator())
            .start()
        return true    }

    override fun animatePersistence(
        viewHolder: RecyclerView.ViewHolder,
        preLayoutInfo: ItemHolderInfo,
        postLayoutInfo: ItemHolderInfo
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        preLayoutInfo: ItemHolderInfo,
        postLayoutInfo: ItemHolderInfo
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun runPendingAnimations() {
        TODO("Not yet implemented")
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        TODO("Not yet implemented")
    }

    override fun endAnimations() {
        TODO("Not yet implemented")
    }

    override fun isRunning(): Boolean {
        TODO("Not yet implemented")
    }

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder, payloads: MutableList<Any>): Boolean {
        return false
    }


}