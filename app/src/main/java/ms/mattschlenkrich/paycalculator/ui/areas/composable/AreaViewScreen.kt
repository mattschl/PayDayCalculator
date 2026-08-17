package ms.mattschlenkrich.paycalculator.ui.areas.composable

import androidx.compose.runtime.Composable
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.common.compose.GenericViewScreen
import ms.mattschlenkrich.paycalculator.data.entity.Areas

@Composable
fun AreaViewScreen(
    areaList: List<Areas>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAreaClick: (Areas) -> Unit,
    minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH
) {
    GenericViewScreen(
        itemList = areaList,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onItemClick = onAreaClick,
        noItemsMessageRes = R.string.no_areas_in_the_list_to_view,
        itemContent = { item, onClick ->
            AreaItem(item, onClick)
        },
        minColumnWidth = minColumnWidth
    )
}