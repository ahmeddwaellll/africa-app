package com.example.africanschools.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Deferred

class DeepSeekApiService {
    private val client = OkHttpClient()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.deepseek.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun generateResponse(prompt: String): Deferred<String> {
        return retrofit.create(DeepSeekApi::class.java).generate(prompt)
    }
}
