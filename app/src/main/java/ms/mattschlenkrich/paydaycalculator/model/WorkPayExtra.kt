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
import ms.mattschlenkrich.paydaycalculator.common.WORK_EXTRA_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.WORK_EXTRA_FREQUENCY
import ms.mattschlenkrich.paydaycalculator.common.WORK_EXTRA_FREQUENCY_NAME


@Entity(
    tableName = TABLE_WORK_EXTRAS_DEFINITIONS,
    foreignKeys = [ForeignKey(
        entity = WorkExtraFrequencies::class,
        parentColumns = [WORK_EXTRA_FREQUENCY_NAME],
        childColumns = [WORK_EXTRA_FREQUENCY]
    ), ForeignKey(
        entity = Employers::class,
        parentColumns = [EMPLOYER_ID],
        childColumns = [WORK_EXTRA_EMPLOYER_ID]
    )]
)
@Parcelize
data class WorkExtrasDefinitions(
    @PrimaryKey
    val workExtraId: Long,
    val weEmployerId: Long,
    val weName: String,
    val weFrequency: String,
    val weValue: Double,
    @ColumnInfo(defaultValue = "1")
    val weIsCredit: Boolean,
    @ColumnInfo(defaultValue = "1")
    val weIsDefault: Boolean,
    @ColumnInfo(defaultValue = "0")
    val weEffectiveDate: String,
    val weIsDeleted: Boolean,
    val weUpdateTime: String,
) : Parcelable

@Entity(
    tableName = TABLE_WORK_EXTRA_FREQUENCIES
)
@Parcelize
data class WorkExtraFrequencies(
    @PrimaryKey
    val workExtraFrequencyName: String
) : Parcelable