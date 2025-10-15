package com.example.news24

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: Article)

    @Query("SELECT * FROM articles WHERE isFavorite = 1")
    suspend fun getFavoriteArticles(): List<Article>

    @Query("UPDATE articles SET isFavorite = :isFavorite WHERE link = :link")
    suspend fun setFavorite(link: String, isFavorite: Boolean)
}
