package ms.mattschlenkrich.paycalculator.database.model.workorder

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "workOrderHistoryWorkPerformed",
    foreignKeys = [ForeignKey(
        entity = WorkOrderHistory::class,
        parentColumns = ["woHistoryId"],
        childColumns = ["wowpHistoryId"]
    ),
        ForeignKey(
            entity = WorkPerformed::class,
            parentColumns = ["workPerformedId"],
            childColumns = ["wowpWorkPerformedId"]
        )],
    indices = [Index(
        value = ["wowpHistoryId", "wowpWorkPerformedId"],
        unique = true
    )]
)
@Parcelize
data class WorkOrderHistoryWorkPerformed(
    @PrimaryKey
    val workOrderHistoryWorkPerformedId: Long,
    @ColumnInfo(index = true)
    val wowpHistoryId: Long,
    @ColumnInfo(index = true)
    val wowpWorkPerformedId: Long,
    val wowpSequence: Int,
    val wowpIsDeleted: Boolean,
    val wowpUpdateTime: String,
) : Parcelable
