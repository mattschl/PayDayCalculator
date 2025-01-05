package ms.mattschlenkrich.paycalculator.database.model.workorder

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MaterialInSequence(
    val workOrderHistoryMaterialId: Long,
    val workOrderHistoryId: Long,
    val materialId: Long,
    val mName: String,
    val mQty: Double,
    val mSequence: Int
) : Parcelable
