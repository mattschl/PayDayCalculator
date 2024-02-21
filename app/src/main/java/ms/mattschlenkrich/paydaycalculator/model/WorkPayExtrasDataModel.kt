package ms.mattschlenkrich.paydaycalculator.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.EMPLOYER_ID
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRAS_DEFINITIONS
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_EXTRA_TYPES
import ms.mattschlenkrich.paydaycalculator.common.WORK_EXTRA_DEFINITIONS_EMPLOYER_ID

@Entity(
    tableName = TABLE_WORK_EXTRA_TYPES,
    foreignKeys = [ForeignKey(
        entity = Employers::class,
        parentColumns = ["employerId"],
        childColumns = ["wetEmployerId"]
    )]
)
@Parcelize
data class WorkExtraTypes(
    @PrimaryKey
    val workExtraTypeId: Long,
    val wetName: String,
    @ColumnInfo(index = true)
    val wetEmployerId: Long,
    val wetAppliesTo: Int,
    val wetAttachTo: Int,
    val wetIsCredit: Boolean,
    val wetIsDefault: Boolean,
    val wetIsDeleted: Boolean,
    val wetUpdateTime: String,
) : Parcelable

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

@DatabaseView(
    "SELECT extraDef.*, " +
            "extraType.* " +
            "FROM $TABLE_WORK_EXTRAS_DEFINITIONS as extraDef " +
            "LEFT JOIN $TABLE_WORK_EXTRA_TYPES as extraType ON " +
            "extraDef.weExtraTypeId = extraType.workExtraTypeId"
)
@Parcelize
data class ExtraDefinitionAndType(
    @Embedded
    val definition: WorkExtrasDefinitions,
    @Relation(
        entity = WorkExtraTypes::class,
        parentColumn = "weExtraTypeId",
        entityColumn = "workExtraTypeId"
    )
    val extraType: WorkExtraTypes
) : Parcelable

@DatabaseView(
    "SELECT extraType.*, extraDef.* " +
            "FROM $TABLE_WORK_EXTRA_TYPES as extraType " +
            "LEFT JOIN $TABLE_WORK_EXTRAS_DEFINITIONS as extraDef ON " +
            "(workExtraTypeId = weExtraTypeId) " +
            "WHERE wetAttachTo = 1 " +
            "AND wetIsDeleted = 0 " +
            "ORDER BY wetName COLLATE NOCASE"
)
@Parcelize
data class ExtraTypeAndDefByDay(
    @Embedded
    val extraType: WorkExtraTypes,
    @Relation(
        entity = WorkExtrasDefinitions::class,
        parentColumn = "workExtraTypeId",
        entityColumn = "weExtraTypeId"
    )
    val extraDef: WorkExtrasDefinitions
) : Parcelable

@Parcelize
data class ExtraTypeAndDef(
    @Embedded
    val extraType: WorkExtraTypes,
    @Relation(
        entity = WorkExtrasDefinitions::class,
        parentColumn = "workExtraTypeId",
        entityColumn = "weExtraTypeId"
    )
    val extraDef: WorkExtrasDefinitions
) : Parcelable

data class EmployerDeductions(
    var deduction: String,
    var amount: Double,
)