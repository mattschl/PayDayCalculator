package ms.mattschlenkrich.paydaycalculator.database.model.payperiod

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_CUTOFF_DATE
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_TAX_CUTOFF_DATE
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_TAX_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_TAX_TYPE
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_PAY_PERIOD_TAX
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_TYPE
import ms.mattschlenkrich.paydaycalculator.database.model.tax.TaxTypes

@Entity(
    tableName = TABLE_WORK_PAY_PERIOD_TAX,
    primaryKeys = [PAY_PERIOD_TAX_CUTOFF_DATE,
        PAY_PERIOD_TAX_EMPLOYER_ID,
        PAY_PERIOD_TAX_TYPE],
    foreignKeys = [ForeignKey(
        entity = PayPeriods::class,
        parentColumns = [PAY_PERIOD_CUTOFF_DATE, PAY_PERIOD_EMPLOYER_ID],
        childColumns = [PAY_PERIOD_TAX_CUTOFF_DATE, PAY_PERIOD_TAX_EMPLOYER_ID]
    ), ForeignKey(
        entity = TaxTypes::class,
        parentColumns = [WORK_TAX_TYPE],
        childColumns = [PAY_PERIOD_TAX_TYPE]
    )]
)
@Parcelize
data class WorkPayPeriodTax(
    val workPayPeriodTaxId: Long,
    val wppCutoffDate: String,
    val wppEmployerId: Long,
    @ColumnInfo(index = true)
    val wppTaxType: String,
    val wppIsDeleted: Boolean,
    val wppUpdateTime: String,
) : Parcelable