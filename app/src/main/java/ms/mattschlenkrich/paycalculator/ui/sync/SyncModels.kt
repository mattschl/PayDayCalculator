package ms.mattschlenkrich.paycalculator.ui.sync

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

data class DriveFileMeta(
    val id: String,
    val name: String,
    val size: Long? = null,
    val modifiedTime: Long? = null
)

@Parcelize
data class ConflictInfo(
    val tableName: String,
    val localName: String,
    val localId: Long,
    val localTime: String,
    val driveId: Long,
    val driveTime: String,
    @StringRes val messageRes: Int? = null
) : Parcelable

enum class ConflictChoice {
    KEEP_LOCAL,
    KEEP_DRIVE
}