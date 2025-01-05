package ms.mattschlenkrich.paycalculator.database.model.tax

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.RoomWarnings
import kotlinx.parcelize.Parcelize

@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
@Parcelize
data class TaxTypeAndRule(
    @Embedded
    val taxType: TaxTypes,
    @Relation(
        entity = WorkTaxRules::class,
        parentColumn = "taxType",
        entityColumn = "wtType"
    )
    val taxRule: WorkTaxRules,
) : Parcelable

