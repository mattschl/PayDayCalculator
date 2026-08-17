package ms.mattschlenkrich.paycalculator.ui.workperformed.composable

import androidx.compose.runtime.Composable
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.common.compose.GenericViewScreen
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed

@Composable
fun WorkPerformedViewScreen(
    workPerformedList: List<WorkPerformed>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onWorkPerformedClick: (WorkPerformed) -> Unit,
    minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH
) {
    GenericViewScreen(
        itemList = workPerformedList,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onItemClick = onWorkPerformedClick,
        noItemsMessageRes = R.string.no_work_descriptions_to_view,
        itemContent = { item, onClick ->
            WorkPerformedItem(item, onClick)
        },
        minColumnWidth = minColumnWidth
    )
}