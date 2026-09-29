package ms.mattschlenkrich.paycalculator.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "workOrderHistoryPictures",
    foreignKeys = [
        ForeignKey(
            entity = WorkOrderHistory::class,
            parentColumns = ["woHistoryId"],
            childColumns = ["wohpHistoryId"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
@Parcelize
data class WorkOrderHistoryPictures(
    @PrimaryKey
    val pictureId: Long,
    @ColumnInfo(index = true)
    val wohpHistoryId: Long,
    val driveFileId: String?,
    val wohpIsDeleted: Boolean = false,
    val wohpUploadTime: String? = null,
    val wohpUpdateTime: String,
) : Parcelable