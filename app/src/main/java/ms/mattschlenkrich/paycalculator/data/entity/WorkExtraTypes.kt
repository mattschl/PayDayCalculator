package ms.mattschlenkrich.paycalculator.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_EXTRA_TYPES


@Entity(
    tableName = TABLE_WORK_EXTRA_TYPES,
    foreignKeys = [ForeignKey(
        entity = Employers::class,
        parentColumns = ["employerId"],
        childColumns = ["wetEmployerId"]
    )]
)
@Parcelize
data class WorkExtraTypes(
    @PrimaryKey
    val workExtraTypeId: Long,
    val wetName: String,
    @ColumnInfo(index = true)
    val wetEmployerId: Long,
    val wetAppliesTo: Int,
    val wetAttachTo: Int,
    val wetIsCredit: Boolean,
    val wetIsDefault: Boolean,
    val wetIsDeleted: Boolean,
    val wetUpdateTime: String,
) : Parcelable