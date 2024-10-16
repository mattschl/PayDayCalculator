package ms.mattschlenkrich.paydaycalculator.database.model.workorder

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorkPerformedInSequence(
    val workPerformedHistoryId: Long,
    val wpWorkPerformedId: Long,
    val wpDescription: String,
    val wpSequence: Int,
) : Parcelable