package com.example.synaera

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import com.example.synaera.databinding.SliderItemBinding

class OnboardingViewPageAdapter() : PagerAdapter() {

     var list = ArrayList<OnboardingItem>()
     lateinit var context : Context

     companion object {
         fun newInstance(context: Context, list: ArrayList<OnboardingItem>) : OnboardingViewPageAdapter {
             return OnboardingViewPageAdapter(context, list)
         }
     }

     constructor(context: Context, list: ArrayList<OnboardingItem>) : this() {
         this.context = context
         this.list = list
     }

     override fun getCount(): Int {
         return list.size
     }

     override fun isViewFromObject(view: View, `object`: Any): Boolean {
         return view === `object` as LinearLayout
     }

     override fun instantiateItem(container: ViewGroup, position: Int): Any {
         val layoutInflater: LayoutInflater =
             context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
         val binding = SliderItemBinding.inflate(layoutInflater, container, false)

         val currItem = list[position]

         binding.titleImage.setImageResource(currItem.image)
         binding.texttitle.text = currItem.heading
         binding.textdeccription.text = currItem.desc

         container.addView(binding.root)

         return binding.root
     }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }
}