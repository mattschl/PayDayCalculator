package ms.mattschlenkrich.paycalculator.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "workOrderPictures",
    foreignKeys = [
        ForeignKey(
            entity = WorkOrder::class,
            parentColumns = ["workOrderId"],
            childColumns = ["wopWorkOrderId"],
            onDelete = ForeignKey.NO_ACTION,
        )
    ]
)
@Parcelize
data class WorkOrderPictures(
    @PrimaryKey
    val wopPictureId: Long,
    @ColumnInfo(index = true)
    val wopWorkOrderId: Long,
    val wopDriveFileId: String?,
    val wopIsDeleted: Boolean = false,
    val wopUploadTime: String? = null,
    val wopUpdateTime: String,
) : Parcelable