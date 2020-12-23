package com.example.chatapp.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        btnLogin.setOnClickListener {
            progress_bar.visibility = View.VISIBLE
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
                Toast.makeText(
                        applicationContext,
                        "email and password are required",
                        Toast.LENGTH_SHORT
                ).show()
            } else {
                auth!!.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) {
                            if (it.isSuccessful) {
                                progress_bar.visibility = View.INVISIBLE
                                etEmail.setText("")
                                etPassword.setText("")
                                val intent = Intent(
                                        this@LoginActivity,
                                        UsersActivity::class.java
                                )
                                startActivity(intent)
                                finish()
                            } else {
                                progress_bar.visibility = View.INVISIBLE
                                Toast.makeText(
                                        applicationContext,
                                        "email or password invalid",
                                        Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
            }
        }

        btnSignUp.setOnClickListener {
            val intent = Intent(
                    this@LoginActivity,
                    SignUpActivity::class.java
            )
            startActivity(intent)
            finish()
        }

        btnSignUpPhone.setOnClickListener {
            val intent = Intent(
                this@LoginActivity,
                LoginPhoneActivity::class.java
            )
            startActivity(intent)
            finish()
        }
    }
}