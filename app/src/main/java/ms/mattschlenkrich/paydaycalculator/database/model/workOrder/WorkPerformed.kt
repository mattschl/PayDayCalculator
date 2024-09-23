package ms.mattschlenkrich.paydaycalculator.database.model.workOrder

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "workPerformed"
)
@Parcelize
data class WorkPerformed(
    @PrimaryKey
    val workPerformedId: Long,
    val wpDescription: String,
    val wpIsDeleted: Boolean,
    val wpUpdateTime: String,
) : Parcelable
