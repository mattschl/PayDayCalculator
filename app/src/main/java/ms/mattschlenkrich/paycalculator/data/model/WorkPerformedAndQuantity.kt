package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorkPerformedAndQuantity(
    val description: String,
    val area: String?,
    val quantity: Int
) : Parcelable