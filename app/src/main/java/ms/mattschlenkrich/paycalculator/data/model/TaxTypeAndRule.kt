package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.entity.TaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkTaxRules

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