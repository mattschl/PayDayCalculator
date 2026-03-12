package ms.mattschlenkrich.paycalculator.database.model.workorder.merged

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.database.model.workorder.Material

@Parcelize
data class MaterialAndChild(
    @Embedded
    val materialMerged: MaterialMerged,
    @Relation(
        entity = Material::class,
        "mmMasterId",
        entityColumn = "materialId"
    )
    val materialParent: Material,
    @Relation(
        entity = Material::class,
        "mmChildId",
        entityColumn = "materialId"
    )
    val materialChild: Material
) : Parcelable