package com.example.news24

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.news24.databinding.ActivityFavoritesBinding
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var database: AppDatabase
    private lateinit var adapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = AppDatabase.getDatabase(this)


        lifecycleScope.launch {
            val favoriteArticles = database.articleDao().getFavoriteArticles()
            adapter = NewsAdapter(this@FavoritesActivity, ArrayList(favoriteArticles)) { article ->
                lifecycleScope.launch {
                    database.articleDao().setFavorite(article.link, false)
                    val position = adapter.articles.indexOf(article)
                    if (position != -1) {
                        adapter.articles.removeAt(position)
                        adapter.notifyItemRemoved(position)
                    }
                }
            }
            binding.favoritesList.adapter = adapter
            binding.favoritesList.layoutManager = LinearLayoutManager(this@FavoritesActivity)
        }
    }
}