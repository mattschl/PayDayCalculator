package ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryExpense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderHistoryExpenseDialog(
    mainViewModel: ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel,
    navController: androidx.navigation.NavController,
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    expense: WorkOrderHistoryExpense? = null,
    onAddExpense: (String, String, String, String) -> Unit,
    onUpdateExpense: (WorkOrderHistoryExpense) -> Unit = {},
    onDeleteExpense: (Long) -> Unit = {}
) {
    if (showDialog) {
        val nf = remember { NumberFunctions() }
        var expenseType by rememberSaveable { mutableStateOf(expense?.woheType ?: "") }
        var supplier by rememberSaveable { mutableStateOf(expense?.woheSupplier ?: "") }
        var invoiceNo by rememberSaveable { mutableStateOf(expense?.woheInvoiceNo ?: "") }
        var amount by rememberSaveable {
            mutableStateOf(
                expense?.let { nf.displayNumberFromDouble(it.woheAmount) } ?: ""
            )
        }

        LaunchedEffect(expense) {
            if (expense != null) {
                expenseType = expense.woheType
                supplier = expense.woheSupplier
                invoiceNo = expense.woheInvoiceNo
                amount = nf.displayNumberFromDouble(expense.woheAmount)
            }
        }

        LaunchedEffect(mainViewModel.getTransferNum()) {
            val transferNum = mainViewModel.getTransferNum()
            if (transferNum != 0.0) {
                amount = nf.displayNumberFromDouble(transferNum)
                mainViewModel.setTransferNum(0.0)
            }
        }

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (expense == null) stringResource(R.string.add_expense)
                    else stringResource(R.string.update_expense),
                    style = MaterialTheme.typography.titleLarge
                )

                SelectAllOutlinedTextField(
                    value = expenseType,
                    onValueChange = { expenseType = it },
                    label = { Text(stringResource(R.string.expense_type)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = expenseType.isBlank()
                )
                if (expenseType.isBlank()) {
                    Text(
                        text = stringResource(R.string.expense_type_is_required),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                SelectAllOutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text(stringResource(R.string.supplier)) },
                    modifier = Modifier.fillMaxWidth()
                )

                SelectAllOutlinedTextField(
                    value = invoiceNo,
                    onValueChange = { invoiceNo = it },
                    label = { Text(stringResource(R.string.invoice_no)) },
                    modifier = Modifier.fillMaxWidth()
                )

                val amountValue = try {
                    nf.getDoubleFromDollars(amount)
                } catch (_: Exception) {
                    null
                }
                val isAmountError =
                    amount.isNotBlank() && (amountValue == null || amountValue == 0.0)

                SelectAllOutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.amount)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isAmountError,
                    trailingIcon = {
                        IconButton(onClick = {
                            mainViewModel.setTransferNum(nf.getDoubleFromDollars(amount))
                            navController.navigate(ms.mattschlenkrich.paycalculator.Screen.Calculator.route)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Calculate Amount"
                            )
                        }
                    }
                )
                if (isAmountError) {
                    Text(
                        text = if (amountValue == 0.0) stringResource(R.string.amount_cannot_be_zero)
                        else stringResource(R.string.invalid_amount),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (expense != null) {
                        TextButton(
                            onClick = {
                                onDeleteExpense(expense.woHistoryExpenseId)
                                onDismissRequest()
                            },
                        ) {
                            Text(
                                text = stringResource(R.string.delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.cancel))
                    }

                    TextButton(
                        onClick = {
                            if (expense == null) {
                                onAddExpense(expenseType, supplier, invoiceNo, amount)
                            } else {
                                onUpdateExpense(
                                    expense.copy(
                                        woheType = expenseType,
                                        woheSupplier = supplier,
                                        woheInvoiceNo = invoiceNo,
                                        woheAmount = amountValue ?: 0.0
                                    )
                                )
                            }
                            onDismissRequest()
                        },
                        enabled = expenseType.isNotBlank() && amount.isNotBlank() &&
                                amountValue != null && amountValue != 0.0
                    ) {
                        Text(
                            text = if (expense == null) stringResource(R.string.label_add)
                            else stringResource(R.string.update)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}