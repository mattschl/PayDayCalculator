package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
data class PayPeriodExtraAndTypeAndDef(
    @Embedded
    var payPeriodExtra: WorkPayPeriodExtras?,
    @Relation(
        entity = WorkExtraTypes::class,
        parentColumn = "ppeExtraTypeId",
        entityColumn = "workExtraTypeId",
    )
    var extraType: WorkExtraTypes?,
    @Relation(
        entity = WorkExtrasDefinitions::class,
        parentColumn = "ppeExtraTypeId",
        entityColumn = "weExtraTypeId",
    )
    var extraDef: WorkExtrasDefinitions?,

    ) : Parcelable