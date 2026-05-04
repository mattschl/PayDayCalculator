package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtrasDefinitions
import ms.mattschlenkrich.paycalculator.data.entity.WorkPayPeriodExtras

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