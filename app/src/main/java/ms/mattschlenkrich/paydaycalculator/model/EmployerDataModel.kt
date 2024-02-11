package ms.mattschlenkrich.paydaycalculator.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_NAME
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_TAX_RULES_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_TAX_RULES_TAX_TYPE
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYERS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYER_PAY_RATES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYER_TAX_TYPES
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_TYPE


@Entity(
    tableName = TABLE_EMPLOYERS,
    indices = [Index(value = [EMPLOYER_NAME], unique = true)]
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
    val mainMonthlyDate: Int,
    val employerIsDeleted: Boolean,
    val employerUpdateTime: String,
) : Parcelable


@Entity(
    tableName = TABLE_EMPLOYER_TAX_TYPES,
    primaryKeys = [
        EMPLOYER_TAX_RULES_EMPLOYER_ID,
        EMPLOYER_TAX_RULES_TAX_TYPE
    ],
    foreignKeys = [ForeignKey(
        entity = Employers::class,
        parentColumns = [EMPLOYER_ID],
        childColumns = [EMPLOYER_TAX_RULES_EMPLOYER_ID]
    ), ForeignKey(
        entity = TaxTypes::class,
        parentColumns = [WORK_TAX_TYPE],
        childColumns = [EMPLOYER_TAX_RULES_TAX_TYPE]
    )]
)
@Parcelize
data class EmployerTaxTypes(
    val etrEmployerId: Long,
    @ColumnInfo(index = true)
    val etrTaxType: String,
    val etrInclude: Boolean,
    val etrIsDeleted: Boolean,
    val etrUpdateTime: String
) : Parcelable

@Entity(
    tableName = TABLE_EMPLOYER_PAY_RATES,
    foreignKeys = [ForeignKey(
        entity = Employers::class,
        parentColumns = ["employerId"],
        childColumns = ["eprEmployerId"]
    )],
    indices = [Index(
        value = ["eprEmployerId", "eprEffectiveDate"],
        unique = true
    )]
)
data class EmployerPayRates(
    @PrimaryKey
    val employerPayRateId: Long,
    @ColumnInfo(index = true)
    val eprEmployerId: Long,
    @ColumnInfo(index = true)
    val eprEffectiveDate: String,
    val eprPerPeriod: Int,
    val eprPayRate: Double,
    val eprIsDeleted: Boolean,
    val eprUpdateTime: String,
)

