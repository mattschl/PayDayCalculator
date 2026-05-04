package ms.mattschlenkrich.paycalculator.data.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.MaterialMerged

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