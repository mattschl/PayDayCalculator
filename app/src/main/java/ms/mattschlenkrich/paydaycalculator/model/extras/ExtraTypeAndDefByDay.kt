package ms.mattschlenkrich.paydaycalculator.model.extras

import android.os.Parcelable
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRA_TYPES


@DatabaseView(
    "SELECT extraType.*, extraDef.* " +
            "FROM $TABLE_WORK_EXTRA_TYPES as extraType " +
            "LEFT JOIN $TABLE_WORK_EXTRAS_DEFINITIONS as extraDef ON " +
            "(workExtraTypeId = weExtraTypeId) " +
            "WHERE wetAttachTo = 1 " +
            "AND wetIsDeleted = 0 " +
            "ORDER BY wetName COLLATE NOCASE"
)
@Parcelize
data class ExtraTypeAndDefByDay(
    @Embedded
    val extraType: WorkExtraTypes,
    @Relation(
        entity = WorkExtrasDefinitions::class,
        parentColumn = "workExtraTypeId",
        entityColumn = "weExtraTypeId"
    )
    val extraDef: WorkExtrasDefinitions
) : Parcelable
