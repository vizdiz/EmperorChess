package com.example.emperorchess

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class SurveyListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: SurveyListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_list)

        // Enable the back button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Available Surveys"

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SurveyData", MODE_PRIVATE)

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view)
        emptyTextView = findViewById(R.id.tv_empty)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SurveyListAdapter { surveyKey ->
            // Launch FillSurveyActivity with the selected survey key
            val intent = Intent(this, FillSurveyActivity::class.java)
            intent.putExtra("survey_key", surveyKey)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Load available surveys
        loadSurveys()
    }

    private fun loadSurveys() {
        val allEntries = sharedPreferences.all
        val surveys = mutableListOf<SurveyItem>()

        for ((key, value) in allEntries) {
            if (key.startsWith("survey_") && !key.startsWith("results_")) {
                try {
                    val surveyJson = value as String
                    val surveyObj = JSONObject(surveyJson)
                    val question = surveyObj.getString("question")
                    val options = surveyObj.getJSONArray("options")
                    
                    surveys.add(SurveyItem(key, question, options.length()))
                } catch (e: Exception) {
                    // Skip invalid surveys
                    continue
                }
            }
        }

        if (surveys.isEmpty()) {
            emptyTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.submitList(surveys)
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    data class SurveyItem(
        val key: String,
        val question: String,
        val optionsCount: Int
    )

    inner class SurveyListAdapter(
        private val onSurveyClick: (String) -> Unit
    ) : RecyclerView.Adapter<SurveyListAdapter.ViewHolder>() {
        private var surveys = listOf<SurveyItem>()

        fun submitList(newSurveys: List<SurveyItem>) {
            surveys = newSurveys
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_survey, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val survey = surveys[position]
            holder.bind(survey)
        }

        override fun getItemCount() = surveys.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val questionTextView: TextView = itemView.findViewById(R.id.tv_question)
            private val optionsCountTextView: TextView = itemView.findViewById(R.id.tv_options_count)

            fun bind(survey: SurveyItem) {
                questionTextView.text = survey.question
                optionsCountTextView.text = "${survey.optionsCount} options"

                itemView.setOnClickListener {
                    onSurveyClick(survey.key)
                }
            }
        }
    }
} 