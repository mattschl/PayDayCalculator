package ms.mattschlenkrich.paycalculator.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.common.TABLE_SYNC_HISTORY

@Parcelize
@Entity(tableName = TABLE_SYNC_HISTORY)
data class SyncHistory(
    @PrimaryKey
    val syncId: Long,
    val syncTime: String,
    val syncSourceName: String,
    val syncDeviceId: Long,
    val syncStatus: String,
    val syncRecordsProcessed: String
) : Parcelable