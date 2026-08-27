package ms.mattschlenkrich.paycalculator.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.common.NumberFunctions

@Composable
fun CalculatorScreen(
    displayValue: String,
    formulaList: List<String>,
    transferResult: Double,
    onDigitClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    onEqualClick: () -> Unit,
    onClearClick: () -> Unit,
    onClearAllClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onTransferClick: () -> Unit
) {
    val nf = NumberFunctions()
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = displayValue,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            val listState = rememberLazyListState()
            LaunchedEffect(formulaList.size) {
                if (formulaList.isNotEmpty()) {
                    listState.animateScrollToItem(formulaList.size - 1)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                state = listState
            ) {
                items(formulaList) { formula ->
                    Text(
                        text = formula,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val buttonModifier = Modifier
                    .size(64.dp)
                val primaryButtonColors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
                val secondaryButtonColors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalcButton("7", secondaryButtonColors, buttonModifier) { onDigitClick("7") }
                    CalcButton("8", secondaryButtonColors, buttonModifier) { onDigitClick("8") }
                    CalcButton("9", secondaryButtonColors, buttonModifier) { onDigitClick("9") }
                    CalcButton(
                        "/",
                        primaryButtonColors,
                        buttonModifier
                    ) { onOperatorClick("/") }
                    CalcButton("<-", primaryButtonColors, buttonModifier) { onBackspaceClick() }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalcButton("4", secondaryButtonColors, buttonModifier) { onDigitClick("4") }
                    CalcButton("5", secondaryButtonColors, buttonModifier) { onDigitClick("5") }
                    CalcButton("6", secondaryButtonColors, buttonModifier) { onDigitClick("6") }
                    CalcButton(
                        "X",
                        primaryButtonColors,
                        buttonModifier
                    ) { onOperatorClick("X") }
                    CalcButton("C", primaryButtonColors, buttonModifier) { onClearClick() }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalcButton("1", secondaryButtonColors, buttonModifier) { onDigitClick("1") }
                    CalcButton("2", secondaryButtonColors, buttonModifier) { onDigitClick("2") }
                    CalcButton("3", secondaryButtonColors, buttonModifier) { onDigitClick("3") }
                    CalcButton(
                        "-",
                        primaryButtonColors,
                        buttonModifier
                    ) { onOperatorClick("-") }
                    CalcButton("CA", primaryButtonColors, buttonModifier) { onClearAllClick() }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalcButton(".", secondaryButtonColors, buttonModifier) { onDigitClick(".") }
                    CalcButton("0", secondaryButtonColors, buttonModifier) { onDigitClick("0") }
                    CalcButton("+/-", secondaryButtonColors, buttonModifier) { onDigitClick("-") }
                    CalcButton(
                        "+",
                        primaryButtonColors,
                        buttonModifier
                    ) { onOperatorClick("+") }
                    CalcButton("=", primaryButtonColors, buttonModifier) { onEqualClick() }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onTransferClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    text = "TRANSFER ${nf.displayDollars(transferResult)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CalcButton(
    text: String,
    colors: ButtonColors,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}