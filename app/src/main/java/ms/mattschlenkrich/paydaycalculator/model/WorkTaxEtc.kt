package ms.mattschlenkrich.paydaycalculator.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_TAX_RULES_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_TAX_RULES_TAX_TYPE
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYER_TAX_RULES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_TAX_EFFECTIVE_DATES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_TAX_RULES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_TAX_TYPES
import ms.mattschlenkrich.paydaycalculator.common.TAX_EFFECTIVE_DATE
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_RULE_EFFECTIVE_DATE
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_RULE_LEVEL
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_RULE_TYPE
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_TYPE

@Entity(
    tableName = TABLE_WORK_TAX_TYPES
)
@Parcelize
data class WorkTaxTypes(
    @PrimaryKey
    val workTaxType: String,
) : Parcelable


@Entity(
    tableName = TABLE_TAX_EFFECTIVE_DATES
)
@Parcelize
data class TaxEffectiveDates(
    @PrimaryKey
    val tdEffectiveDate: String,
) : Parcelable

@Entity(
    tableName = TABLE_WORK_TAX_RULES,
    primaryKeys = [WORK_TAX_RULE_TYPE, WORK_TAX_RULE_LEVEL, WORK_TAX_RULE_EFFECTIVE_DATE],
    foreignKeys = [ForeignKey(
        entity = WorkTaxTypes::class,
        parentColumns = [WORK_TAX_TYPE],
        childColumns = [WORK_TAX_RULE_TYPE]
    ), ForeignKey(
        entity = TaxEffectiveDates::class,
        parentColumns = [TAX_EFFECTIVE_DATE],
        childColumns = [WORK_TAX_RULE_EFFECTIVE_DATE]
    )],
    indices = [Index(
        value = [WORK_TAX_RULE_EFFECTIVE_DATE]
    )]
)
@Parcelize
data class WorkTaxRules(
    val workTaxRuleId: Long,
    val wtType: String,
    val wtLevel: Int,
    val wtEffectiveDate: String,
    val wtPercent: Double,
    val wtHasExemption: Boolean,
    val wtExemptionAmount: Double,
    val wtHasBracket: Boolean,
    val wtBracketAmount: Double,
    val wtIsDeleted: Boolean,
    val wtUpdateTime: String,
) : Parcelable

@Entity(
    tableName = TABLE_EMPLOYER_TAX_RULES,
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
        childColumns = [EMPLOYER_TAX_RULES_EMPLOYER_ID]
    )]
)
@Parcelize
data class EmployerTaxRules(
    val etrEmployerId: Long,
    @ColumnInfo(index = true)
    val etrTaxType: Long,
    val etrInclude: Boolean,
    val etrIsDeleted: Boolean,
    val etrUpdateTime: String
) : Parcelable

