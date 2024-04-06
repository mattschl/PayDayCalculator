package ms.mattschlenkrich.paydaycalculator.model.extras

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates

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