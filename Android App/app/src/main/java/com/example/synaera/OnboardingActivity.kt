package com.example.synaera

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.example.synaera.databinding.ActivityOnboardingBinding


class OnboardingActivity : AppCompatActivity() {
    lateinit var binding : ActivityOnboardingBinding
    lateinit var dots: Array<TextView?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnboardingBinding.inflate(layoutInflater)

        if (!SharedPref.getInstance(applicationContext).isFirstLaunch()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        setContentView(binding.root)

        binding.backbtn.setOnClickListener {
            if (getitem(0) > 0) {
                binding.slideViewPager.setCurrentItem(getitem(-1), true)
            }
        }

        binding.nextbtn.setOnClickListener{

            if (getitem(0) < 2) binding.slideViewPager.setCurrentItem(getitem(1), true) else {
                SharedPref.getInstance(applicationContext).setIsFirstLaunchToFalse()
                val i = Intent(this, LoginActivity::class.java)
                startActivity(i)
                finish()
            }

        }
        binding.skipButton.setOnClickListener {
            SharedPref.getInstance(applicationContext).setIsFirstLaunchToFalse()
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
            finish()
        }

        val list = ArrayList<OnboardingItem>()
        list.add(OnboardingItem("Real-time sign language translation", R.drawable.onboarding_screen1,
            "Use your phone's camera to record ASL signs and display translation"))

        list.add(OnboardingItem("Chat with ASL user", R.drawable.onboarding_screen2,
            "As a non-ASL user, use the chat feature to reply"))

        list.add(OnboardingItem("Caption Generation of ASL", R.drawable.video_caption,
            "Use the upload video feature to get captions generated for a video of a person signing in ASL"))

        val viewPagerAdapter = OnboardingViewPageAdapter(this, list)
        binding.slideViewPager.adapter = viewPagerAdapter
        setUpindicator(0)
        binding.slideViewPager.addOnPageChangeListener(viewListener)
    }

    fun setUpindicator(position: Int) {
        dots = arrayOfNulls(3)
        binding.indicatorLayout.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]!!.text = Html.fromHtml("&#8226")
            dots[i]!!.textSize = 35f
            dots[i]!!.setTextColor(resources.getColor(R.color.black))
            binding.indicatorLayout.addView(dots[i])
        }
        dots[position]!!.setTextColor(resources.getColor(R.color.feldgrau))
    }

    var viewListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            setUpindicator(position)
            if (position > 0) {
                binding.backbtn.visibility = View.VISIBLE
            } else {
                binding.backbtn.visibility = View.INVISIBLE
            }
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    private fun getitem(i: Int): Int {
        return binding.slideViewPager.currentItem + i
    }
}