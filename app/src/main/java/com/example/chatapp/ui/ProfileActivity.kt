package com.example.chatapp.ui

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.database.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_users.*
import kotlinx.android.synthetic.main.item_users.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReference: DatabaseReference

    private var filePath: Uri? = null

    private val PICK_IMAGE_REQUEST: Int = 2020

    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        databaseReference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                etUserName.setText(user!!.userName)
                Log.v("Photo in onData:", user.profileImage)

                if (user.profileImage == ""){
                    Glide.with(this@ProfileActivity).load(R.drawable.ic_user).into(userImagePA)
                } else {
                    loadPhoto(user.profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

        })

        imgBackPA.setOnClickListener {
            onBackPressed()
        }

        userImagePA.setOnClickListener {
            chooseImage()
        }

        btnSave.setOnClickListener {
            uploadImage()
            progressBar.visibility = View.VISIBLE
        }

        imgLogout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Do you want to logout?")
            builder.setMessage("You will lose all data")

            builder.setPositiveButton("Yes") { _, _ ->
                val intent = Intent(
                    this@ProfileActivity,
                    LoginActivity::class.java
                )
                startActivity(intent)
                finish()
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_REQUEST)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (data != null) {
                filePath = data.data
                try {
                    val source = filePath?.let { ImageDecoder.createSource(this.contentResolver, it) }
                    if (source != null) {
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        userImagePA.setImageBitmap(bitmap)
                    }

                    btnSave.visibility = View.VISIBLE
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun uploadImage() {
        if (filePath != null) {
            val ref: StorageReference = storageRef.child("image/" + UUID.randomUUID().toString())
            Log.v("Photo URL:", ref.toString())
            ref.putFile(filePath!!)
                    .addOnSuccessListener {

                        val hashMap:HashMap<String,String> = HashMap()
                        hashMap["userName"] = etUserName.text.toString()
                        hashMap["profileImage"] = ref.toString()
                        databaseReference.updateChildren(hashMap as Map<String, Any>)

                        progressBar.visibility = View.GONE
                        Toast.makeText(applicationContext, "Uploaded", Toast.LENGTH_SHORT).show()
                        btnSave.visibility = View.GONE
                }
                    .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(applicationContext, "Not uploaded", Toast.LENGTH_SHORT).show()
                        }
        }
    }

    fun loadPhoto(uri: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val storage = FirebaseStorage.getInstance()
            val gsReference = storage.getReferenceFromUrl(uri)
            gsReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this@ProfileActivity)
                        .load(uri)
                        .into(userImagePA) }
        } catch(e: Exception) {
            Log.e("TAG", e.toString())
        }
    }
}