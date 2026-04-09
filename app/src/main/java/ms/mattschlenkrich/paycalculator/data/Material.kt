package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "materials",
    indices = [Index(
        value = ["mName"], unique = true
    )]
)
@Parcelize
data class Material(
    @PrimaryKey
    val materialId: Long,
    val mName: String,
    val mCost: Double,
    val mPrice: Double,
    val mIsDeleted: Boolean,
    val mUpdateTime: String,
) : Parcelable
