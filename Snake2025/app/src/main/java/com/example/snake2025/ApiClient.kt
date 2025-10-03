package com.example.snake2025


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://quotes-api12.p.rapidapi.com/"
    private const val API_KEY = "4bf620614dmshb191b15e7b65124p19ef4bjsn296bb4a7552d"
    private const val API_HOST = "quotes-api12.p.rapidapi.com"

    fun create(): QuoteApiService {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("x-rapidapi-key", API_KEY)
                    .addHeader("x-rapidapi-host", API_HOST)
                    .build()
                chain.proceed(newRequest)
            }
            .addInterceptor(logger)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuoteApiService::class.java)
    }
}
