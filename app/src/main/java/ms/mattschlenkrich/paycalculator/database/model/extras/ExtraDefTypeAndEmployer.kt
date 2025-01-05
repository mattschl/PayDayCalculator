package ms.mattschlenkrich.paycalculator.database.model.extras

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers

@Parcelize
data class ExtraDefTypeAndEmployer(
    @Embedded
    val definition: WorkExtrasDefinitions,
    @Relation(
        entity = Employers::class,
        parentColumn = "weEmployerId",
        entityColumn = "employerId"
    )
    var employer: Employers,
    @Relation(
        entity = WorkExtraTypes::class,
        parentColumn = "weExtraTypeId",
        entityColumn = "workExtraTypeId"
    )
    var extraType: WorkExtraTypes
) : Parcelable