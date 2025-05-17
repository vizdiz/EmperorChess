import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.emperorchess.R
import com.example.emperorchess.models.CustomerReport
import java.text.NumberFormat

class CustomerReportAdapter(
    private val reports: List<CustomerReport>
) : RecyclerView.Adapter<CustomerReportAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
        val tvTotalPurchases: TextView = view.findViewById(R.id.tvTotalPurchases)
        val tvLastPurchase: TextView = view.findViewById(R.id.tvLastPurchase)
        val tvNumberOfPurchases: TextView = view.findViewById(R.id.tvNumberOfPurchases)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reports[position]
        val numberFormat = NumberFormat.getCurrencyInstance()

        holder.apply {
            tvCustomerName.text = report.customerName
            tvTotalPurchases.text = "Total: ${numberFormat.format(report.totalPurchases)}"
            tvLastPurchase.text = "Last Purchase: ${
                if (report.lastPurchaseDate.isNotEmpty()) report.lastPurchaseDate 
                else "No purchases"
            }"
            tvNumberOfPurchases.text = "Purchases: ${report.numberOfPurchases}"
        }
    }

    override fun getItemCount() = reports.size
} 