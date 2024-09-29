package ms.mattschlenkrich.paydaycalculator.database.model.workOrder

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorkPerformedInSequence(
    @PrimaryKey
    val workPerformedHistoryId: Long,
    val wpWorkPerformedId: Long,
    val wpDescription: String,
    val wpSequence: Int,
) : Parcelable