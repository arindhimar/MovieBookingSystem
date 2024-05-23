package com.example.combined_loginregister

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 4 // Number of tabs/fragments
    }



    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserMainFragment()
            1 -> UserFeedbackFragment()
            2 -> UserFeedbackFragment()
            3-> CommonProfileFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
