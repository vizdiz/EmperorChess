package com.example.emperorchess

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    private lateinit var cardSurveyCreation: CardView
    private lateinit var cardFillSurvey: CardView
    private lateinit var cardViewResults: CardView
    private lateinit var cardAccounting: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        initViews()

        // Set click listeners
        setupClickListeners()
    }

    private fun initViews() {
        cardSurveyCreation = findViewById(R.id.cardSurveyCreation)
        cardFillSurvey = findViewById(R.id.cardFillSurvey)
        cardViewResults = findViewById(R.id.cardViewResults)
        cardAccounting = findViewById(R.id.cardAccounting)
    }

    private fun setupClickListeners() {
        // Survey Creation button - Admin feature
        cardSurveyCreation.setOnClickListener {
            // You might want to check if the user has admin rights here
            if (isAdmin()) {
                val intent = Intent(this, SurveyActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Admin access required", Toast.LENGTH_SHORT).show()
            }
        }

        // Fill Survey button - User feature
        cardFillSurvey.setOnClickListener {
            val intent = Intent(this, SurveyListActivity::class.java)
            startActivity(intent)
        }

        // View Results button
        cardViewResults.setOnClickListener {
            val intent = Intent(this, ViewResultsActivity::class.java)
            startActivity(intent)
        }

        // Accounting System button
        cardAccounting.setOnClickListener {
            // Check for appropriate permissions
            if (hasAccountingAccess()) {
                val intent = Intent(this, AccountingActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Accounting access required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Check if current user has admin rights
     * This is just a placeholder - implement your own authentication logic
     */
    private fun isAdmin(): Boolean {
        // TODO: Replace with actual admin check logic
        // For example, check user role from your authentication system
        return true // For testing purposes, return true
    }

    /**
     * Check if current user has accounting access
     * This is just a placeholder - implement your own permission logic
     */
    private fun hasAccountingAccess(): Boolean {
        // TODO: Replace with actual permission check logic
        return true // For testing purposes, return true
    }
}