package com.example.bar.ui.reg

import android.annotation.SuppressLint
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

class RegActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val sharedViewModel: SharedViewModel by viewModels()  // Используем SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg)

        auth = FirebaseAuth.getInstance()

        val regBtn = findViewById<Button>(R.id.button3)
        val emailField = findViewById<EditText>(R.id.editText)
        val passwordField = findViewById<EditText>(R.id.editTextTextPassword)
        val loginButton = findViewById<Button>(R.id.button)
        val regButton = findViewById<Button>(R.id.button3)
        val rPasswordField = findViewById<EditText>(R.id.editTextTextPassword2)

        loginButton.setOnClickListener {
            val intent = Intent(this, RegLogInActivity::class.java)
            startActivity(intent)
            finish()
        }

        regBtn.setOnClickListener {
            val psw = rPasswordField.text.toString()
            var rpsw = passwordField.text.toString()
            if (psw == rpsw) {
                registerNewUser(emailField.text.toString(), passwordField.text.toString(), "")
            }
            else {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("WrongViewCast")
    private fun registerNewUser(email: String, password: String, avatarUrl: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { loginTask ->
                            if (loginTask.isSuccessful) {
                                val randomUsername = findViewById<EditText>(R.id.editText2)
                                val userId = auth.currentUser?.uid

                                val database = FirebaseDatabase.getInstance()
                                val userRef = database.getReference("users").child(userId!!)

                                val userMap = mutableMapOf<String, Any>(
                                    "username" to randomUsername.text.toString(),
                                    "avatar" to avatarUrl,
                                    "role" to "user"
                                )

                                userRef.setValue(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Учётная запись создана и пользователь вошел", Toast.LENGTH_SHORT).show()

                                        userId.let {
                                            sharedViewModel.setUserId(it)  // Устанавливаем userId в ViewModel
                                        }

                                        val intent = Intent(this, Note2Activity::class.java)
                                        intent.putExtra("USER_ID", userId)
                                        startActivity(intent)
                                        finish()
                                    }
                            }
                        }
                } else {
                    Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_LONG).show()
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