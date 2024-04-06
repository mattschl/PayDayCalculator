package ms.mattschlenkrich.paydaycalculator.model.payperiod

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_PAY_PERIOD_EXTRAS
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtraTypes


@Entity(
    tableName = TABLE_WORK_PAY_PERIOD_EXTRAS,
    foreignKeys = [
        ForeignKey(
            entity = WorkExtraTypes::class,
            parentColumns = ["workExtraTypeId"],
            childColumns = ["ppeExtraTypeId"]
        ),
        ForeignKey(
            entity = PayPeriods::class,
            parentColumns = ["payPeriodId"],
            childColumns = ["ppePayPeriodId"]
        )],
    indices = [
        Index(
            value =
            ["ppePayPeriodId", "ppeName"],
            unique = true
        )]
)
@Parcelize
data class WorkPayPeriodExtras(
    @PrimaryKey
    val workPayPeriodExtraId: Long,
    @ColumnInfo(index = true)
    val ppePayPeriodId: Long,
    @ColumnInfo(index = true)
    val ppeExtraTypeId: Long?,
    val ppeName: String,
    val ppeAppliesTo: Int,
    val ppeAttachTo: Int,
    val ppeValue: Double,
    val ppeIsFixed: Boolean,
    val ppeIsCredit: Boolean,
    val ppeIsDeleted: Boolean,
    val ppeUpdateTime: String,
) : Parcelable
