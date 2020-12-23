
package com.example.chatapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.adapter.UserAdapter
import com.example.chatapp.database.User
import com.example.chatapp.firebase.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_users.*
import kotlinx.android.synthetic.main.item_users.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class UsersActivity : AppCompatActivity() {
    val userList = ArrayList<User>()
    lateinit var mStorageRef : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        mStorageRef = FirebaseStorage.getInstance().reference

        FirebaseService.sharedPref = getSharedPreferences("sharedPref",Context.MODE_PRIVATE)

        FirebaseInstallations.getInstance().getToken(true).addOnSuccessListener {
            FirebaseService.token = it.token
        }

        userRecyclerView.layoutManager = LinearLayoutManager(this)

        imgBack.setOnClickListener {
            onBackPressed()
        }

        imgProfile.setOnClickListener {
            val intent = Intent(
                    this@UsersActivity,
                    ProfileActivity::class.java
            )
            startActivity(intent)
        }
        getUsersList()
    }

    private fun getUsersList() {
        val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        val userId = firebase.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userId")

        val databaseReference: DatabaseReference =
                FirebaseDatabase.getInstance().getReference("Users")

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()
                    val currentUser = snapshot.getValue(User::class.java)
                    
                    if (currentUser!!.profileImage != "") {
                        loadPhoto(currentUser.profileImage)
                    } else {
                        Toast.makeText(this@UsersActivity, "Image wrong", Toast.LENGTH_SHORT).show()
                    }

                    for (dataSnapShot: DataSnapshot in snapshot.children) {
                        val user = dataSnapShot.getValue(User::class.java)

                        if (user!!.userId != firebase.uid) {
                            userList.add(user)
                        }
                    }

                    val userAdapter = UserAdapter(this@UsersActivity, userList)
                    userRecyclerView.adapter = userAdapter
                }

            })
    }

    fun loadPhoto(uri: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val storage = FirebaseStorage.getInstance()
            val gsReference = storage.getReferenceFromUrl(uri)
            gsReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this@UsersActivity)
                        .load(uri)
                        .into(userImage) }
        } catch(e: Exception) {
            Log.e("TAG", e.toString())
        }
    }

}