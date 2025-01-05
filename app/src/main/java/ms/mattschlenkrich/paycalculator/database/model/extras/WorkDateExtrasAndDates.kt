package ms.mattschlenkrich.paycalculator.database.model.extras

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates

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