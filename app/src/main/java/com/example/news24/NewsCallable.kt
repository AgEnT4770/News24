package com.example.news24

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsCallable {

    @GET("/api/1/latest")
    fun getNews(
        @Query("apikey") apiKey: String = "pub_2a221485f6e04c939d226af0c2537ef4",
        @Query("country") country: String,
        @Query("category") category: String ,
        @Query("size") size: Int = 10
    ): Call<News>
}
