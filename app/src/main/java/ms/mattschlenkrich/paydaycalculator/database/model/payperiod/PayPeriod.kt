package ms.mattschlenkrich.paydaycalculator.database.model.payperiod

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.TABLE_PAY_PERIODS
import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers

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


