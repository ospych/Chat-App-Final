package com.example.chatapp

import com.example.chatapp.constants.Constants.Companion.BASE_URL
import com.example.chatapp.`interface`.NotificationApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {

    companion object{
        private val retrofit by lazy {
            Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }

        val api by lazy {
            retrofit.create(NotificationApi::class.java)
        }
    }
}