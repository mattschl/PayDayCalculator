package ms.mattschlenkrich.paydaycalculator.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_TAX_RULES_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_TAX_RULES_TAX_TYPE
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYERS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYER_TAX_TYPES
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_TYPE


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
        entity = WorkTaxTypes::class,
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

