package ms.mattschlenkrich.paycalculator.database.model.workorder

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "areas",
    indices = [Index(
        value = ["areaName"],
        unique = true
    )]
)
@Parcelize
data class Areas(
    @PrimaryKey
    val areaId: Long,
    val areaName: String,
    val areaIsDeleted: Boolean,
    val areaUpdateTime: String,
) : Parcelable
