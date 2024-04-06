package ms.mattschlenkrich.paydaycalculator.model.employer

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ms.mattschlenkrich.paydaycalculator.common.TABLE_EMPLOYER_PAY_RATES


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