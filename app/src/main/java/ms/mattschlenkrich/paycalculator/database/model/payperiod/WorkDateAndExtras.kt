package ms.mattschlenkrich.paycalculator.database.model.payperiod

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

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