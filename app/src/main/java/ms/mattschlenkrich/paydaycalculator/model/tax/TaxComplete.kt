package ms.mattschlenkrich.paydaycalculator.model.tax

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.RoomWarnings
import kotlinx.parcelize.Parcelize

@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
@Parcelize
data class TaxComplete(
    @Embedded
    val taxType: TaxTypes,
    @Relation(
        entity = WorkTaxRules::class,
        parentColumn = "taxType",
        entityColumn = "wtType"
    )
    val taxRule: WorkTaxRules,
) : Parcelable

