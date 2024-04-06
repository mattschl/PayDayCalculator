package ms.mattschlenkrich.paydaycalculator.model.tax

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_TAX_RULES
import ms.mattschlenkrich.paydaycalculator.common.TAX_EFFECTIVE_DATE
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_RULE_EFFECTIVE_DATE
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_RULE_LEVEL
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_RULE_TYPE
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_TYPE


@Entity(
    tableName = TABLE_WORK_TAX_RULES,
    primaryKeys = [WORK_TAX_RULE_TYPE,
        WORK_TAX_RULE_LEVEL,
        WORK_TAX_RULE_EFFECTIVE_DATE],
    foreignKeys = [ForeignKey(
        entity = TaxTypes::class,
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
