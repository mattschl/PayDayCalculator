package ms.mattschlenkrich.paycalculator.ui.workorderhistory.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.CapitalizedOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.PictureAttachmentManager
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryExpense
import ms.mattschlenkrich.paycalculator.data.model.PictureItem
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderHistoryExpenseScreen(
    mainViewModel: MainViewModel,
    navController: NavController,
    initialExpense: WorkOrderHistoryExpense?,
    pictures: List<PictureItem>,
    onSaveExpense: (type: String, supplier: String, invoiceNo: String, amount: String) -> Unit,
    onDeleteExpense: () -> Unit,
    onCancel: () -> Unit,
    onPictureTaken: (File) -> Unit,
    onDeletePicture: (PictureItem) -> Unit,
    onDownloadPicture: (PictureItem) -> Unit,
) {
    val nf = remember { NumberFunctions() }
    var expenseType by rememberSaveable { mutableStateOf(initialExpense?.woheType ?: "") }
    var supplier by rememberSaveable { mutableStateOf(initialExpense?.woheSupplier ?: "") }
    var invoiceNo by rememberSaveable { mutableStateOf(initialExpense?.woheInvoiceNo ?: "") }
    var amount by rememberSaveable {
        mutableStateOf(
            initialExpense?.let { nf.displayNumberFromDouble(it.woheAmount) } ?: ""
        )
    }

    val amountValue = try {
        nf.getDoubleFromDollars(amount)
    } catch (_: Exception) {
        null
    }
    val isAmountError = amount.isNotBlank() && ((amountValue == null) || (amountValue == 0.0))

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (initialExpense == null) stringResource(R.string.add_expense)
                        else stringResource(R.string.update_expense),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                },
                actions = {
                    if (initialExpense != null) {
                        IconButton(onClick = onDeleteExpense) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL, vertical = SCREEN_PADDING_VERTICAL)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
        ) {
            CapitalizedOutlinedTextField(
                value = expenseType,
                onValueChange = { expenseType = it },
                label = { Text(stringResource(R.string.expense_type)) },
                modifier = Modifier.fillMaxWidth()
            )

            CapitalizedOutlinedTextField(
                value = supplier,
                onValueChange = { supplier = it },
                label = { Text(stringResource(R.string.supplier)) },
                modifier = Modifier.fillMaxWidth()
            )

            SelectAllOutlinedTextField(
                value = invoiceNo,
                onValueChange = { invoiceNo = it },
                label = { Text(stringResource(R.string.invoice_no)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING / 2)) {
                DecimalOutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.amount)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                mainViewModel.setTransferNum(nf.getDoubleFromDollars(amount))
                                navController.navigate(Screen.Calculator.route)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Calculator"
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
            }

            if (initialExpense != null) {
                PictureAttachmentManager(
                    pictures = pictures,
                    onPictureTaken = onPictureTaken,
                    onDeletePicture = onDeletePicture,
                    onDownloadPicture = onDownloadPicture,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING, Alignment.End)
            ) {
                OutlinedButton(onClick = onCancel) {
                    Text(stringResource(R.string.cancel))
                }

                if (initialExpense != null) {
                    Button(
                        onClick = onDeleteExpense,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }

                Button(
                    onClick = {
                        onSaveExpense(expenseType, supplier, invoiceNo, amount)
                    },
                    enabled = expenseType.isNotBlank() && amount.isNotBlank() &&
                            amountValue != null && amountValue != 0.0
                ) {
                    Text(
                        text = if (initialExpense == null) stringResource(R.string.label_add)
                        else stringResource(R.string.update)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}