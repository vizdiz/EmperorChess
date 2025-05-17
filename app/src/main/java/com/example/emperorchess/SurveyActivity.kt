package com.example.emperorchess

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.EditText
import android.widget.Toast
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class SurveyActivity : AppCompatActivity() {
    private lateinit var optionsContainer: LinearLayout
    private lateinit var addOptionButton: Button
    private lateinit var saveButton: Button
    private lateinit var questionEditText: EditText
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.survey)
        
        // Enable the back button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SurveyData", MODE_PRIVATE)

        // Initialize views
        optionsContainer = findViewById(R.id.options_container)
        addOptionButton = findViewById(R.id.btn_add_option)
        saveButton = findViewById(R.id.btn_save)
        questionEditText = findViewById(R.id.edit_question)

        // Set up add option button click listener
        addOptionButton.setOnClickListener {
            addNewOption()
        }

        // Set up save button click listener
        saveButton.setOnClickListener {
            saveSurvey()
        }
    }

    private fun addNewOption() {
        // Inflate the radio option layout
        val optionView = LayoutInflater.from(this).inflate(R.layout.item_radio_option, optionsContainer, false)
        
        // Add the view to the container
        optionsContainer.addView(optionView)
    }

    private fun saveSurvey() {
        val question = questionEditText.text.toString()
        if (question.isEmpty()) {
            Toast.makeText(this, "Please enter a question", Toast.LENGTH_SHORT).show()
            return
        }

        // Create JSON object to store survey data
        val surveyJson = JSONObject().apply {
            put("question", question)
            put("options", JSONArray().apply {
                // Get all option views from the container
                for (i in 0 until optionsContainer.childCount) {
                    val optionView = optionsContainer.getChildAt(i)
                    val optionText = optionView.findViewById<EditText>(R.id.edit_option_text).text.toString()
                    if (optionText.isNotEmpty()) {
                        put(optionText)
                    }
                }
            })
        }

        // Save to SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("survey_${System.currentTimeMillis()}", surveyJson.toString())
        editor.apply()

        Toast.makeText(this, "Survey saved successfully", Toast.LENGTH_SHORT).show()
        finish() // Return to previous activity
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the back button click
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}