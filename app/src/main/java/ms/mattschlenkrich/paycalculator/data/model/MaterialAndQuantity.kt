package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MaterialAndQuantity(
    val name: String,
    val quantity: Double
) : Parcelable