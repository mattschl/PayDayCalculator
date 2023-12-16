package ms.mattschlenkrich.paydaycalculator.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRA_FREQUENCIES
import ms.mattschlenkrich.paydaycalculator.common.WORK_EXTRA_DEFINITIONS_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.WORK_EXTRA_DEFINITIONS_FREQUENCY_ID
import ms.mattschlenkrich.paydaycalculator.common.WORK_EXTRA_FREQUENCY_ID


@Entity(
    tableName = TABLE_WORK_EXTRAS_DEFINITIONS,
    foreignKeys = [ForeignKey(
        entity = WorkExtraFrequencies::class,
        parentColumns = [WORK_EXTRA_FREQUENCY_ID],
        childColumns = [WORK_EXTRA_DEFINITIONS_FREQUENCY_ID]
    ), ForeignKey(
        entity = Employers::class,
        parentColumns = [EMPLOYER_ID],
        childColumns = [WORK_EXTRA_DEFINITIONS_EMPLOYER_ID]
    )]
)
@Parcelize
data class WorkExtrasDefinitions(
    @PrimaryKey
    val workExtraId: Long,
    @ColumnInfo(index = true)
    val weEmployerId: Long,
    val weName: String,
    @ColumnInfo(index = true)
    val weFrequencyId: Long,
    val weValue: Double,
    val weIsCredit: Boolean,
    val weIsDefault: Boolean,
    val weEffectiveDate: String,
    val weIsDeleted: Boolean,
    val weUpdateTime: String,
) : Parcelable

@Entity(
    tableName = TABLE_WORK_EXTRA_FREQUENCIES,
)
@Parcelize
data class WorkExtraFrequencies(
    @PrimaryKey
    val workExtraFrequencyId: Long,
    @ColumnInfo(index = true)
    val workExtraFrequencyName: String,
    val wefIsDeleted: Boolean,
    val wefUpdateTime: String,
) : Parcelable