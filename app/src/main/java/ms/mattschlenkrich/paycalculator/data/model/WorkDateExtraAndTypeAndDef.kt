package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtrasDefinitions

@Parcelize
data class WorkDateExtraAndTypeAndDef(
    @Embedded
    val extra: WorkDateExtras,
    @Relation(
        entity = WorkExtraTypes::class,
        parentColumn = "wdeExtraTypeId",
        entityColumn = "workExtraTypeId"
    )
    var type: WorkExtraTypes?,
    @Relation(
        entity = WorkExtrasDefinitions::class,
        parentColumn = "wdeExtraTypeId",
        entityColumn = "weExtraTypeId"
    )
    var def: WorkExtrasDefinitions?
) : Parcelable