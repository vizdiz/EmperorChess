package com.example.emperorchess.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.emperorchess.R
import com.example.emperorchess.database.AccountingDatabase
import com.example.emperorchess.databinding.FragmentReportsBinding
import com.example.emperorchess.models.Sale
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.NumberFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import android.widget.AutoCompleteTextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import android.widget.ArrayAdapter
import com.example.emperorchess.models.Customer

class ReportsFragment : Fragment() {
    private lateinit var binding: FragmentReportsBinding
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvTotalSales: TextView
    private lateinit var tvAverageSale: TextView
    private lateinit var customerSpinner: AutoCompleteTextView
    private lateinit var dateRangeButton: MaterialButton
    private lateinit var accountingDatabase: AccountingDatabase
    
    private var selectedCustomerId: String? = null
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var customers = listOf<Customer>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        pieChart = binding.pieChart
        barChart = binding.barChart
        tvTotalRevenue = binding.tvTotalRevenue
        tvTotalSales = binding.tvTotalSales
        tvAverageSale = binding.tvAverageSale
        customerSpinner = binding.customerSpinner
        dateRangeButton = binding.dateRangeButton
        accountingDatabase = AccountingDatabase(requireContext())

        setupCustomerFilter()
        setupDateFilter()
        setupCharts()
        updateCharts()
    }

    override fun onResume() {
        super.onResume()
        // Update charts every time fragment becomes visible
        updateCharts()
    }

    private fun setupCustomerFilter() {
        lifecycleScope.launch(Dispatchers.IO) {
            customers = accountingDatabase.getCustomers()
            
            withContext(Dispatchers.Main) {
                val customerNames = listOf("All Customers") + customers.map { it.name }
                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.item_dropdown,
                    customerNames
                )
                
                customerSpinner.setAdapter(adapter)
                customerSpinner.setText("All Customers", false)
                
                customerSpinner.setOnItemClickListener { _, _, position, _ ->
                    selectedCustomerId = if (position == 0) null else customers[position - 1].id
                    updateCharts()
                }
            }
        }
    }

    private fun setupDateFilter() {
        dateRangeButton.setOnClickListener {
            showDateRangePicker()
        }
    }

    private fun showDateRangePicker() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")

        // Set initial date range if exists
        if (startDate != null && endDate != null) {
            builder.setSelection(
                androidx.core.util.Pair(
                    startDate!!.timeInMillis,
                    endDate!!.timeInMillis
                )
            )
        }

        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { selection ->
            startDate = Calendar.getInstance().apply {
                timeInMillis = selection.first
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            
            endDate = Calendar.getInstance().apply {
                timeInMillis = selection.second
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }

            // Update button text
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dateRangeButton.text = "${dateFormat.format(startDate!!.time)} - ${dateFormat.format(endDate!!.time)}"
            
            updateCharts()
        }

        picker.addOnNegativeButtonClickListener {
            // Clear date range
            startDate = null
            endDate = null
            dateRangeButton.text = "Select Date Range"
            updateCharts()
        }

        picker.show(childFragmentManager, "date_picker")
    }

    private fun setupCharts() {
        pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            holeRadius = 50f
            setHoleColor(Color.TRANSPARENT)
            centerText = "Sales by\nProduct"
            setCenterTextSize(14f)
            setCenterTextColor(requireContext().getColor(R.color.emperor_text))
            setExtraOffsets(20f, 20f, 20f, 20f)
        }

        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = requireContext().getColor(R.color.emperor_text)
                textSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        // Format date labels
                        return try {
                            val date = Date(value.toLong())
                            SimpleDateFormat("MM/dd", Locale.getDefault()).format(date)
                        } catch (e: Exception) {
                            ""
                        }
                    }
                }
            }

            axisLeft.apply {
                setDrawGridLines(true)
                textColor = requireContext().getColor(R.color.emperor_text)
                textSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return NumberFormat.getCurrencyInstance().format(value)
                    }
                }
            }

            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun updateCharts() {
        lifecycleScope.launch(Dispatchers.IO) {
            var sales = accountingDatabase.getSales()

            // Apply customer filter
            if (selectedCustomerId != null) {
                sales = sales.filter { it.customerId == selectedCustomerId }
            }

            // Apply date filter
            if (startDate != null && endDate != null) {
                sales = sales.filter { sale ->
                    val saleDate = Sale.parseDate(sale.date)?.time ?: return@filter false
                    saleDate in startDate!!.timeInMillis..endDate!!.timeInMillis
                }
            }

            // Calculate metrics
            val totalRevenue = sales.sumOf { it.totalAmount }
            val totalSales = sales.size
            val averageSale = if (totalSales > 0) totalRevenue / totalSales else 0.0

            withContext(Dispatchers.Main) {
                // Update metrics
                val currencyFormat = NumberFormat.getCurrencyInstance()
                tvTotalRevenue.text = currencyFormat.format(totalRevenue)
                tvTotalSales.text = totalSales.toString()
                tvAverageSale.text = currencyFormat.format(averageSale)

                // Update charts
                updatePieChart(sales)
                updateBarChart(sales)
            }
        }
    }

    private fun updatePieChart(sales: List<Sale>) {
        // Group sales by item name
        val salesByItem = mutableMapOf<String, Double>()
        
        for (sale in sales) {
            for (item in sale.items) {
                val currentTotal = salesByItem[item.name] ?: 0.0
                salesByItem[item.name] = currentTotal + item.total
            }
        }

        // Create pie entries for all items, sorted by value
        val entries = salesByItem.entries
            .sortedByDescending { it.value }
            .map { entry ->
                PieEntry(
                    entry.value.toFloat(),
                    entry.key,
                    entry.key // Add item name as data for legend
                )
            }

        val dataSet = PieDataSet(entries, "").apply {
            // Generate green color variations
            val colors = mutableListOf<Int>()
            val baseColors = listOf(
                requireContext().getColor(R.color.emperor_primary),        // Material Green
                requireContext().getColor(R.color.emperor_primary_dark),   // Darker Green
                requireContext().getColor(R.color.emperor_accent),         // Bright Green
                requireContext().getColor(R.color.emperor_accent_dark),    // Dark Bright Green
                requireContext().getColor(R.color.emperor_text),          // Very Dark Green
                requireContext().getColor(R.color.emperor_text_secondary), // Dark Green
                requireContext().getColor(R.color.emperor_primary_light),  // Light Green
                requireContext().getColor(R.color.emperor_accent_light),   // Light Bright Green
                requireContext().getColor(R.color.emperor_selected),       // Light Green
                requireContext().getColor(R.color.emperor_button_ripple)   // Very Light Green
            )
            
            // Generate variations of the base colors if we need more
            repeat((entries.size / baseColors.size) + 1) { i ->
                baseColors.forEach { baseColor ->
                    val hsv = FloatArray(3)
                    Color.colorToHSV(baseColor, hsv)
                    // Adjust hue and saturation while keeping in green spectrum
                    hsv[0] = (hsv[0] + (i * 3)) % 360 // Small hue adjustment
                    hsv[1] = Math.max(0.3f, Math.min(1f, hsv[1] - (i * 0.1f))) // Saturation
                    hsv[2] = Math.max(0.3f, Math.min(0.9f, hsv[2] - (i * 0.1f))) // Value
                    colors.add(Color.HSVToColor(hsv))
                }
            }

            this.colors = colors.take(entries.size)
            valueTextSize = 12f
            valueTextColor = requireContext().getColor(R.color.emperor_text)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.1f%%", value)
                }
            }
            valueLinePart1Length = 0.4f
            valueLinePart2Length = 0.4f
            valueLineColor = requireContext().getColor(R.color.emperor_text)
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            
            sliceSpace = when {
                entries.size > 15 -> 1f
                entries.size > 10 -> 2f
                else -> 3f
            }
        }

        pieChart.apply {
            data = PieData(dataSet).apply {
                setValueTextSize(12f)
                setValueTextColor(requireContext().getColor(R.color.emperor_text))
            }
            
            // Adjust legend based on number of items
            legend.apply {
                isEnabled = true
                textSize = when {
                    entries.size > 15 -> 10f
                    entries.size > 10 -> 12f
                    else -> 14f
                }
                formSize = 12f
                form = Legend.LegendForm.CIRCLE
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                orientation = if (entries.size > 8) {
                    Legend.LegendOrientation.VERTICAL
                } else {
                    Legend.LegendOrientation.HORIZONTAL
                }
                setDrawInside(false)
                xEntrySpace = 10f
                yEntrySpace = 5f
                yOffset = 20f
                textColor = requireContext().getColor(R.color.emperor_text)
                maxSizePercent = 0.5f // Allow legend to take up to 50% of chart height
            }
            
            // Adjust hole size based on number of items
            isDrawHoleEnabled = true
            holeRadius = when {
                entries.size > 15 -> 40f
                entries.size > 10 -> 45f
                else -> 50f
            }
            
            centerText = "Sales by\nProduct"
            setCenterTextSize(14f)
            setCenterTextColor(requireContext().getColor(R.color.emperor_text))
            
            // Increase minimum offset to accommodate labels
            setExtraOffsets(20f, 20f, 20f, 20f)
            
            animateY(1400)
            invalidate()
        }
    }

    private fun updateBarChart(sales: List<Sale>) {
        if (sales.isEmpty()) {
            barChart.setNoDataText("No sales data available")
            barChart.invalidate()
            return
        }

        // Group sales by date (truncated to day)
        val salesByDate = mutableMapOf<String, Double>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // If we have a date range, create entries for all days in the range
        if (startDate != null && endDate != null) {
            val currentDate = startDate!!.clone() as Calendar
            while (!currentDate.after(endDate)) {
                salesByDate[dateFormat.format(currentDate.time)] = 0.0
                currentDate.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Add sales data
        sales.forEach { sale ->
            try {
                val fullDate = Sale.DATE_FORMAT.parse(sale.date)
                val dateKey = dateFormat.format(fullDate)
                salesByDate[dateKey] = (salesByDate[dateKey] ?: 0.0) + sale.totalAmount
            } catch (e: Exception) {
                // Skip invalid dates
            }
        }

        // Create bar entries
        val entries = salesByDate.entries
            .sortedBy { it.key }
            .mapIndexed { index, (_, total) ->
                BarEntry(index.toFloat(), total.toFloat())
            }

        if (entries.isEmpty()) {
            barChart.setNoDataText("No sales data available")
            barChart.invalidate()
            return
        }

        val dataSet = BarDataSet(entries, "Daily Sales").apply {
            color = requireContext().getColor(R.color.emperor_primary)
            valueTextColor = requireContext().getColor(R.color.emperor_text)
            valueTextSize = 12f
            setDrawValues(true)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return NumberFormat.getCurrencyInstance().format(value)
                }
            }
        }

        // Create list of date labels
        val dateLabels = salesByDate.keys.sorted()

        barChart.apply {
            data = BarData(dataSet).apply {
                barWidth = 0.8f
            }
            
            // Configure X axis for dates
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelRotationAngle = -45f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        if (index >= 0 && index < dateLabels.size) {
                            try {
                                val date = dateFormat.parse(dateLabels[index])
                                return SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
                            } catch (e: Exception) {
                                return ""
                            }
                        }
                        return ""
                    }
                }
            }

            // Configure Y axis
            axisLeft.apply {
                setDrawGridLines(true)
                textColor = requireContext().getColor(R.color.emperor_text)
                textSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return NumberFormat.getCurrencyInstance().format(value)
                    }
                }
                axisMinimum = 0f
                spaceTop = 15f
            }

            axisRight.isEnabled = false

            // Enable scrolling and zooming settings
            setScaleEnabled(false) // Disable scaling
            isDoubleTapToZoomEnabled = false
            isDragEnabled = true // Enable dragging
            setVisibleXRangeMinimum(5f) // Show at least 5 bars
            setVisibleXRangeMaximum(7f) // Show at most 7 bars
            
            // Add padding for rotated labels
            setExtraBottomOffset(20f)
            setExtraTopOffset(10f)
            setViewPortOffsets(60f, 20f, 20f, 60f)

            // Ensure all bars are visible
            if (entries.size > 1) {
                xAxis.axisMinimum = -0.5f
                xAxis.axisMaximum = entries.size - 0.5f
            }

            // If we have many bars, scroll to show the most recent dates
            if (entries.size > 7) {
                moveViewToX(entries.size - 7f) // Show the most recent dates
            }

            // Animate and refresh
            animateY(1400)
            invalidate()
        }
    }
} 