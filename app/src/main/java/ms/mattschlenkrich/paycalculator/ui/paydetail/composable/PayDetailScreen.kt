package ms.mattschlenkrich.paycalculator.ui.paydetail.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SCREEN_PADDING_HORIZONTAL)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
    ) {
        Spacer(modifier = Modifier.height(SCREEN_PADDING_VERTICAL))

        SummaryCard(data = paySummary)

        HourlyBreakdownCard(data = hourlyBreakdown)

        ExtrasCard(
            title = stringResource(R.string.credits),
            extras = credits,
            total = paySummary.totalCredits,
            onAddClick = onAddCreditClick,
            onExtraClick = onExtraClick,
            onActiveChange = onExtraActiveChange,
            addButtonContentDescription = stringResource(R.string.add_new_credit)
        )

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

        Spacer(modifier = Modifier.height(SCREEN_PADDING_VERTICAL))
    }
}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayDetailScreen(
    employers: List<Employers>,
    selectedEmployer: Employers?,
    onEmployerSelected: (Employers) -> Unit,
    onAddNewEmployer: () -> Unit,
    cutOffDates: List<String>,
    selectedCutOffDate: String,
    onCutOffDateSelected: (String) -> Unit,
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
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                onCutOffDateSelected = onCutOffDateSelected
            )

            PayDetailContent(
                paySummary = paySummary,
                hourlyBreakdown = hourlyBreakdown,
                credits = credits,
                deductions = deductions,
                taxes = taxes,
                onAddCreditClick = onAddCreditClick,
                onAddDeductionClick = onAddDeductionClick,
                onExtraClick = onExtraClick,
                onExtraActiveChange = onExtraActiveChange
            )
        }
    }
}*/