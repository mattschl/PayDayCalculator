package ms.mattschlenkrich.paycalculator.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "materialMerged",
    foreignKeys = [
        ForeignKey(
            entity = Material::class,
            parentColumns = ["materialId"],
            childColumns = ["mmMasterId"]
        ),
        ForeignKey(
            entity = Material::class,
            parentColumns = ["materialId"],
            childColumns = ["mmChildId"]
        )
    ]
)
@Parcelize
data class MaterialMerged(
    @PrimaryKey
    val materialMergeId: Long,
    @ColumnInfo(index = true)
    val mmMasterId: Long,
    @ColumnInfo(index = true)
    val mmChildId: Long,
    val mmIsDeleted: Boolean,
    val mmUpdateTime: String,
) : Parcelable