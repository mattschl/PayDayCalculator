package ms.mattschlenkrich.paydaycalculator.model.extras

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paydaycalculator.common.WORK_EXTRA_DEFINITIONS_EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers


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


@Parcelize
data class ExtraDefinitionFull(
    @Embedded
    val definition: WorkExtrasDefinitions,
    @Relation(
        entity = Employers::class,
        parentColumn = "weEmployerId",
        entityColumn = "employerId"
    )
    var employer: Employers,
    @Relation(
        entity = WorkExtraTypes::class,
        parentColumn = "weExtraTypeId",
        entityColumn = "workExtraTypeId"
    )
    var extraType: WorkExtraTypes
) : Parcelable
