package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExpenseSummary(
    val supplier: String,
    val type: String,
    val totalAmount: Double
) : Parcelable