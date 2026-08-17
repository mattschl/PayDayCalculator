package ms.mattschlenkrich.paycalculator.ui.jobspec.composable

import androidx.compose.runtime.Composable
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.common.compose.GenericViewScreen
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec

@Composable
fun JobSpecViewScreen(
    jobSpecList: List<JobSpec>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onJobSpecClick: (JobSpec) -> Unit,
    minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH
) {
    GenericViewScreen(
        itemList = jobSpecList,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onItemClick = onJobSpecClick,
        noItemsMessageRes = R.string.no_job_specs_to_view,
        itemContent = { item, onClick ->
            JobSpecItem(item, onClick)
        },
        minColumnWidth = minColumnWidth
    )
}