package ms.mattschlenkrich.paycalculator.database.model.workorder.merged

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformed

@Entity(
    tableName = "workPerformedMerged",
    foreignKeys = [
        ForeignKey(
            entity = WorkPerformed::class,
            parentColumns = ["workPerformedId"],
            childColumns = ["wpmMasterId"]
        ),
        ForeignKey(
            entity = WorkPerformed::class,
            parentColumns = ["workPerformedId"],
            childColumns = ["wpmChildId"]
        )
    ]
)
@Parcelize
data class WorkPerformedMerged(
    @PrimaryKey
    val workPerformedMergeId: Long,
    @ColumnInfo(index = true)
    val wpmMasterId: Long,
    @ColumnInfo(index = true)
    val wpmChildId: Long,
    val wpmIsDeleted: Boolean,
    val wpmUpdateTime: String,
) : Parcelable