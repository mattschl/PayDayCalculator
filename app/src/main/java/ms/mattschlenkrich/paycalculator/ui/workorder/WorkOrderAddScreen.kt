package ms.mattschlenkrich.paycalculator.ui.workorder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.CapitalizedOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.data.Employers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderAddScreen(
    employers: List<Employers>,
    selectedEmployer: Employers?,
    onEmployerSelected: (Employers) -> Unit,
    fixedEmployerName: String?,
    woNumber: String,
    onWoNumberChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onDoneClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WorkOrderAddScreen") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onDoneClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Done, contentDescription = stringResource(R.string.done))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL),
            verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING),
            contentPadding = PaddingValues(
                vertical = SCREEN_PADDING_VERTICAL
            )
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                    ) {
                        // Employer selection
                        if (fixedEmployerName != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.employer) + ": ",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = fixedEmployerName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            SimpleDropdownField(
                                label = stringResource(R.string.employer),
                                items = employers,
                                selectedItem = selectedEmployer,
                                onItemSelected = onEmployerSelected,
                                itemToString = { it.employerName }
                            )
                        }

                        SelectAllOutlinedTextField(
                            value = woNumber,
                            onValueChange = onWoNumberChange,
                            label = { Text(stringResource(R.string.work_order_number)) },
                            placeholder = { Text(stringResource(R.string.text_zeros)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        CapitalizedOutlinedTextField(
                            value = address,
                            onValueChange = onAddressChange,
                            label = { Text(stringResource(R.string.address)) },
                            placeholder = { Text(stringResource(R.string.enter_address)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.general_job_description),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        CapitalizedOutlinedTextField(
                            value = description,
                            onValueChange = onDescriptionChange,
                            placeholder = { Text(stringResource(R.string.enter_general_job_description)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            singleLine = false
                        )
                    }
                }
            }
        }
    }
}