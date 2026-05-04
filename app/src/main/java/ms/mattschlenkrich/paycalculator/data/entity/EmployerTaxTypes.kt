package ms.mattschlenkrich.paycalculator.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.common.EMPLOYER_ID
import ms.mattschlenkrich.paycalculator.common.EMPLOYER_TAX_RULES_EMPLOYER_ID
import ms.mattschlenkrich.paycalculator.common.EMPLOYER_TAX_RULES_TAX_TYPE
import ms.mattschlenkrich.paycalculator.common.TABLE_EMPLOYER_TAX_TYPES
import ms.mattschlenkrich.paycalculator.common.WORK_TAX_TYPE


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