package com.example.news24

import androidx.room.PrimaryKey

class FavoriteArticles(
    val title: String? = "",
    val link: String = "",
    val imageUrl: String? = "",
    var isFavorite: Boolean = false,
    val id : String = ""
)