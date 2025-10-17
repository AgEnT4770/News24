package com.example.news24

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsCallable {

    @GET("/api/1/latest")
    fun getNews(
        @Query("apikey") apiKey: String = "pub_959a925773184f23b86449b39b37ee69",
        @Query("country") country: String,
        @Query("category") category: String ,
        @Query("size") size: Int = 10
    ): Call<News>
}
