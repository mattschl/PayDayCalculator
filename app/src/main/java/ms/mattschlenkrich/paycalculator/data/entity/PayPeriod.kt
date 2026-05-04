package ms.mattschlenkrich.paycalculator.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.common.TABLE_PAY_PERIODS

@Entity(
    tableName = TABLE_PAY_PERIODS,
    primaryKeys = ["ppCutoffDate", "ppEmployerId"],
    foreignKeys = [ForeignKey(
        entity = Employers::class,
        parentColumns = ["employerId"],
        childColumns = ["ppEmployerId"]
    )],
    indices = [Index(
        value = ["ppEmployerId", "ppCutoffDate"], unique = true
    ),
        Index(
            value = ["payPeriodId"], unique = true
        )]
)
@Parcelize
data class PayPeriods(
    val payPeriodId: Long,
    @ColumnInfo(index = true)
    val ppCutoffDate: String,
    @ColumnInfo(index = true)
    val ppEmployerId: Long,
    val ppIsDeleted: Boolean,
    val ppUpdateTime: String,
) : Parcelable