package ms.mattschlenkrich.paycalculator.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "materialCostHistory",
    foreignKeys = [
        ForeignKey(
            entity = Material::class,
            parentColumns = ["materialId"],
            childColumns = ["mehMaterialId"],
        ),
    ]
)
@Parcelize
data class MaterialCostHistory(
    @PrimaryKey
    val materialExpenseHistoryId: Long = 0,
    @ColumnInfo(index = true)
    val mehMaterialId: Long,
    val mehRawCost: Double,
    val mehPurchaseDate: String,
    val mehIsDeleted: Boolean = false,
    val mehUpdateTime: String,
) : Parcelable {
}