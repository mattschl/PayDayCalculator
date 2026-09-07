package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class JobSpecAndQuantity(
    val jobSpecId: Long,
    val name: String,
    val quantity: Int
) : Parcelable