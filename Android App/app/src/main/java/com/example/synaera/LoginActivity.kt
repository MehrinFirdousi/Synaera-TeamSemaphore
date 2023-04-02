package com.example.synaera

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.synaera.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    lateinit var users : ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)

        val db = DatabaseHelper(this)

        val loggedInUser = db.getLoggedIn()

        if (loggedInUser.email != "") {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("id", loggedInUser.id)
            startActivity(intent)
            finish()
        }

        setContentView(binding.root)

        binding.signupBttn.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginBttn.setOnClickListener{
            val email = binding.emailEditText.text.toString()
            val password = Hasher.hash(binding.passwordEditText.text.toString())
            var userFound = false

            users = db.getAllUsers()

            for (user in users) {
                if (email.equals(user.email, true) && password == user.password) {
                    userFound = true
                    val intent = Intent(this, MainActivity::class.java)
                    db.addLoggedInUser(user)
                    intent.putExtra("id", user.id)
                    startActivity(intent)
                    finish()
                }
            }
            if (!userFound) {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.passwordEditText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                binding.loginBttn.performClick()
                return@OnKeyListener true
            }
            false
        })
    }


}