package com.example.news24

data class News(
    val status: String,
    val results: ArrayList<Article>
)

data class Article(
    val title: String?,
    val link: String?,
    val image_url: String?
)
