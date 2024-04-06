package ms.mattschlenkrich.paydaycalculator.model.tax

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.common.TABLE_TAX_EFFECTIVE_DATES

@Entity(
    tableName = TABLE_TAX_EFFECTIVE_DATES,
    indices = [Index(value = ["tdEffectiveDateId"], unique = true)]
)
@Parcelize
data class TaxEffectiveDates(
    @PrimaryKey
    val tdEffectiveDate: String,
    val tdEffectiveDateId: Long,
    val tdIsDeleted: Boolean,
    val tdUpdateTime: String,
) : Parcelable