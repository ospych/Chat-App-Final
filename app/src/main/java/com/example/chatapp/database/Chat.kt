package com.example.chatapp.database

data class Chat(var senderId:String = "",
                var receiverId:String = "",
                var message:String = "")