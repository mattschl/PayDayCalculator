package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize


@Parcelize
data class WorkOrderHistoryMaterialCombined(
    @Embedded
    val workOrderHistoryMaterial: WorkOrderHistoryMaterial,
    @Relation(
        entity = Material::class,
        parentColumn = "wohmMaterialId",
        entityColumn = "materialId"
    )
    val material: Material
) : Parcelable
