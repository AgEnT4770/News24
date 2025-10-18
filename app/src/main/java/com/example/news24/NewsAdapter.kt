package com.example.news24

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import android.view.LayoutInflater
import android.view.ViewGroup
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

        val userId = Firebase.auth.currentUser?.uid ?: return
        val db = Firebase.firestore
        val favoritesRef = db.collection("users").document(userId).collection("favorites")

        
        favoritesRef.document(article.link.hashCode().toString())
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    article.isFavorite = true
                    binding.favouriteFab.setImageDrawable(
                        ContextCompat.getDrawable(a, R.drawable.ic_favorite)
                    )
                } else {
                    article.isFavorite = false
                    binding.favouriteFab.setImageDrawable(
                        ContextCompat.getDrawable(a, R.drawable.ic_favorite_border)
                    )
                }
            }


        binding.favouriteFab.setOnClickListener {
            val favDoc = favoritesRef.document(article.link.hashCode().toString())
            val currentArticle = FavoriteArticles(article.title, article.link, article.image_url, true)

            if (article.isFavorite) {
                favDoc.delete()
                    .addOnSuccessListener {
                        article.isFavorite = false
                        binding.favouriteFab.setImageDrawable(
                            ContextCompat.getDrawable(a, R.drawable.ic_favorite_border)
                        )
                        Toast.makeText(a, "Removed from favorites", Toast.LENGTH_SHORT).show()
                        notifyItemChanged(position)
                    }
                    .addOnFailureListener {
                        Toast.makeText(a, "Failed to remove favorite", Toast.LENGTH_SHORT).show()
                    }
            } else {
                favDoc.set(currentArticle)
                    .addOnSuccessListener {
                        article.isFavorite = true
                        binding.favouriteFab.setImageDrawable(
                            ContextCompat.getDrawable(a, R.drawable.ic_favorite)
                        )
                        Toast.makeText(a, "Added to favorites", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(a, "Failed to add favorite", Toast.LENGTH_SHORT).show()
                    }
            }

            onFavoriteClicked(article)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = articles.size

    class NewsViewHolder(val binding: ArticleListItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}