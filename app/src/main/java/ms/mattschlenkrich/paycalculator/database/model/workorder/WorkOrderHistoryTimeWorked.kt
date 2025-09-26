package ms.mattschlenkrich.paycalculator.database.model.workorder

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity(
    tableName = "workOrderHistoryTimeWorked",
    foreignKeys = [
        ForeignKey(
            entity = WorkOrderHistory::class,
            parentColumns = ["woHistoryId"],
            childColumns = ["wohtHistoryId"]
        )
    ],
    indices = [
        Index(value = ["wohtDateId", "wohtStartTime"], unique = true),
        Index(value = ["wohtDateId", "wohtEndTime"], unique = true)
    ]
)
@Parcelize
data class WorkOrderHistoryTimeWorked(
    @PrimaryKey
    val woHistoryTimeWorkedId: Long,
    @ColumnInfo(index = true)
    val wohtHistoryId: Long,
    @ColumnInfo(index = true)
    var wohtDateId: Long,
    @ColumnInfo(index = true)
    var wohtStartTime: String,
    @ColumnInfo(index = true)
    var wohtEndTime: String,
    var wohtTimeType: Int,
    val wohtIsDeleted: Boolean,
    val wohtUpdateTime: String,
) : Parcelable
