package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.WorkExtrasDefinitions

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