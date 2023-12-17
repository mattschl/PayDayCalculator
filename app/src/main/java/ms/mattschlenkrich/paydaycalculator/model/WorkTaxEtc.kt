package ms.mattschlenkrich.paydaycalculator.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_TAX_RULES_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_TAX_RULES_TAX_TYPE_ID
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYER_TAX_RULES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_TAX_RULES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_TAX_TYPES
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_RULE_TYPE_ID
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_TYPE_ID


@Entity(
    tableName = TABLE_WORK_TAX_TYPES
)
@Parcelize
data class WorkTaxTypes(
    @PrimaryKey
    val workTaxTypeId: Long,
    val workTaxType: String,
    val wttIsDeleted: Boolean,
    val wttUpdateTime: String,
) : Parcelable

@Entity(
    tableName = TABLE_WORK_TAX_RULES,
    foreignKeys = [ForeignKey(
        entity = WorkTaxTypes::class,
        parentColumns = [WORK_TAX_TYPE_ID],
        childColumns = [WORK_TAX_RULE_TYPE_ID]
    )]
)
@Parcelize
data class WorkTaxRules(
    @PrimaryKey
    val workTaxRuleId: Long,
    @ColumnInfo(index = true)
    val wtName: String,
    val wtTypeId: String,
    val wtPercent: Double,
    val wtHasExemption: Boolean,
    val wtExemptionAmount: Double,
    val wtHasBracket: Boolean,
    val wtBracketAmount: Double,
    val wtEffectiveDate: String,
    val wtIsDeleted: Boolean,
    val wtUpdateTime: String,
) : Parcelable

@Entity(
    tableName = TABLE_EMPLOYER_TAX_RULES,
    primaryKeys = [
        EMPLOYER_TAX_RULES_EMPLOYER_ID,
        EMPLOYER_TAX_RULES_TAX_TYPE_ID
    ],
    foreignKeys = [ForeignKey(
        entity = Employers::class,
        parentColumns = [EMPLOYER_ID],
        childColumns = [EMPLOYER_TAX_RULES_EMPLOYER_ID]
    ), ForeignKey(
        entity = WorkTaxTypes::class,
        parentColumns = [WORK_TAX_TYPE_ID],
        childColumns = [EMPLOYER_TAX_RULES_TAX_TYPE_ID]
    )]
)
@Parcelize
data class EmployerTaxRules(
    val etrEmployerId: Long,
    @ColumnInfo(index = true)
    val etrTaxTypeId: Long,
    val etrInclude: Boolean,
    val etrIsDeleted: Boolean,
    val etrUpdateTime: String
) : Parcelable

@Parcelize
data class TaxRuleWithType(
    @Embedded
    val taxRule: WorkTaxRules,
    @Relation(
        entity = WorkTaxTypes::class,
        parentColumn = WORK_TAX_RULE_TYPE_ID,
        entityColumn = WORK_TAX_TYPE_ID
    )
    val taxType: WorkTaxTypes,
) : Parcelable