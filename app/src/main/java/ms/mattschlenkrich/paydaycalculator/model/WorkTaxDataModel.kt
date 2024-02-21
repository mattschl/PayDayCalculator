package ms.mattschlenkrich.paydaycalculator.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.TABLE_TAX_EFFECTIVE_DATES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_TAX_TYPES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_TAX_RULES
import ms.mattschlenkrich.paydaycalculator.common.TAX_EFFECTIVE_DATE
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_RULE_EFFECTIVE_DATE
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_RULE_LEVEL
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_RULE_TYPE
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_TYPE

@Entity(
    tableName = TABLE_TAX_TYPES,
    indices = [Index(value = ["taxType"], unique = true)]
)
@Parcelize
data class TaxTypes(
    @PrimaryKey
    val taxTypeId: Long,
    val taxType: String,
    val ttIsDeleted: Boolean,
    val ttUpdateTime: String,
) : Parcelable

@Entity(
    tableName = TABLE_TAX_EFFECTIVE_DATES,
    indices = [Index(value = ["tdEffectiveDateId"], unique = true)]
)
@Parcelize
data class TaxEffectiveDates(
    @PrimaryKey
    val tdEffectiveDate: String,
    val tdEffectiveDateId: Long,
    val tdIsDeleted: Boolean,
    val tdUpdateTime: String,
) : Parcelable


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

@Parcelize
data class TaxComplete(
    @Embedded
    val taxType: TaxTypes,
    @Relation(
        entity = WorkTaxRules::class,
        parentColumn = "taxTypeId",
        entityColumn = "wtType"
    )
    val taxRule: WorkTaxRules,
) : Parcelable