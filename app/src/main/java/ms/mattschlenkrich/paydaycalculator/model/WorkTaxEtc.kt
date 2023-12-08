package ms.mattschlenkrich.paydaycalculator.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_TAX_TYPES


@Entity(
    tableName = TABLE_WORK_TAX_TYPES
)
@Parcelize
data class WorkTaxTypes(
    @PrimaryKey
    val workTaxType: String,
    val wttIsDeleted: Boolean,
    val wttUpdateTime: String,
) : Parcelable

//@Entity(
//    tableName = TABLE_WORK_TAX_RULES,
//    foreignKeys = [ForeignKey(
//        entity = WorkTaxTypes::class,
//        parentColumns = [WORK_TAX_TYPE],
//        childColumns = [WORK_TAX_RULE_TYPE]
//    )]
//)
//@Parcelize
//data class WorkTaxRules(
//    @PrimaryKey
//    val workTaxRuleId: Long,
//    val workTaxRuleType: String,
//    val wtPercent: Double,
//    val wtHasExemption: Boolean,
//    val wtExemptionAmount: Double,
//    val wtHasBracket: Boolean,
//    val wtBracketAmount: Double,
//    val wtEffectiveDate: String,
//    val wtIsDeleted: Boolean,
//    val wtUpdateTime: String,
//) : Parcelable

//@Entity(
//    tableName = TABLE_EMPLOYER_TAX_RULES,
//    primaryKeys = [
//        EMPLOYER_TAX_RULES_EMPLOYER_ID,
//    EMPLOYER_TAX_RULES_TAX_TYPE
//                  ],
//    foreignKeys = [ForeignKey(
//        entity = Employers::class,
//        parentColumns = [EMPLOYER_ID],
//        childColumns = [EMPLOYER_TAX_RULES_EMPLOYER_ID]
//    ), ForeignKey(
//        entity = WorkTaxTypes::class,
//        parentColumns = [WORK_TAX_TYPE],
//        childColumns = [EMPLOYER_TAX_RULES_TAX_TYPE]
//    )]
//)
//@Parcelize
//data class EmployerTaxRules(
//    val etrEmployerId: Long,
//    val etrTaxType: String,
//    val etrInclude: Boolean,
//    val etrIsDeleted: Boolean,
//    val etrUpdateTime: String,
//) : Parcelable