package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MaterialAndQuantity(
    val materialId: Long,
    val name: String,
    var quantity: Double,
    val totalAmount: Double = 0.0,
    val cost: Double = 0.0,
    val price: Double = 0.0
) : Parcelable