package com.example.news24

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.news24.databinding.ActivityHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbar)

        val categories = listOf(
            Category("", R.drawable.top),
            Category("business", R.drawable.bussiness),
            Category("sports", R.drawable.sports),
            Category("technology", R.drawable.tech),
            Category("entertainment", R.drawable.entertainment)
        )

        adapter = CategoryAdapter(categories) { category ->
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(
                "category_name",
                if (category.title.isEmpty()) "top" else category.title
            )
            startActivity(intent)
        }
        binding.recyclerCategories.layoutManager = LinearLayoutManager(this)
        binding.recyclerCategories.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        for (i in 0 until (menu?.size() ?: 0)) {
            val menuItem = menu!!.getItem(i)
            val spanString = SpannableString(menuItem.title)
            spanString.setSpan(
                ForegroundColorSpan(Color.BLACK),
                0,
                spanString.length,
                0
            )
            menuItem.title = spanString
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)

                startActivity(intent)
                true
            }

            R.id.action_favorites -> {
                val intent = Intent(this, FavoritesActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.action_logout -> {
                Firebase.auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
