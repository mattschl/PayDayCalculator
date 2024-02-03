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
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_CUTOFF_DATE
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_TAX_CUTOFF_DATE
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_TAX_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_TAX_TYPE
import ms.mattschlenkrich.paydaycalculator.common.TABLE_PAY_PERIODS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATE_EXTRAS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_PAY_PERIOD_EXTRAS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_PAY_PERIOD_TAX
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_TYPE

@Entity(
    tableName = TABLE_PAY_PERIODS,
    primaryKeys = ["ppCutoffDate", "ppEmployerId"],
    foreignKeys = [ForeignKey(
        entity = Employers::class,
        parentColumns = ["employerId"],
        childColumns = ["ppEmployerId"]
    )]
)
@Parcelize
data class PayPeriods(
    val payPeriodId: Long,
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
            parentColumns = ["ppEmployerId", "ppCutoffDate"],
            childColumns = ["wdEmployerId", "wdCutoffDate"]
        )
    ], indices = [Index(
        value =
        ["wdEmployerId", "wdDate"], unique = true
    )
    ]
)
@Parcelize
data class WorkDates(
    @PrimaryKey
    val workDateId: Long,
    val wdPayPeriodId: Long,
    val wdEmployerId: Long,
    @ColumnInfo(index = true)
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
        entity = WorkDateExtras::class,
        parentColumn = "workDateId",
        entityColumn = "wdeWorkDateId"
    )
    var extras: WorkDateExtras?
) : Parcelable


@Entity(
    tableName = TABLE_WORK_DATE_EXTRAS,
    foreignKeys = [ForeignKey(
        entity = WorkDates::class,
        parentColumns = ["workDateId"],
        childColumns = ["wdeWorkDateId"]
    )],
    indices = [Index(
        value =
        ["wdeWorkDateId", "wdeExtraTypeId"], unique = true
    )]
)
@Parcelize
data class WorkDateExtras(
    @PrimaryKey
    val workDateExtraId: Long,
    @ColumnInfo(index = true)
    val wdeWorkDateId: Long,
    @ColumnInfo(index = true)
    val wdeExtraTypeId: Long?,
    @ColumnInfo(index = true)
    val wdeName: String,
    val wdeAppliesTo: Int,
    val wdeAttachTo: Int,
    val wdeValue: Double,
    val wdeIsFixed: Boolean,
    val wdeIsCredit: Boolean,
    val wdeIsDeleted: Boolean,
    val wdeUpdateTime: String,
) : Parcelable

@Entity(
    tableName = TABLE_WORK_PAY_PERIOD_EXTRAS,
    primaryKeys = [
        "ppeEmployerId",
        "ppeCutoffDate",
        "ppeExtraId"],
    foreignKeys = [ForeignKey(
        entity = PayPeriods::class,
        parentColumns = [PAY_PERIOD_EMPLOYER_ID, PAY_PERIOD_CUTOFF_DATE],
        childColumns = ["ppeEmployerId",
            "ppeCutoffDate"]
    )]
)
@Parcelize
data class WorkPayPeriodExtras(
    val workPayPeriodExtraId: Long,
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
    val workPayPeriodTaxId: Long,
    val wppCutoffDate: String,
    val wppEmployerId: Long,
    @ColumnInfo(index = true)
    val wppTaxType: String,
    val wppIsDeleted: Boolean,
    val wppUpdateTime: String,
) : Parcelable

@Parcelize
data class WorkDateAndExtrasFull(
    @Embedded
    val workDates: WorkDates,
    @Relation(
        entity = ExtraDefinitionAndType::class,
        parentColumn = "wdEmployerId",
        entityColumn = "weEmployerId"
    )
    val extraDef: ExtraDefinitionAndType,
) : Parcelable