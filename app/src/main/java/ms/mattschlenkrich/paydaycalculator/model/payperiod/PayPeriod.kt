package ms.mattschlenkrich.paydaycalculator.model.payperiod

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Relation
import androidx.room.RoomWarnings
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_CUTOFF_DATE
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_TAX_CUTOFF_DATE
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_TAX_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.PAY_PERIOD_TAX_TYPE
import ms.mattschlenkrich.paydaycalculator.common.TABLE_PAY_PERIODS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_PAY_PERIOD_TAX
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_TYPE
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtrasDefinitions
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxTypes

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

@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
@Parcelize
data class WorkDateExtraAndType(
    @Embedded
    val extra: WorkDateExtras,
    @Relation(
        entity = WorkExtraTypes::class,
        parentColumn = "workExtraTypeId",
        entityColumn = "wdeExtraTypeId"
    )
    var type: WorkExtraTypes?
) : Parcelable

@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
@Parcelize
data class WorkDateExtraAndTypeFull(
    @Embedded
    val extra: WorkDateExtras,
    @Relation(
        entity = WorkExtraTypes::class,
        parentColumn = "wdeExtraTypeId",
        entityColumn = "workExtraTypeId"
    )
    var type: WorkExtraTypes?,
    @Relation(
        entity = WorkExtrasDefinitions::class,
        parentColumn = "wdeExtraTypeId",
        entityColumn = "weExtraTypeId"
    )
    var def: WorkExtrasDefinitions?
) : Parcelable

@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
@Parcelize
data class PayPeriodExtraAndTypeFull(
    @Embedded
    var payPeriodExtra: WorkPayPeriodExtras?,
    @Relation(
        entity = WorkExtraTypes::class,
        parentColumn = "ppeExtraTypeId",
        entityColumn = "workExtraTypeId",
    )
    var extraType: WorkExtraTypes?,
    @Relation(
        entity = WorkExtrasDefinitions::class,
        parentColumn = "ppeExtraTypeId",
        entityColumn = "weExtraTypeId",
    )
    var extraDef: WorkExtrasDefinitions?,

    ) : Parcelable