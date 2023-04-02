package com.example.synaera

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(var fa: FragmentActivity, var chatFragment: ChatFragment, var filesFragment: FilesFragment, var homeFragment: HomeFragment) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return (5)
    }
    override fun createFragment(position: Int): Fragment {



        Log.d("position msg: ", "new position = $position")
        return when(position) {
            0 -> homeFragment
            1 -> filesFragment
            3 -> chatFragment
            4 -> ProfileFragment.newInstance()
            else -> CameraFragment.newInstance()
        }
    }



}