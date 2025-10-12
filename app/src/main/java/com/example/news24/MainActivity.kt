package com.example.news24

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.news24.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
         binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val categoryFromIntent = intent.getStringExtra("category_name") ?: "top"
        loadnews(categoryFromIntent)
    }

    private fun loadnews(category: String) {
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
                Log.d("Trace", "Articles: $articles")
                showNews(articles)
                binding.progress.isVisible = false
            }

            override fun onFailure(call: Call<News>, t: Throwable) {
                Log.d("Trace", "Error: ${t.message}")
                binding.progress.isVisible = false
            }
        })
    }

    private fun showNews(articles: ArrayList<Article>) {
        val adapter = NewsAdapter(this, articles)
        binding.newsList.adapter = adapter
    }

//    override fun onResume() {
//        super.onResume()
//        loadnews(category = categoryFromIntent) // ✅ Refresh news with updated country
//    }

}
