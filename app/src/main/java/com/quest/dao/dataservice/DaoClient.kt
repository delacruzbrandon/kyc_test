package com.quest.dao.dataservice

import com.quest.dao.MainActivity
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DaoClient {

    private val BASE_URL = "https://netverify.com/"
    private val client = OkHttpClient.Builder()
        .addInterceptor {
            val credentials: String = Credentials.basic(MainActivity.KEY_API_TOKEN, MainActivity.KEY_API_SECRET)

            val requestBuilder = it.request().newBuilder()
            val authRequest = requestBuilder
                .addHeader("Authorization", credentials)
                .build()

            it.proceed(authRequest)
        }
        .build()
    
    private val retrofitBuilder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val getAPI: API = retrofitBuilder.create(API::class.java)

}