package ms.mattschlenkrich.paydaycalculator.database.model.payperiod

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.RoomWarnings
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.database.model.extras.WorkExtraTypes

@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
@Parcelize
data class WorkDateExtraAndType(
    @Embedded
    val extra: WorkDateExtras,
    @Relation(
        entity = WorkExtraTypes::class,
        parentColumn = "workExtraTypeId",
        entityColumn = "wdeExtraTypeId"
    )
    var type: WorkExtraTypes?
) : Parcelable