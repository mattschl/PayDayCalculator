package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates

@Parcelize
data class WorkDateAndExtras(
    @Embedded
    val workDate: WorkDates,
    @Relation(
        entity = WorkDateExtras::class,
        parentColumn = "workDateId",
        entityColumn = "wdeWorkDateId"
    )
    var extras: WorkDateExtras?
) : Parcelable