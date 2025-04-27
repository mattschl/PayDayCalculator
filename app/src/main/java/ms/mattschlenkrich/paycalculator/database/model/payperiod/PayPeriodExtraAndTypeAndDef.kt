package ms.mattschlenkrich.paycalculator.database.model.payperiod

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtrasDefinitions

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