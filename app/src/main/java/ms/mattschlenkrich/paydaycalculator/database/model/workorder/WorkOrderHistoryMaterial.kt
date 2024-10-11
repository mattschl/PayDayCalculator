package ms.mattschlenkrich.paydaycalculator.database.model.workorder

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "workOrderHistoryMaterials",
    foreignKeys = [ForeignKey(
        entity = WorkOrderHistory::class,
        parentColumns = ["woHistoryId"],
        childColumns = ["wohmHistoryId"]
    ),
        ForeignKey(
            entity = Material::class,
            parentColumns = ["materialId"],
            childColumns = ["wohmMaterialId"]
        )],
    indices = [Index(
        value = ["wohmHistoryId", "wohmMaterialId"],
        unique = true
    )]
)
@Parcelize
data class WorkOrderHistoryMaterial(
    @PrimaryKey
    val workOrderHistoryMaterialId: Long,
    @ColumnInfo(index = true)
    val wohmHistoryId: Long,
    @ColumnInfo(index = true)
    val wohmMaterialId: Long,
    val wohmQuantity: Double,
    val wohmSequence: Int,
    val wohmIsDeleted: Boolean,
    val wohmUpdateTime: String
) : Parcelable
