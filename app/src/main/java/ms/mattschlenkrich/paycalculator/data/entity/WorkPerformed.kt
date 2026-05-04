package ms.mattschlenkrich.paycalculator.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "workPerformed",
    indices = [Index(
        value = ["wpDescription"], unique = true
    )]
)
@Parcelize
data class WorkPerformed(
    @PrimaryKey
    val workPerformedId: Long,
    val wpDescription: String,
    val wpIsDeleted: Boolean,
    val wpUpdateTime: String,
) : Parcelable