package ms.mattschlenkrich.paydaycalculator.database.model.workOrder

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "jobSpecs"
)
@Parcelize
data class JobSpec(
    @PrimaryKey
    val jobSpecId: Long,
    val jsName: String,
    val jsIsDeleted: Boolean,
    val jsUpdateTime: String,
) : Parcelable
