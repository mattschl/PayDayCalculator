package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.Employers

@Parcelize
@DatabaseView(
    value = "SELECT * FROM workDates " +
            "INNER JOIN employers " +
            "ON wdEmployerId = employerId"
)
data class WorkDateAndEmployer(
    @Embedded
    var workDate: WorkDates,
    @Relation(
        entity = Employers::class,
        parentColumn = "wdEmployerId",
        entityColumn = "employerId"
    )
    var employer: Employers
) : Parcelable
