package ms.mattschlenkrich.paycalculator.ui.paydetail.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.MAX_CONTENT_WIDTH
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.calculateGridColumns
import ms.mattschlenkrich.paycalculator.data.model.ExtraContainer
import ms.mattschlenkrich.paycalculator.data.model.TaxAndAmount
import ms.mattschlenkrich.paycalculator.ui.paydetail.HourlyBreakdownData
import ms.mattschlenkrich.paycalculator.ui.paydetail.PaySummaryData

@Composable
fun PayDetailContent(
    modifier: Modifier = Modifier,
    paySummary: PaySummaryData,
    hourlyBreakdown: HourlyBreakdownData,
    credits: List<ExtraContainer>,
    deductions: List<ExtraContainer>,
    taxes: List<TaxAndAmount>,
    onAddCreditClick: () -> Unit,
    onAddDeductionClick: () -> Unit,
    onExtraClick: (ExtraContainer) -> Unit,
    onExtraActiveChange: (ExtraContainer, Boolean) -> Unit
) {
    val columns = calculateGridColumns()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .widthIn(max = MAX_CONTENT_WIDTH)
                .fillMaxSize()
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL),
            verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING),
            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item(span = { GridItemSpan(columns) }) {
                Spacer(modifier = Modifier.height(SCREEN_PADDING_VERTICAL))
            }

            item(span = { GridItemSpan(columns) }) {
                SummaryCard(data = paySummary)
            }

            item(span = { GridItemSpan(columns) }) {
                HourlyBreakdownCard(data = hourlyBreakdown)
            }

            item {
                ExtrasCard(
                    title = stringResource(R.string.credits),
                    extras = credits,
                    total = paySummary.totalCredits,
                    onAddClick = onAddCreditClick,
                    onExtraClick = onExtraClick,
                    onActiveChange = onExtraActiveChange,
                    addButtonContentDescription = stringResource(R.string.add_new_credit)
                )
            }

            item {
                ExtrasCard(
                    title = stringResource(R.string.deductions),
                    extras = deductions,
                    taxes = taxes,
                    total = paySummary.totalDeductions,
                    onAddClick = onAddDeductionClick,
                    onExtraClick = onExtraClick,
                    onActiveChange = onExtraActiveChange,
                    addButtonContentDescription = stringResource(R.string.add_new_deductions)
                )
            }

            item(span = { GridItemSpan(columns) }) {
                Spacer(modifier = Modifier.height(SCREEN_PADDING_VERTICAL))
            }
        }
    }
}