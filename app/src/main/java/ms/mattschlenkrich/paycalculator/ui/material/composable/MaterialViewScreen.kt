package ms.mattschlenkrich.paycalculator.ui.material.composable

import androidx.compose.runtime.Composable
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.GenericViewScreen
import ms.mattschlenkrich.paycalculator.data.entity.Material

@Composable
fun MaterialViewScreen(
    materialList: List<Material>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMaterialClick: (Material) -> Unit,
    minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH
) {
    val nf = NumberFunctions()
    GenericViewScreen(
        itemList = materialList,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onItemClick = onMaterialClick,
        noItemsMessageRes = R.string.no_materials_to_view,
        itemContent = { item, onClick ->
            MaterialItem(item, nf, onClick)
        },
        minColumnWidth = minColumnWidth
    )
}