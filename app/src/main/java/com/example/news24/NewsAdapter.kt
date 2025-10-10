package com.example.news24

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.news24.databinding.ArticleListItemBinding

class NewsAdapter(val a: Activity, val articles: ArrayList<Article>) :
    Adapter<NewsAdapter.NewsViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewsViewHolder {
        val binding =
            ArticleListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: NewsViewHolder,
        position: Int
    ) {
        holder.binding.articleText.text = articles[position].title
        val url = articles[position].link
        Glide.with(holder.binding.articleImage.context)
            .load(articles[position].image_url)
            .error(R.drawable.broken_image)
            .transition(DrawableTransitionOptions.withCrossFade(1000))
            .into(holder.binding.articleImage)


        holder.binding.articleContainer.setOnClickListener {

            val i = Intent(Intent.ACTION_VIEW, url?.toUri())
            a.startActivity(i)

        }
        holder.binding.shareFab.setOnClickListener {
            ShareCompat
                .IntentBuilder(a)
                .setType("text/plain")
                .setChooserTitle("Share with: ")
                .setText(url)
                .startChooser()
        }

    }

    override fun getItemCount() = articles.size


    class NewsViewHolder(val binding: ArticleListItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}