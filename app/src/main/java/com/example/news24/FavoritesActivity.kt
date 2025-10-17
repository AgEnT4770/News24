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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.news24.databinding.ActivityFavoritesBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class FavoritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var adapter: NewsAdapter
    private val favoriteArticles = ArrayList<Article>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()

        if(!isOnline(this)) {
            binding.progress.isVisible = false
            binding.emptyText.isVisible = true
            binding.emptyText.text = "No internet connection"
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()

        } else {
            fetchFavoriteFromFirestore()
        }
    }

    private fun setupRecyclerView() {
        adapter = NewsAdapter(this, favoriteArticles) { article ->
            // The adapter has already deleted this from Firestore.
            // All we need to do is update the UI.
            val position = favoriteArticles.indexOf(article)
            if (position != -1) {
                favoriteArticles.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, favoriteArticles.size)
                if (favoriteArticles.isEmpty()) {
                    binding.emptyText.isVisible = true
                }
            }
        }
        binding.favoritesList.adapter = adapter
        binding.favoritesList.layoutManager = LinearLayoutManager(this)
        binding.emptyTex
        t.text = "No favorites yet."
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
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
        binding.emptyText.isVisible = false

        val userId = Firebase.auth.currentUser?.uid ?: run {
            binding.progress.isVisible = false
            binding.emptyText.isVisible = true
            binding.emptyText.text = "Please log in to see favorites."
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val db = Firebase.firestore
        val favoritesCollection = db.collection("users").document(userId).collection("favorites")

        favoritesCollection.get()
            .addOnSuccessListener { documents ->
                binding.progress.isVisible = false
                val newArticles = ArrayList<Article>()
                for (document in documents) {
                    val article = Article(
                        title = document.getString("title") ?: "",
                        link = document.getString("link") ?: "",
                        image_url = document.getString("image_url") ?: "",
                        isFavorite = true
                    )
                    newArticles.add(article)
                }

                val oldSize = favoriteArticles.size
                favoriteArticles.clear()
                favoriteArticles.addAll(newArticles)
                adapter.notifyItemRangeRemoved(0, oldSize)
                adapter.notifyItemRangeInserted(0, newArticles.size)

                if (favoriteArticles.isEmpty()) {
                    binding.emptyText.isVisible = true
                }
            }
            .addOnFailureListener { exception ->
                binding.progress.isVisible = false
                binding.emptyText.isVisible = true
                binding.emptyText.text = "Could not fetch favorites."
                Toast.makeText(this, "Could not fetch favorites: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}
