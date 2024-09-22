package ms.mattschlenkrich.paydaycalculator.database.model.workOrder

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "workOderHistoryWorkSpec",
    foreignKeys = [ForeignKey(
        entity = WorkOrderHistory::class,
        parentColumns = ["woHistoryId"],
        childColumns = ["wohwsHistoryId"]
    ),
        ForeignKey(
            entity = WorkSpec::class,
            parentColumns = ["workSpecId"],
            childColumns = ["wohwsWorkSpecId"]
        )],
    indices = [Index(
        value = ["wohwsHistoryId", "wohwsWorkSpecId"],
        unique = true
    )]
)
@Parcelize
data class WorkOrderHistoryWorkSpec(
    @PrimaryKey
    val workOrderHistoryWorkSpecId: Long,
    @ColumnInfo(index = true)
    val wohwsHistoryId: Long,
    @ColumnInfo(index = true)
    val wohwsWorkSpecId: Long,
    val wohwsIsDeleted: Boolean,
    val wohwsUpdateTime: String,
) : Parcelable
