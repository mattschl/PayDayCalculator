package ms.mattschlenkrich.paycalculator.ui.timesheet.composable


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.common.DEFAULT_MIN_COLUMN_WIDTH
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.model.WorkDateExtraAndTypeAndDef
import ms.mattschlenkrich.paycalculator.ui.timesheet.TimeSheetPaySummary

@Composable
fun TimeSheetContent(
    modifier: Modifier = Modifier,
    paySummary: TimeSheetPaySummary,
    week1Summary: String,
    week2Summary: String,
    workDates: List<WorkDates>,
    workDateExtras: Map<Long, List<WorkDateExtraAndTypeAndDef>>,
    onWorkDateClick: (WorkDates) -> Unit,
    onWorkDateLongClick: (WorkDates) -> Unit,
    onViewPayDetailsClick: () -> Unit,
    week1EndDate: String,
    displayDate: (String) -> String,
    formatHours: (WorkDates) -> String,
    minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val widthDp = with(density) { windowInfo.containerSize.width.toDp() }
    val columns = maxOf(1, (widthDp.value / minColumnWidth).toInt())
    val isCompact = widthDp < 480.dp

    val activeWorkDates = remember(workDates) {
        workDates.filter { !it.wdIsDeleted }.sortedBy { it.wdDate }
    }
    val week1Dates = remember(activeWorkDates, week1EndDate) {
        activeWorkDates.filter { it.wdDate <= week1EndDate }
    }
    val week2Dates = remember(activeWorkDates, week1EndDate) {
        activeWorkDates.filter { it.wdDate > week1EndDate }
    }

    val displayDateMemo = remember(displayDate) { displayDate }
    val formatHoursMemo = remember(formatHours) { formatHours }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SCREEN_PADDING_HORIZONTAL),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) ELEMENT_SPACING / 2 else ELEMENT_SPACING),
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) ELEMENT_SPACING / 2 else ELEMENT_SPACING),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item(span = { GridItemSpan(columns) }) {
            Spacer(modifier = Modifier.height(SCREEN_PADDING_VERTICAL))
        }

        item(span = { GridItemSpan(columns) }) {
            TimeSheetSummaryCard(
                paySummary = paySummary,
                onViewPayDetailsClick = onViewPayDetailsClick
            )
        }

        items(
            items = week1Dates,
            key = { it.workDateId }
        ) { workDate ->
            WorkDateCard(
                workDate = workDate,
                extras = workDateExtras[workDate.workDateId] ?: emptyList(),
                onWorkDateClick = { onWorkDateClick(workDate) },
                onWorkDateLongClick = { onWorkDateLongClick(workDate) },
                displayDate = displayDateMemo,
                formatHours = formatHoursMemo,
                isCompact = isCompact
            )
        }

        if (week1Dates.isNotEmpty()) {
            item(span = { GridItemSpan(columns) }) {
                CenteredSummaryText(text = week1Summary, isCompact = isCompact)
            }
        }

        if (week1Dates.isNotEmpty() && week2Dates.isNotEmpty()) {
            item(span = { GridItemSpan(columns) }) {
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier.padding(vertical = if (isCompact) 4.dp else 8.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }

        items(
            items = week2Dates,
            key = { it.workDateId }
        ) { workDate ->
            WorkDateCard(
                workDate = workDate,
                extras = workDateExtras[workDate.workDateId] ?: emptyList(),
                onWorkDateClick = { onWorkDateClick(workDate) },
                onWorkDateLongClick = { onWorkDateLongClick(workDate) },
                displayDate = displayDateMemo,
                formatHours = formatHoursMemo,
                isCompact = isCompact
            )
        }

        if (week2Dates.isNotEmpty()) {
            item(span = { GridItemSpan(columns) }) {
                CenteredSummaryText(text = week2Summary, isCompact = isCompact)
            }
        }

        item(span = { GridItemSpan(columns) }) {
            Spacer(modifier = Modifier.height(SCREEN_PADDING_VERTICAL))
        }
    }
}

/*@OptIn(ExperimentalMaterial3Api::class)
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
    week1Summary: String,
    week2Summary: String,
    workDates: List<WorkDates>,
    workDateExtras: Map<Long, List<WorkDateExtraAndTypeAndDef>>,
    onWorkDateClick: (WorkDates) -> Unit,
    onWorkDateLongClick: (WorkDates) -> Unit,
    onAddWorkDateClick: () -> Unit,
    onViewPayDetailsClick: () -> Unit,
    onGenerateCutoffClick: () -> Unit,
    week1EndDate: String,
    displayDate: (String) -> String,
    formatHours: (WorkDates) -> String,
    minColumnWidth: Int = DEFAULT_MIN_COLUMN_WIDTH
) {
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
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SelectionCard(
                modifier = Modifier.padding(horizontal = SCREEN_PADDING_HORIZONTAL),
                employers = employers,
                selectedEmployer = selectedEmployer,
                onEmployerSelected = onEmployerSelected,
                onAddNewEmployer = onAddNewEmployer,
                cutOffDates = cutOffDates,
                selectedCutOffDate = selectedCutOffDate,
                onCutOffDateSelected = onCutOffDateSelected,
                onGenerateCutoffClick = onGenerateCutoffClick
            )

            TimeSheetContent(
                paySummary = paySummary,
                week1Summary = week1Summary,
                week2Summary = week2Summary,
                workDates = workDates,
                workDateExtras = workDateExtras,
                onWorkDateClick = onWorkDateClick,
                onWorkDateLongClick = onWorkDateLongClick,
                onViewPayDetailsClick = onViewPayDetailsClick,
                week1EndDate = week1EndDate,
                displayDate = displayDate,
                formatHours = formatHours,
                minColumnWidth = minColumnWidth
            )
        }
    }
}*/

@Composable
fun CenteredSummaryText(text: String, isCompact: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isCompact) 2.dp else 4.dp)
    )
}