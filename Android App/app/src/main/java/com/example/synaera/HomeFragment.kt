package com.example.synaera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.synaera.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeFragment() : Fragment() {

    private lateinit var homeBinding: FragmentHomeBinding
    private lateinit var bottomNavigationView : BottomNavigationView
    lateinit var db : DatabaseHelper
    lateinit var user : User

    companion object {
        @JvmStatic
        fun newInstance(db: DatabaseHelper) : HomeFragment {

            return HomeFragment(db)
        }
    }
    constructor(db : DatabaseHelper) : this() {
        this.db = db
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)

        val mainActivity = requireActivity() as MainActivity
        bottomNavigationView = mainActivity.findViewById<BottomNavigationView>(R.id.bottom_nav_bar)

        homeBinding.translateHomeButton.setOnClickListener {
            bottomNavigationView.selectedItemId = R.id.camera_menu_id
        }
        homeBinding.chatHomeButton.setOnClickListener {
            bottomNavigationView.selectedItemId = R.id.chat_menu_id
        }
        homeBinding.uploadHomeButton.setOnClickListener {
            bottomNavigationView.selectedItemId = R.id.gallery_menu_id
        }
        homeBinding.profileImg.setOnClickListener {
            bottomNavigationView.selectedItemId = R.id.profile_menu_id
        }

//        val view = requireActivity().findViewById<View>(R.id.bottom_nav_bar)
//        homeBinding.cardView.shapeAppearanceModel = homeBinding.cardView.shapeAppearanceModel.toBuilder()
//            .setTopLeftCorner(CornerFamily.ROUNDED, 0f)
//            .setBottomLeftCorner(CustomCornerTreatment())
//            .setTopRightCorner(CornerFamily.ROUNDED, 0f)
//            .build()

//        homeBinding.cardView.shapeAppearanceModel = homeBinding.cardView.shapeAppearanceModel.toBuilder()
//            .setTopLeftCorner(CustomCornerTreatment())
//            .setBottomLeftCorner(CornerFamily.ROUNDED, 0f)
//            .setBottomRightCorner(CornerFamily.ROUNDED, 0f)
//            .build()

        return homeBinding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = db.getUser(requireActivity().intent.getIntExtra("id", 0))
        val welcome = "Welcome " + user.name
        homeBinding.welcomeMsg.text = welcome
    }

    fun updateName() {
        user = db.getUser(user.id)
        val welcome = "Welcome " + user.name
        homeBinding.welcomeMsg.text = welcome
    }

}