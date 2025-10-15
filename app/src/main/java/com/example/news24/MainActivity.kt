package com.example.news24

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.news24.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = AppDatabase.getDatabase(this)

        val categoryFromIntent = intent.getStringExtra("category_name") ?: "top"
        loadNews(categoryFromIntent)

        binding.swipeRefresh.setOnRefreshListener { loadNews(categoryFromIntent) }
    }

    private fun loadNews(category: String) {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val selectedCountry = prefs.getString("selected_country", "us") ?: "us"

        val retrofit = Retrofit
            .Builder()
            .baseUrl("https://newsdata.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val c = retrofit.create(NewsCallable::class.java)
        c.getNews(
            country = selectedCountry,
            category = category,
            size = 10
        ).enqueue(object : Callback<News> {

            override fun onResponse(call: Call<News>, response: Response<News>) {
                val news = response.body()
                val articles = news?.results ?: arrayListOf()
                lifecycleScope.launch {
                    val favoriteArticles = database.articleDao().getFavoriteArticles()
                    val favoriteLinks = favoriteArticles.map { it.link }.toSet()
                    articles.forEach { it.isFavorite = favoriteLinks.contains(it.link) }
                    Log.d("Trace", "Articles: $articles")
                    showNews(articles)
                    binding.progress.isVisible = false
                    binding.swipeRefresh.isRefreshing = false
                }
            }

            override fun onFailure(call: Call<News>, t: Throwable) {
                Log.d("Trace", "Error: ${t.message}")
                binding.progress.isVisible = false
                binding.swipeRefresh.isRefreshing = false
            }
        })
    }

    private fun showNews(articles: ArrayList<Article>) {
        val adapter = NewsAdapter(this, articles) {
            lifecycleScope.launch {
                if (it.isFavorite) {
                    database.articleDao().insert(it)
                } else {
                    database.articleDao().setFavorite(it.link, false)
                }
            }
        }
        binding.newsList.adapter = adapter
    }

}
