package ms.mattschlenkrich.paydaycalculator.database.model.workorder

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MaterialAndQuantity(
    val name: String,
    var quantity: Double
) : Parcelable