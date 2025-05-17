package com.example.emperorchess

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import android.view.View
import com.google.android.material.button.MaterialButton
import android.widget.LinearLayout

class FillSurveyActivity : AppCompatActivity() {
    private lateinit var questionTextView: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var submitButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private var currentSurveyKey: String = ""
    
    // New views for post-submission state
    private lateinit var surveyContainer: LinearLayout
    private lateinit var successContainer: LinearLayout
    private lateinit var fillAgainButton: MaterialButton
    private lateinit var returnToListButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_survey)

        // Enable the back button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SurveyData", MODE_PRIVATE)

        // Initialize views
        initializeViews()

        // Get the survey key from intent
        currentSurveyKey = intent.getStringExtra("survey_key") ?: ""

        // Load and display the survey
        loadSurvey()

        // Set up button click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        questionTextView = findViewById(R.id.tv_question)
        radioGroup = findViewById(R.id.radio_group)
        submitButton = findViewById(R.id.btn_submit)
        surveyContainer = findViewById(R.id.survey_container)
        successContainer = findViewById(R.id.success_container)
        fillAgainButton = findViewById(R.id.btn_fill_again)
        returnToListButton = findViewById(R.id.btn_return_to_list)
    }

    private fun setupClickListeners() {
        submitButton.setOnClickListener {
            submitResponse()
        }

        fillAgainButton.setOnClickListener {
            resetSurvey()
        }

        returnToListButton.setOnClickListener {
            finish()
        }
    }

    private fun loadSurvey() {
        if (currentSurveyKey.isEmpty()) {
            Toast.makeText(this, "No survey selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val surveyJson = sharedPreferences.getString(currentSurveyKey, null)
        if (surveyJson == null) {
            Toast.makeText(this, "Survey not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        try {
            val survey = JSONObject(surveyJson)
            val question = survey.getString("question")
            val options = survey.getJSONArray("options")

            // Set the question
            questionTextView.text = question

            // Add radio buttons for each option
            for (i in 0 until options.length()) {
                val optionText = options.getString(i)
                val radioButton = android.widget.RadioButton(this).apply {
                    text = optionText
                    id = i
                    textSize = 16f
                    setPadding(32, 24, 32, 24)
                }
                radioGroup.addView(radioButton)
            }

            // Show survey container, hide success container
            surveyContainer.visibility = View.VISIBLE
            successContainer.visibility = View.GONE

        } catch (e: Exception) {
            Toast.makeText(this, "Error loading survey", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun submitResponse() {
        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Get the selected option text
            val selectedOption = radioGroup.findViewById<android.widget.RadioButton>(selectedId).text.toString()

            // Load the survey to get all options
            val surveyJson = sharedPreferences.getString(currentSurveyKey, null)
            val survey = JSONObject(surveyJson)
            val options = survey.getJSONArray("options")

            // Create or update results
            val resultsKey = "results_$currentSurveyKey"
            val resultsJson = sharedPreferences.getString(resultsKey, null)
            val results = if (resultsJson != null) {
                JSONObject(resultsJson)
            } else {
                JSONObject().apply {
                    put("question", survey.getString("question"))
                    put("results", JSONObject())
                }
            }

            // Update the count for the selected option
            val resultsObj = results.getJSONObject("results")
            val currentCount = resultsObj.optInt(selectedOption, 0)
            resultsObj.put(selectedOption, currentCount + 1)

            // Save the updated results
            sharedPreferences.edit().apply {
                putString(resultsKey, results.toString())
                apply()
            }

            // Show success state
            showSuccessState()

        } catch (e: Exception) {
            Toast.makeText(this, "Error submitting response", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSuccessState() {
        // Hide survey container and show success container
        surveyContainer.visibility = View.GONE
        successContainer.visibility = View.VISIBLE
    }

    private fun resetSurvey() {
        // Clear radio button selection
        radioGroup.clearCheck()
        
        // Show survey container and hide success container
        surveyContainer.visibility = View.VISIBLE
        successContainer.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 