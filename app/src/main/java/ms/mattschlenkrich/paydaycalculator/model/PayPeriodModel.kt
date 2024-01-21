package ms.mattschlenkrich.paydaycalculator.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
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
    @PrimaryKey
    val workDateId: Long,
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

@Parcelize
data class WorkDateAndExtras(
    @Embedded
    val workDate: WorkDates,
    @Relation(
        entity = WorkDatesExtras::class,
        parentColumn = "workDateId",
        entityColumn = "wdId"
    )
    var extras: WorkDatesExtras?
) : Parcelable

@Entity(
    tableName = TABLE_WORK_DATES_EXTRAS,
    foreignKeys = [ForeignKey(
        entity = WorkDates::class,
        parentColumns = ["workDateId"],
        childColumns = ["wdId"]
    )]
)
@Parcelize
data class WorkDatesExtras(
    @PrimaryKey
    val workDateExtraId: Long,
    @ColumnInfo(index = true)
    val wdId: Long,
    @ColumnInfo(index = true)
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