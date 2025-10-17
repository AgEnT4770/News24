package com.example.news24

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.news24.databinding.ActivityFavoritesBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
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

        if(!isOnline(this)) {
            binding.progress.isVisible = true
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
            lifecycleScope.launch {
                binding.progress.isVisible = false
                val favoriteArticles = database.articleDao().getFavoriteArticles()
                adapter =
                    NewsAdapter(this@FavoritesActivity, ArrayList(favoriteArticles)) { article ->
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
        else {
            fetchFavoriteFromFirestore()
        }

    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    private fun fetchFavoriteFromFirestore() {
        binding.progress.isVisible = true

        val userId = Firebase.auth.currentUser?.uid!!

        val db = Firebase.firestore
        val favoritesCollection = db.collection("users").document(userId).collection("favorites")


        favoritesCollection.get()
            .addOnSuccessListener { documents ->
                binding.progress.isVisible = false
                val firestoreArticles = ArrayList<Article>()
                for (document in documents) {
                    val article = Article(
                        title = document.getString("title") ?: "",
                        link = document.getString("link") ?: "",
                        image_url = document.getString("image_url") ?: "",
                        isFavorite = true
                    )
                    firestoreArticles.add(article)
                }
                adapter = NewsAdapter(this, firestoreArticles) { article ->
                    val query = favoritesCollection.whereEqualTo("link", article.link)
                    query.get().addOnSuccessListener { docsToRemove ->
                        if (!docsToRemove.isEmpty) {
                            for (doc in docsToRemove) {
                                favoritesCollection.document(doc.id).delete()
                            }
                            val position = adapter.articles.indexOf(article)
                            if (position != -1) {
                                adapter.articles.removeAt(position)
                                adapter.notifyItemRemoved(position)
                            }
                        }
                    }.addOnFailureListener {
                        Toast.makeText(
                            this@FavoritesActivity,
                            "Failed to remove favorite",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                binding.favoritesList.adapter = adapter
                binding.favoritesList.layoutManager = LinearLayoutManager(this)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Could not fetch Firestore favorites: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }


}