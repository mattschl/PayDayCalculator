package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.common.EMPLOYER_ID
import ms.mattschlenkrich.paycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paycalculator.common.WORK_EXTRA_DEFINITIONS_EMPLOYER_ID
import ms.mattschlenkrich.paycalculator.data.Employers


@Entity(
    tableName = TABLE_WORK_EXTRAS_DEFINITIONS,
    foreignKeys = [ForeignKey(
        entity = Employers::class,
        parentColumns = [EMPLOYER_ID],
        childColumns = [WORK_EXTRA_DEFINITIONS_EMPLOYER_ID]
    ), ForeignKey(
        entity = WorkExtraTypes::class,
        parentColumns = ["workExtraTypeId"],
        childColumns = ["weExtraTypeId"]
    )]
)
@Parcelize
data class WorkExtrasDefinitions(
    @PrimaryKey
    val workExtraDefId: Long,
    @ColumnInfo(index = true)
    val weEmployerId: Long,
    @ColumnInfo(index = true)
    val weExtraTypeId: Long,
    val weValue: Double,
    val weIsFixed: Boolean,
    val weEffectiveDate: String?,
    val weIsDeleted: Boolean,
    val weUpdateTime: String,
) : Parcelable


