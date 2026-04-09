package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.WorkExtraTypes

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