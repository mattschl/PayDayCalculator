package ms.mattschlenkrich.paydaycalculator.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_CUTOFF_DATE
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_TAX_CUTOFF_DATE
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_TAX_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_TAX_TYPE
import ms.mattschlenkrich.paydaycalculator.common.TABLE_PAY_PERIODS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATES_EXTRAS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_PAY_PERIOD_EXTRAS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_PAY_PERIOD_TAX
import ms.mattschlenkrich.paydaycalculator.common.WORK_DATES_CUTOFF_DATE
import ms.mattschlenkrich.paydaycalculator.common.WORK_DATES_DATE
import ms.mattschlenkrich.paydaycalculator.common.WORK_DATES_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.WORK_DATES_EXTRAS_DATE
import ms.mattschlenkrich.paydaycalculator.common.WORK_DATES_EXTRAS_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.WORK_DATES_EXTRA_ID
import ms.mattschlenkrich.paydaycalculator.common.WORK_EXTRA_DEFINITIONS_ID
import ms.mattschlenkrich.paydaycalculator.common.WORK_PAY_PERIOD_EXTRA_CUTOFF_DATE
import ms.mattschlenkrich.paydaycalculator.common.WORK_PAY_PERIOD_EXTRA_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.WORK_PAY_PERIOD_EXTRA_ID
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_TYPE


@Entity(
    tableName = TABLE_PAY_PERIODS,
    primaryKeys = [PAY_PERIOD_CUTOFF_DATE, PAY_PERIOD_EMPLOYER_ID],
    foreignKeys = [ForeignKey(
        entity = Employers::class,
        parentColumns = [EMPLOYER_ID],
        childColumns = [PAY_PERIOD_EMPLOYER_ID]
    )]
)
@Parcelize
data class PayPeriods(
    val ppCutoffDate: String,
    @ColumnInfo(index = true)
    val ppEmployerId: Long,
    val ppIsDeleted: Boolean,
    val ppUpdateTime: String,
) : Parcelable

@Entity(
    tableName = TABLE_WORK_DATES,
    primaryKeys = [WORK_DATES_EMPLOYER_ID,
        WORK_DATES_CUTOFF_DATE,
        WORK_DATES_DATE],
    foreignKeys = [
        ForeignKey(
            entity = PayPeriods::class,
            parentColumns = [PAY_PERIOD_EMPLOYER_ID, PAY_PERIOD_CUTOFF_DATE],
            childColumns = [WORK_DATES_EMPLOYER_ID, WORK_DATES_CUTOFF_DATE]
        )
    ], indices = [Index(value = [WORK_DATES_EMPLOYER_ID, WORK_DATES_DATE], unique = true)]
)
@Parcelize
data class WorkDates(
    val wdEmployerId: Long,
    val wdCutoffDate: String,
    val wdDate: String,
    val wdRegHours: Double,
    val wdOtHours: Double,
    val wdDblOtHours: Double,
    val wdStatHours: Double,
    val wdIsDeleted: Boolean,
    val wdUpdateTime: String,
) : Parcelable

@Entity(
    tableName = TABLE_WORK_DATES_EXTRAS,
    primaryKeys = [WORK_DATES_EXTRAS_EMPLOYER_ID,
        WORK_DATES_EXTRAS_DATE,
        WORK_DATES_EXTRA_ID],
    foreignKeys = [ForeignKey(
        entity = WorkDates::class,
        parentColumns = [WORK_DATES_EMPLOYER_ID, WORK_DATES_DATE],
        childColumns = [WORK_DATES_EXTRAS_EMPLOYER_ID, WORK_DATES_EXTRAS_DATE]
    ), ForeignKey(
        entity = WorkExtrasDefinitions::class,
        parentColumns = [WORK_EXTRA_DEFINITIONS_ID],
        childColumns = [WORK_DATES_EXTRA_ID]
    )
    ]
)
@Parcelize
data class WorkDatesExtras(
    val wdeEmployerId: Long,
    @ColumnInfo(index = true)
    val wdeDate: String,
    @ColumnInfo(index = true)
    val wdeId: Long,
    val wdeName: String,
    val wdeValue: Double,
    val wdeIsDeleted: Boolean,
    val wdeUpdateTime: String,
) : Parcelable

@Entity(
    tableName = TABLE_WORK_PAY_PERIOD_EXTRAS,
    primaryKeys = [
        WORK_PAY_PERIOD_EXTRA_EMPLOYER_ID,
        WORK_PAY_PERIOD_EXTRA_CUTOFF_DATE,
        WORK_PAY_PERIOD_EXTRA_ID],
    foreignKeys = [ForeignKey(
        entity = PayPeriods::class,
        parentColumns = [PAY_PERIOD_EMPLOYER_ID, PAY_PERIOD_CUTOFF_DATE],
        childColumns = [WORK_PAY_PERIOD_EXTRA_EMPLOYER_ID,
            WORK_PAY_PERIOD_EXTRA_CUTOFF_DATE]
    )]
)
@Parcelize
data class WorkPayPeriodExtras(
    val ppeEmployerId: Long,
    val ppeCutoffDate: String,
    val ppeExtraId: Long,
    val ppeName: String,
    val ppeValue: Double,
    val ppeIsDeleted: Boolean,
    val ppeUpdateTime: String,
) : Parcelable

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
    val wppCutoffDate: String,
    val wppEmployerId: Long,
    @ColumnInfo(index = true)
    val wppTaxType: String,
    val wppIsDeleted: Boolean,
    val wppUpdateTime: String,
) : Parcelable