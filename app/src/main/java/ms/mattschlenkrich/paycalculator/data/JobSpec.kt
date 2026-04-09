package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "jobSpecs",
    indices = [Index(
        value = ["jsName"], unique = true
    )]
)
@Parcelize
data class JobSpec(
    @PrimaryKey
    val jobSpecId: Long,
    val jsName: String,
    val jsIsDeleted: Boolean,
    val jsUpdateTime: String,
) : Parcelable