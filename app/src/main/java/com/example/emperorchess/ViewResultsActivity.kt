package com.example.emperorchess

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class ViewResultsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: SurveyResultsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_results)

        // Enable the back button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Survey Results"

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SurveyData", MODE_PRIVATE)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SurveyResultsAdapter()
        recyclerView.adapter = adapter

        // Load and display results
        loadResults()
    }

    private fun loadResults() {
        val allEntries = sharedPreferences.all
        val results = mutableListOf<SurveyResult>()

        for ((key, value) in allEntries) {
            if (key.startsWith("results_")) {
                try {
                    val resultsJson = value as String
                    val resultObj = JSONObject(resultsJson)
                    val question = resultObj.getString("question")
                    val resultsData = resultObj.getJSONObject("results")
                    
                    val optionResults = mutableMapOf<String, Int>()
                    val iterator = resultsData.keys()
                    while (iterator.hasNext()) {
                        val option = iterator.next()
                        optionResults[option] = resultsData.getInt(option)
                    }
                    
                    results.add(SurveyResult(question, optionResults))
                } catch (e: Exception) {
                    // Skip invalid results
                    continue
                }
            }
        }

        adapter.submitList(results)
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

    data class SurveyResult(
        val question: String,
        val optionResults: Map<String, Int>
    )

    inner class SurveyResultsAdapter : RecyclerView.Adapter<SurveyResultsAdapter.ViewHolder>() {
        private var results = listOf<SurveyResult>()

        fun submitList(newResults: List<SurveyResult>) {
            results = newResults
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_survey_result, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val result = results[position]
            holder.bind(result)
        }

        override fun getItemCount() = results.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val questionTextView: TextView = itemView.findViewById(R.id.tv_question)
            private val optionsContainer: LinearLayout = itemView.findViewById(R.id.options_container)

            fun bind(result: SurveyResult) {
                questionTextView.text = result.question
                optionsContainer.removeAllViews()

                // Calculate total responses
                val totalResponses = result.optionResults.values.sum()

                // Add result views for each option
                for ((option, count) in result.optionResults) {
                    val percentage = if (totalResponses > 0) {
                        (count.toFloat() / totalResponses) * 100
                    } else {
                        0f
                    }

                    val optionView = LayoutInflater.from(itemView.context)
                        .inflate(R.layout.item_survey_option_result, optionsContainer, false)

                    optionView.findViewById<TextView>(R.id.tv_option).text = option
                    optionView.findViewById<TextView>(R.id.tv_percentage).text = 
                        String.format("%.1f%%", percentage)

                    val progressBar = optionView.findViewById<View>(R.id.progress_bar)
                    progressBar.post {
                        val params = progressBar.layoutParams
                        params.width = ((progressBar.parent as View).width * percentage / 100).toInt()
                        progressBar.layoutParams = params
                    }

                    optionsContainer.addView(optionView)
                }
            }
        }
    }
}