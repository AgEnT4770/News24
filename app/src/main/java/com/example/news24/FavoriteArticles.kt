package com.example.news24

import androidx.room.PrimaryKey

class FavoriteArticles(
    val title: String? = "",
    val link: String = "",
    val image_url: String? = "",
    var isFavorite: Boolean = false,
    val id : String = ""
)