package ms.mattschlenkrich.paydaycalculator.database.model.extras

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize


@Parcelize
data class ExtraTypeAndDef(
    @Embedded
    val extraType: WorkExtraTypes,
    @Relation(
        entity = WorkExtrasDefinitions::class,
        parentColumn = "workExtraTypeId",
        entityColumn = "weExtraTypeId"
    )
    val extraDef: WorkExtrasDefinitions
) : Parcelable

