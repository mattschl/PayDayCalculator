package ms.mattschlenkrich.paycalculator.ui.timesheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.calculateGridColumns
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.WorkDateExtraAndTypeAndDef
import ms.mattschlenkrich.paycalculator.data.WorkDates
import ms.mattschlenkrich.paycalculator.ui.timesheet.components.TimeSheetSelectionCard
import ms.mattschlenkrich.paycalculator.ui.timesheet.components.TimeSheetSummaryCard
import ms.mattschlenkrich.paycalculator.ui.timesheet.components.WorkDateCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSheetScreen(
    employers: List<Employers>,
    selectedEmployer: Employers?,
    onEmployerSelected: (Employers) -> Unit,
    onAddNewEmployer: () -> Unit,
    cutOffDates: List<String>,
    selectedCutOffDate: String,
    onCutOffDateSelected: (String) -> Unit,
    paySummary: TimeSheetPaySummary,
    workDates: List<WorkDates>,
    workDateExtras: Map<Long, List<WorkDateExtraAndTypeAndDef>>,
    onWorkDateClick: (WorkDates) -> Unit,
    onWorkDateLongClick: (WorkDates) -> Unit,
    onAddWorkDateClick: () -> Unit,
    onViewPayDetailsClick: () -> Unit,
    onGenerateCutoffClick: () -> Unit,
    displayDate: (String) -> String,
    formatHours: (WorkDates) -> String
) {
    val columns = calculateGridColumns()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddWorkDateClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_a_new_work_date)
                )
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL),
            verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING),
            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item(span = { GridItemSpan(columns) }) {
                Spacer(modifier = Modifier.height(SCREEN_PADDING_VERTICAL))
            }

            item(span = { GridItemSpan(columns) }) {
                TimeSheetSelectionCard(
                    employers = employers,
                    selectedEmployer = selectedEmployer,
                    onEmployerSelected = onEmployerSelected,
                    onAddNewEmployer = onAddNewEmployer,
                    cutOffDates = cutOffDates,
                    selectedCutOffDate = selectedCutOffDate,
                    onCutOffDateSelected = onCutOffDateSelected,
                    onGenerateCutoffClick = onGenerateCutoffClick
                )
            }

            item(span = { GridItemSpan(columns) }) {
                TimeSheetSummaryCard(
                    paySummary = paySummary,
                    onViewPayDetailsClick = onViewPayDetailsClick
                )
            }

            items(workDates.filter { !it.wdIsDeleted }.sortedBy { it.wdDate }) { workDate ->
                WorkDateCard(
                    workDate = workDate,
                    extras = workDateExtras[workDate.workDateId] ?: emptyList(),
                    onWorkDateClick = { onWorkDateClick(workDate) },
                    onWorkDateLongClick = { onWorkDateLongClick(workDate) },
                    displayDate = displayDate,
                    formatHours = formatHours
                )
            }

            item(span = { GridItemSpan(columns) }) {
                Spacer(modifier = Modifier.height(SCREEN_PADDING_VERTICAL))
            }
        }
    }
}