package ms.mattschlenkrich.paydaycalculator.model.payperiod

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATES


@Entity(
    tableName = TABLE_WORK_DATES,
    foreignKeys = [
        ForeignKey(
            entity = PayPeriods::class,
            parentColumns = ["ppEmployerId", "ppCutoffDate"],
            childColumns = ["wdEmployerId", "wdCutoffDate"]
        )
    ], indices = [Index(
        value =
        ["wdEmployerId", "wdDate", "wdCutoffDate"], unique = true
    ), Index(
        value = ["wdEmployerId", "wdCutoffDate"]
    )
    ]
)
@Parcelize
data class WorkDates(
    @PrimaryKey
    val workDateId: Long,
    @ColumnInfo(index = true)
    val wdPayPeriodId: Long,
    @ColumnInfo(index = true)
    val wdEmployerId: Long,
    @ColumnInfo(index = true)
    val wdCutoffDate: String,
    @ColumnInfo(index = true)
    val wdDate: String,
    val wdRegHours: Double,
    val wdOtHours: Double,
    val wdDblOtHours: Double,
    val wdStatHours: Double,
    val wdIsDeleted: Boolean,
    val wdUpdateTime: String,
) : Parcelable
