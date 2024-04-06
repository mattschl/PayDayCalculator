package ms.mattschlenkrich.paydaycalculator.model.extras

import android.os.Parcelable
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRA_TYPES

@DatabaseView(
    "SELECT extraDef.*, " +
            "extraType.* " +
            "FROM $TABLE_WORK_EXTRAS_DEFINITIONS as extraDef " +
            "LEFT JOIN $TABLE_WORK_EXTRA_TYPES as extraType ON " +
            "extraDef.weExtraTypeId = extraType.workExtraTypeId"
)
@Parcelize
data class ExtraDefinitionAndType(
    @Embedded
    val definition: WorkExtrasDefinitions,
    @Relation(
        entity = WorkExtraTypes::class,
        parentColumn = "weExtraTypeId",
        entityColumn = "workExtraTypeId"
    )
    val extraType: WorkExtraTypes
) : Parcelable
