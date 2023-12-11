package ms.mattschlenkrich.paydaycalculator.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_CUTOFF_DATE
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYERS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_PAY_PERIODS


@Entity(
    tableName = TABLE_EMPLOYERS
)
@Parcelize
data class Employers(
    @PrimaryKey
    val employerId: Long,
    val employerName: String,
    val payFrequency: String,
    val startDate: String,
    val dayOfWeek: String,
    val cutoffDaysBefore: Int,
    val midMonthlyDate: Int,
    val mainMonthlyDay: Int,
    val employerIsDeleted: Boolean,
    val employerUpdateTime: String,
) : Parcelable

@Entity(
    tableName = TABLE_WORK_PAY_PERIODS,
    primaryKeys = [PAY_PERIOD_CUTOFF_DATE, PAY_PERIOD_EMPLOYER_ID],
    foreignKeys = [ForeignKey(
        entity = Employers::class,
        parentColumns = [EMPLOYER_ID],
        childColumns = [PAY_PERIOD_EMPLOYER_ID]
    )]
)
@Parcelize
data class WorkPayPeriods(
    val ppCutoffDate: String,
    val ppEmployerId: Long,
    @ColumnInfo(defaultValue = "0")
    val ppIsDeleted: Boolean,
    val ppUpdateTime: String,
) : Parcelable

//@Entity(
//    tableName = TABLE_WORK_DATES,
//    primaryKeys = [WORK_DATES_EMPLOYER_ID,
//        WORK_DATES_CUTOFF_DATE,
//        WORK_DATES_DATE],
//    foreignKeys = [
//        ForeignKey(
//            entity = WorkPayPeriods::class,
//            parentColumns = [PAY_PERIOD_EMPLOYER_ID,PAY_PERIOD_CUTOFF_DATE],
//            childColumns = [WORK_DATES_EMPLOYER_ID,WORK_DATES_CUTOFF_DATE]
//        )
//    ]
//)
//@Parcelize
//data class WorkDates(
//    val wdEmployerId: Long,
//    val wdCutoffDate: String,
//    val wdDate: String,
//    val wdRegHours: Double,
//    val wdOtHours: Double,
//    val wdDblOtHours: Double,
//    val wdStatHours: Double,
//    @ColumnInfo(defaultValue = "0")
//    val wdIsDeleted: Boolean,
//    val wdUpdateTime: String,
//) : Parcelable
//
//@Entity(
//    tableName = TABLE_WORK_DATES_EXTRAS,
//    primaryKeys = [WORK_DATES_EXTRAS_EMPLOYER_ID,
//        WORK_DATES_EXTRAS_DATE,
//        WORK_DATES_EXTRA_ID],
//    foreignKeys = [ ForeignKey(
//            entity = WorkDates::class,
//            parentColumns = [WORK_DATES_EMPLOYER_ID,WORK_DATES_DATE],
//            childColumns = [WORK_DATES_EXTRAS_EMPLOYER_ID,WORK_DATES_EXTRAS_DATE]
//        ), ForeignKey(
//            entity = WorkExtrasDefinitions::class,
//            parentColumns = [WORK_EXTRA_ID],
//            childColumns = [WORK_DATES_EXTRA_ID]
//        )
//    ]
//)
//@Parcelize
//data class WorkDatesExtras(
//    val wdeEmployerId: Long,
//    val wdeDate: String,
//    val wdeId: Long,
//    val wdeName: String,
//    val wdeValue: Double,
//    @ColumnInfo(defaultValue = "0")
//    val wdeIsDeleted: Boolean,
//    val wdeUpdateTime: String,
//) : Parcelable
//
//@Entity(
//    tableName = TABLE_WORK_PAY_PERIOD_EXTRAS,
//    primaryKeys = [
//        WORK_PAY_PERIOD_EXTRA_EMPLOYER_ID,
//        WORK_PAY_PERIOD_EXTRA_CUTOFF_DATE,
//        WORK_PAY_PERIOD_EXTRA_ID],
//    foreignKeys = [ForeignKey(
//        entity = WorkPayPeriods::class,
//        parentColumns = [PAY_PERIOD_EMPLOYER_ID, PAY_PERIOD_CUTOFF_DATE],
//        childColumns = [WORK_PAY_PERIOD_EXTRA_EMPLOYER_ID,
//            WORK_PAY_PERIOD_EXTRA_CUTOFF_DATE]
//    )]
//)
//@Parcelize
//data class WorkPayPeriodExtras(
//    val ppeEmployerId: Long,
//    val ppeCutoffDate: String,
//    val ppeExtraId: Long,
//    val ppeName: String,
//    val ppeValue: Double,
//    val ppeIsDeleted: Boolean,
//    val ppeUpdateTime: String,
//) : Parcelable
//
//@Entity(
//    tableName = TABLE_WORK_PAY_PERIOD_TAX,
//    primaryKeys = [PAY_PERIOD_TAX_CUTOFF_DATE,
//                  PAY_PERIOD_TAX_EMPLOYER_ID,
//                  PAY_PERIOD_TAX_TYPE],
//    foreignKeys = [ForeignKey(
//        entity = WorkPayPeriods::class,
//        parentColumns = [PAY_PERIOD_CUTOFF_DATE, PAY_PERIOD_EMPLOYER_ID],
//        childColumns = [PAY_PERIOD_TAX_CUTOFF_DATE, PAY_PERIOD_TAX_EMPLOYER_ID]
//    ), ForeignKey(
//        entity = WorkTaxTypes::class,
//        parentColumns = [WORK_TAX_TYPE],
//        childColumns = [PAY_PERIOD_TAX_TYPE]
//    )]
//)
//@Parcelize
//data class WorkPayPeriodTax(
//    val wppCutoffDate: String,
//    val wppEmployerId: Long,
//    val wppTaxType: String,
//    val wppIsDeleted: Boolean,
//    val wppUpdateTime: String,
//) : Parcelable