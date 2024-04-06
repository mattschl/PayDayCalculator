package ms.mattschlenkrich.paydaycalculator.model.tax

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.TABLE_TAX_TYPES

@Entity(
    tableName = TABLE_TAX_TYPES,
    indices = [Index(value = ["taxType"], unique = true)]
)
@Parcelize
data class TaxTypes(
    @PrimaryKey
    val taxTypeId: Long,
    val taxType: String,
    val ttBasedOn: Int,
    val ttIsDeleted: Boolean,
    val ttUpdateTime: String,
) : Parcelable