package ms.mattschlenkrich.paycalculator.database.model.workorder.merged

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.database.model.workorder.JobSpec

@Entity(
    tableName = "jobSpecMerged",
    foreignKeys = [
        ForeignKey(
            entity = JobSpec::class,
            parentColumns = ["jobSpecId"],
            childColumns = ["jsmMasterId"],
        ),
        ForeignKey(
            entity = JobSpec::class,
            parentColumns = ["jobSpecId"],
            childColumns = ["jsmChildId"],
        )
    ]
)
@Parcelize
data class JobSpecMerged(
    @PrimaryKey
    val jobSpecMergedId: Long,
    @ColumnInfo(index = true)
    val jsmMasterId: Long,
    @ColumnInfo(index = true)
    val jsmChildId: Long,
    val jsmIsDeleted: Boolean,
    val jsmUpdateTime: String,
) : Parcelable