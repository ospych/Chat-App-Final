package com.example.chatapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login_phone.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.concurrent.TimeUnit


class LoginPhoneActivity : AppCompatActivity() {
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    val Tag = "Phone"

    lateinit var mAuth: FirebaseAuth
    var verificationId = ""
    private val KEY_VERIFICATION_ID = "key_verification_id"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_phone)

        auth = FirebaseAuth.getInstance()

        imgBackPLA.setOnClickListener {
            val intent = Intent(this@LoginPhoneActivity,
                    LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        generate_btn.setOnClickListener {
            login_progress_bar.visibility = View.VISIBLE
            generate_btn.visibility = View.INVISIBLE
            verify ()
        }

        login_btn.setOnClickListener {
            login_progress_bar.visibility = View.VISIBLE
            generate_btn.visibility = View.VISIBLE
            authenticate()
        }

        if (verificationId == null && savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
    }

    private fun verificationCallbacks () {
        callbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                login_progress_bar.visibility = View.INVISIBLE
                signIn(credential)
                Log.e(Tag, "Completed verification")
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                login_form_feedback.visibility = View.VISIBLE
                login_progress_bar.visibility = View.INVISIBLE
                login_form_feedback.text = "Something wrong..."
                Log.e(Tag, "Error verification")
            }

            override fun onCodeSent(
                verfication: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verfication, token)
                verificationId = verfication
                var resendToken = token
                login_progress_bar.visibility = View.INVISIBLE
            }

        }
    }

    private fun authenticate() {
        val verifyCode = verification_text.text.toString()

        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
            verificationId!!,
            verifyCode
        )

        signIn(credential)
    }

    private fun signIn(credential: PhoneAuthCredential) {
        val phoneNumber = phone_text.text.toString()
        val completeNumber = "+$phoneNumber"

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user: FirebaseUser? = auth.currentUser
                        val userId:String = user!!.uid

                        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                        val hashMap:HashMap<String,String> = HashMap()
                        hashMap["userId"] = userId
                        hashMap["userName"] = completeNumber
                        hashMap["profileImage"] = ""

                        databaseReference.setValue(hashMap).addOnCompleteListener(this){
                            if (it.isSuccessful){
                                //open home activity
                                phone_text.setText("")
                                verification_text.setText("")
                                val intent = Intent(this@LoginPhoneActivity,
                                    UsersActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                        startActivity(Intent(this, UsersActivity::class.java))
                    }
                }
    }

    private fun verify() {
        verificationCallbacks()

        val phoneNumber = phone_text.text.toString()

        val completeNumber = "+$phoneNumber"
        Log.v(Tag, completeNumber)

        val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(completeNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_VERIFICATION_ID, verificationId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        verificationId = savedInstanceState.getString(KEY_VERIFICATION_ID).toString()
    }
}