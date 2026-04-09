package ms.mattschlenkrich.paycalculator.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

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