package com.example.news24

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.news24.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val countries = arrayOf("USA", "Egypt", "Indonesia")
        val countryCodes = mapOf("USA" to "us", "Egypt" to "eg", "Indonesia" to "id")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.countrySpinner.adapter = adapter

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val savedCode = prefs.getString("selected_country", "us")
        val savedName = countryCodes.entries.find { it.value == savedCode }?.key ?: "USA"
        binding.countrySpinner.setSelection(countries.indexOf(savedName))

        binding.saveButton.setOnClickListener {
            val selectedName = binding.countrySpinner.selectedItem.toString()
            val selectedCode = countryCodes[selectedName] ?: "us"
            prefs.edit().putString("selected_country", selectedCode).apply()

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        }
    }
}
