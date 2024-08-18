package ms.mattschlenkrich.paydaycalculator.model.payperiod

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers

@Parcelize
data class WorkDateFull(
    @Embedded
    val date: WorkDates,
    @Relation(
        entity = Employers::class,
        parentColumn = "employerId",
        entityColumn = "wdEmployerId"
    )
    val employer: Employers
) : Parcelable
