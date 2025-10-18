package com.example.news24

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.news24.databinding.ArticleListItemBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class NewsAdapter(
    private val a: Activity,
    val articles: ArrayList<Article>,
    private val onFavoriteClicked: (Article) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ArticleListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]
        val binding = holder.binding

        binding.articleText.text = article.title
        Glide.with(binding.articleImage.context)
            .load(article.image_url)
            .error(R.drawable.broken_image)
            .transition(DrawableTransitionOptions.withCrossFade(1000))
            .into(binding.articleImage)

        val url = article.link
        binding.articleContainer.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, url.toUri())
            a.startActivity(i)
        }

        binding.shareFab.setOnClickListener {
            ShareCompat.IntentBuilder(a)
                .setType("text/plain")
                .setChooserTitle("Share with: ")
                .setText(url)
                .startChooser()
        }

        // Check Firestore for the true favorite status
        val userId = Firebase.auth.currentUser?.uid
        if (userId == null) {
            // If user isn't logged in, they can't have favorites.
            article.isFavorite = false
            binding.favouriteFab.setImageDrawable(ContextCompat.getDrawable(a, R.drawable.ic_favorite_border))
        } else {
            // Check the database to see what the real state is.
            val db = Firebase.firestore
            val favDoc = db.collection("users").document(userId)
                .collection("favorites")
                .document(article.link.hashCode().toString())

            favDoc.get().addOnSuccessListener { document ->
                val isFavorite = document.exists()
                article.isFavorite = isFavorite
                if (isFavorite) {
                    binding.favouriteFab.setImageDrawable(ContextCompat.getDrawable(a, R.drawable.ic_favorite))
                } else {
                    binding.favouriteFab.setImageDrawable(ContextCompat.getDrawable(a, R.drawable.ic_favorite_border))
                }
            }.addOnFailureListener {
                // If the check fails, assume it's not a favorite.
                article.isFavorite = false
                binding.favouriteFab.setImageDrawable(ContextCompat.getDrawable(a, R.drawable.ic_favorite_border))
            }
        }

        binding.favouriteFab.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }
            val currentArticle = articles[currentPosition]

            val uid = Firebase.auth.currentUser?.uid ?: return@setOnClickListener
            val db = Firebase.firestore
            val favoritesRef = db.collection("users").document(uid).collection("favorites")
            val document = favoritesRef.document(currentArticle.link.hashCode().toString())

            if (currentArticle.isFavorite) {
                document.delete()
                    .addOnSuccessListener {
                        currentArticle.isFavorite = false
                        binding.favouriteFab.setImageDrawable(ContextCompat.getDrawable(a, R.drawable.ic_favorite_border))
                        Toast.makeText(a, "Removed from favorites", Toast.LENGTH_SHORT).show()
                        onFavoriteClicked(currentArticle)
                    }
                    .addOnFailureListener {
                        Toast.makeText(a, "Failed to remove favorite", Toast.LENGTH_SHORT).show()
                    }
            } else {
                val favoriteArticle = FavoriteArticles(currentArticle.title, currentArticle.link, currentArticle.image_url, true)
                document.set(favoriteArticle)
                    .addOnSuccessListener {
                        currentArticle.isFavorite = true
                        binding.favouriteFab.setImageDrawable(ContextCompat.getDrawable(a, R.drawable.ic_favorite))
                        Toast.makeText(a, "Added to favorites", Toast.LENGTH_SHORT).show()
                        onFavoriteClicked(currentArticle) // Notify activity
                    }
                    .addOnFailureListener {
                        Toast.makeText(a, "Failed to add favorite", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun getItemCount() = articles.size

    class NewsViewHolder(val binding: ArticleListItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
