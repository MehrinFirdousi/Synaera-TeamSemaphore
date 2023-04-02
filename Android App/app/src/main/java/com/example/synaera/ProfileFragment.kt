package com.example.synaera

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.synaera.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance() : ProfileFragment {
            return ProfileFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailParams = binding.emailIcon.layoutParams as ConstraintLayout.LayoutParams
        val nameParams = binding.nameIcon.layoutParams as ConstraintLayout.LayoutParams

        val db = DatabaseHelper(context)
        val user = db.getUser(requireActivity().intent.getIntExtra("id", 0))

        binding.emailValue.text = user.email
        binding.nameValue.text = user.name

        val imm: InputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        binding.profileEditButton.setOnClickListener{
            binding.nameValue.visibility = View.GONE
            binding.emailValue.visibility = View.GONE
            binding.nameLabel.visibility = View.GONE
            binding.emailLabel.visibility = View.GONE

            binding.editName.visibility = View.VISIBLE
            binding.editEmail.visibility = View.VISIBLE

            binding.editName.setText(user.name)
            binding.editEmail.setText(user.email)

            emailParams.topToTop = binding.editEmail.id
            emailParams.bottomToBottom = binding.editEmail.id
            nameParams.topToTop = binding.editName.id
            nameParams.bottomToBottom = binding.editName.id

            binding.saveBttn.visibility = View.VISIBLE

            if(binding.editName.requestFocus()){
                imm.showSoftInput(binding.editName, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        binding.saveBttn.setOnClickListener{
            db.deleteLoggedInUser(user)
            user.name = binding.editName.text.toString()
            user.email = binding.editEmail.text.toString()

            db.addLoggedInUser(user)
            db.updateUser(user, user.id)

            binding.nameValue.visibility = View.VISIBLE
            binding.emailValue.visibility = View.VISIBLE
            binding.nameLabel.visibility = View.VISIBLE
            binding.emailLabel.visibility = View.VISIBLE

            binding.editName.visibility = View.GONE
            binding.editEmail.visibility = View.GONE
            binding.saveBttn.visibility = View.GONE

            binding.emailValue.text = user.email
            binding.nameValue.text = user.name

            emailParams.topToTop = binding.emailLabel.id
            emailParams.bottomToBottom = binding.emailValue.id
            nameParams.topToTop = binding.nameLabel.id
            nameParams.bottomToBottom = binding.nameValue.id

            imm.hideSoftInputFromWindow(binding.editEmail.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        }

        binding.editEmail.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                binding.saveBttn.performClick()
                return@OnKeyListener true
            }
            false
        })

        binding.logoutBttn.setOnClickListener{
            db.deleteLoggedInUser(user)
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }
}