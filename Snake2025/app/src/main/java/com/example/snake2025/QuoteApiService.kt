package com.example.snake2025

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


data class JokeResponse(
    val joke: String
)

interface QuoteApiService {
    @GET("dev-jokes")
    fun getJokes(
        @Query("category") category: String = "all",
        @Query("subcategory") subcategory: String = "javascript"
    ): Call<List<JokeResponse>>
}