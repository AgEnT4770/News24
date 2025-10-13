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

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)

        // RecyclerView setup
        val categories = listOf(
            Category("top"),
            Category("business"),
            Category("sports"),
            Category("technology"),
            Category("entertainment")
        )

        adapter = CategoryAdapter(categories) { category ->
//            Toast.makeText(this, "${category.title} clicked", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            // optionally send the category name to MainActivity
            intent.putExtra("category_name", category.title)
            startActivity(intent)


        }

        binding.recyclerCategories.layoutManager = LinearLayoutManager(this)
        binding.recyclerCategories.adapter = adapter
    }

    // Inflate options menu (3 dots)
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

    // Handle menu actions
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)

                startActivity(intent)
                true
            }
            R.id.action_favorites -> {
                Toast.makeText(this, "Favorites clicked", Toast.LENGTH_SHORT).show()
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
