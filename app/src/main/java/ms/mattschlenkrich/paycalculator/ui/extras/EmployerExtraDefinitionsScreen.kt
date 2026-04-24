package ms.mattschlenkrich.paycalculator.ui.extras

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.ExtraDefTypeAndEmployer
import ms.mattschlenkrich.paycalculator.data.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.ui.extras.components.ExtraDefinitionItem
import ms.mattschlenkrich.paycalculator.ui.extras.components.ExtraTypeInfoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerExtraDefinitionsScreen(
    employerViewModel: EmployerViewModel,
    workExtraViewModel: WorkExtraViewModel,
    initialEmployer: Employers?,
    initialExtraType: WorkExtraTypes?,
    onAddExtraDefinition: (Employers, WorkExtraTypes) -> Unit,
    onUpdateExtraDefinition: (ExtraDefTypeAndEmployer) -> Unit,
    onUpdateExtraType: (Employers, WorkExtraTypes) -> Unit,
    onAddNewEmployer: () -> Unit,
    onAddNewExtraType: (Employers) -> Unit,
    onDeleteExtraDefinition: (ExtraDefTypeAndEmployer) -> Unit
) {
    val employers by employerViewModel.employersAll.observeAsState(emptyList())
    var selectedEmployer by remember { mutableStateOf(initialEmployer) }

    val extraTypes by if (selectedEmployer != null) {
        remember(selectedEmployer) {
            workExtraViewModel.getWorkExtraTypeList(selectedEmployer!!.employerId)
        }.observeAsState(emptyList<WorkExtraTypes>())
    } else {
        remember { mutableStateOf(emptyList<WorkExtraTypes>()) }
    }
    var selectedExtraType by remember { mutableStateOf(initialExtraType) }

    val definitions by if (selectedExtraType != null) {
        remember(selectedExtraType) {
            workExtraViewModel.getActiveExtraDefinitionsFull(
                selectedExtraType!!.wetEmployerId,
                selectedExtraType!!.workExtraTypeId
            )
        }.observeAsState(emptyList<ExtraDefTypeAndEmployer>())
    } else {
        remember { mutableStateOf(emptyList<ExtraDefTypeAndEmployer>()) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (selectedEmployer != null && selectedExtraType != null) {
                FloatingActionButton(
                    onClick = { onAddExtraDefinition(selectedEmployer!!, selectedExtraType!!) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_new_definition)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimpleDropdownField(
                    label = stringResource(R.string.employer),
                    items = employers,
                    selectedItem = selectedEmployer,
                    onItemSelected = {
                        selectedEmployer = it
                        selectedExtraType = null
                    },
                    itemToString = { it.employerName },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onAddNewEmployer) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_new_employer)
                    )
                }
            }

            if (selectedEmployer != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SimpleDropdownField(
                        label = stringResource(R.string.extra_type),
                        items = extraTypes,
                        selectedItem = selectedExtraType,
                        onItemSelected = { selectedExtraType = it },
                        itemToString = { it.wetName },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onAddNewExtraType(selectedEmployer!!) }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_new_extra_type)
                        )
                    }
                }
            }

            if (selectedExtraType != null) {
                ExtraTypeInfoCard(
                    extraType = selectedExtraType!!,
                    onClick = { onUpdateExtraType(selectedEmployer!!, selectedExtraType!!) },
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Text(
                    text = stringResource(R.string.definitions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(definitions.sortedByDescending { it.definition.weEffectiveDate }) { definition ->
                        ExtraDefinitionItem(
                            item = definition,
                            isCurrent = definition == definitions.firstOrNull(),
                            onClick = { onUpdateExtraDefinition(definition) }
                        )
                    }
                }
            }
        }
    }
}