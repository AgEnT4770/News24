package com.example.news24

import retrofit2.Call
import retrofit2.http.GET

interface NewsCallable {

    @GET("/v2/top-headlines?country=us&category=general&apiKey=d18253509e2840d89b70a994e29dfd0b&pageSize=30")
    fun getNews(): Call<News>


}