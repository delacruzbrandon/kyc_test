package com.quest.dao.dataservice

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object AuthClient {
    private const val baseURL = "https://api-uat.unionbankph.com/ubp/uat/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .addInterceptor {

            val original = it.request()
            val requestBuilder = original.newBuilder()
            val authRequest = requestBuilder
                .method(original.method(), original.body())
                .build()

            it.proceed(authRequest)
        }.build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseURL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    val getAPI: API = retrofit.create(API::class.java)
}