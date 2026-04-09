package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.WorkDates

@Parcelize
data class WorkDateExtrasAndDates(
    @Embedded
    var workDateExtra: WorkDateExtras,
    @Relation(
        entity = WorkDates::class,
        parentColumn = "wdeWorkDateId",
        entityColumn = "workDateId"
    )
    var workDate: WorkDates
) : Parcelable