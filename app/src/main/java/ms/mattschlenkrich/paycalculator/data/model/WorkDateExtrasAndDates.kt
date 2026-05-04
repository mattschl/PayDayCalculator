package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates

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