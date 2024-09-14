package ms.mattschlenkrich.paydaycalculator.database.model.payperiod

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.RoomWarnings
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.database.model.extras.WorkExtrasDefinitions

@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
@Parcelize
data class WorkDateExtraAndTypeFull(
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