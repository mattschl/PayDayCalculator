package ms.mattschlenkrich.paydaycalculator.model.payperiod

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.TABLE_WORK_DATE_EXTRAS


@Entity(
    tableName = TABLE_WORK_DATE_EXTRAS,
    foreignKeys = [ForeignKey(
        entity = WorkDates::class,
        parentColumns = ["workDateId"],
        childColumns = ["wdeWorkDateId"]
    )],
    indices = [Index(
        value =
        ["wdeWorkDateId", "wdeName"], unique = true
    )]
)
@Parcelize
data class WorkDateExtras(
    @PrimaryKey
    val workDateExtraId: Long,
    @ColumnInfo(index = true)
    val wdeWorkDateId: Long,
    @ColumnInfo(index = true)
    val wdeExtraTypeId: Long?,
    @ColumnInfo(index = true)
    val wdeName: String,
    val wdeAppliesTo: Int,
    val wdeAttachTo: Int,
    val wdeValue: Double,
    val wdeIsFixed: Boolean,
    val wdeIsCredit: Boolean,
    val wdeIsDeleted: Boolean,
    val wdeUpdateTime: String,
) : Parcelable


@Parcelize
data class WorkDateAndExtras(
    @Embedded
    val workDate: WorkDates,
    @Relation(
        entity = WorkDateExtras::class,
        parentColumn = "workDateId",
        entityColumn = "wdeWorkDateId"
    )
    var extras: WorkDateExtras?
) : Parcelable
