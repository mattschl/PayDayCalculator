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
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_TAX_RULES
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_TAX_TYPES
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_RULE_TYPE
import ms.mattschlenkrich.paydaycalculator.common.WORK_TAX_TYPE

@Entity(
    tableName = TABLE_WORK_TAX_TYPES,
    indices = [Index(value = ["workTaxType"], unique = true)]
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
    primaryKeys = ["wtType", "wtLevel", "wtEffectiveDate"],
    foreignKeys = [ForeignKey(
        entity = WorkTaxTypes::class,
        parentColumns = [WORK_TAX_TYPE],
        childColumns = [WORK_TAX_RULE_TYPE]
    )]
)
@Parcelize
data class WorkTaxRules(
    val workTaxRuleId: Long,
    @ColumnInfo(index = true)
    val wtType: String,
    @ColumnInfo(index = true)
    val wtLevel: Int,
    @ColumnInfo(index = true)
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
        childColumns = [EMPLOYER_TAX_RULES_TAX_TYPE]
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

