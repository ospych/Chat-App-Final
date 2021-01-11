package com.example.chatapp.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.database.User
import com.example.chatapp.ui.ChatActivity
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(private val context: Context, private val userList: ArrayList<User>):
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_users, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user = userList[position]

        holder.txtUserName.text = user.userName
        try {
            val storage = FirebaseStorage.getInstance()
            val gsReference = storage.getReferenceFromUrl(user.profileImage)
            if (user.profileImage != " ") {
                gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(context)
                        .load(uri)
                        .into(holder.imgUser)
                }
            } else {
                Glide.with(context)
                    .load(R.drawable.ic_user)
                    .into(holder.imgUser)
            }
        } catch (e: Exception) {
            Log.e("TAG", e.toString())
        }



        holder.layoutUser.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("userId", user.userId)
            intent.putExtra("userName", user.userName)
            intent.putExtra("profileImage", user.profileImage)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val txtUserName: TextView = view.findViewById(R.id.userName)
        val imgUser: CircleImageView = view.findViewById(R.id.userImage)
        val layoutUser: LinearLayout = view.findViewById(R.id.layoutUser)
    }
}