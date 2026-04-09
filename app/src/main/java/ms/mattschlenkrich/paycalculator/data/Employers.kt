package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.common.EMPLOYER_NAME
import ms.mattschlenkrich.paycalculator.common.TABLE_EMPLOYERS

@Entity(
    tableName = TABLE_EMPLOYERS,
    indices = [Index(value = [EMPLOYER_NAME], unique = true)]
)
@Parcelize
data class Employers(
    @PrimaryKey
    val employerId: Long,
    val employerName: String,
    val payFrequency: String,
    val startDate: String,
    val dayOfWeek: String,
    val cutoffDaysBefore: Int,
    val midMonthlyDate: Int,
    val mainMonthlyDate: Int,
    val employerIsDeleted: Boolean,
    val employerUpdateTime: String,
) : Parcelable



