package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MaterialAndQuantity(
    val name: String,
    var quantity: Double
) : Parcelable