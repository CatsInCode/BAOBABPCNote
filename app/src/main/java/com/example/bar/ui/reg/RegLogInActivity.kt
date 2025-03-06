package com.example.bar.ui.reg

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.bar.Note2Activity
import com.example.bar.R
import com.example.bar.ui.SharedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegLogInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val sharedViewModel: SharedViewModel by viewModels()  // Используем SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg_log_in)

        auth = FirebaseAuth.getInstance()

        val emailField = findViewById<EditText>(R.id.editText)
        val passwordField = findViewById<EditText>(R.id.editTextTextPassword)
        val loginButton = findViewById<Button>(R.id.button)
        val regButton = findViewById<Button>(R.id.button3)
        val avatarField = findViewById<EditText>(R.id.ava)

        regButton.setOnClickListener {
            val intent = Intent(this, RegActivity::class.java)
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val avatarUrl = avatarField.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginOrRegister(email, password, avatarUrl)
            } else {
                Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginOrRegister(email: String, password: String, avatarUrl: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        sharedViewModel.setUserId(it) // Устанавливаем userId в ViewModel

                        val intent = Intent(this, Note2Activity::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Registration...", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, RegActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
    }

    /*private fun updateAvatar(avatarUrl: String) {
        val userId = auth.currentUser?.uid
        if (avatarUrl.isNotEmpty() && userId != null) {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(userId)

            val userMap = mapOf("avatar" to avatarUrl)

            userRef.updateChildren(userMap)
        }
    }

     */
}
