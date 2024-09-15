package ms.mattschlenkrich.paydaycalculator.database.model.payperiod

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.RoomWarnings
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paydaycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.database.model.extras.WorkExtrasDefinitions

@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
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