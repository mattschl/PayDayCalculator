package ms.mattschlenkrich.paydaycalculator.database.model.workOrder

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "workSpecs"
)
@Parcelize
data class WorkSpec(
    @PrimaryKey
    val workSpecId: Long,
    val wsName: String,
    val wsIsDeleted: Boolean,
    val wsUpdateTime: String,
) : Parcelable
