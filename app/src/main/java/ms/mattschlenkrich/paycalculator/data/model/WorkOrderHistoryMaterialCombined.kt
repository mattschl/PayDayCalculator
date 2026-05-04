package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryMaterial


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