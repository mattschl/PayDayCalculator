package ms.mattschlenkrich.paycalculator.ui.extras.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import androidx.lifecycle.MutableLiveData
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.common.compose.calculateGridColumns
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.model.ExtraDefTypeAndEmployer
import ms.mattschlenkrich.paycalculator.data.viewmodel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerExtraDefinitionsScreen(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workExtraViewModel: WorkExtraViewModel,
    onAddExtraDefinition: (Employers, WorkExtraTypes) -> Unit,
    onUpdateExtraDefinition: (ExtraDefTypeAndEmployer) -> Unit,
    onUpdateExtraType: (Employers, WorkExtraTypes) -> Unit,
    onAddNewEmployer: () -> Unit,
    onAddNewExtraType: (Employers) -> Unit,
    onDeleteExtraDefinition: (ExtraDefTypeAndEmployer) -> Unit
) {
    val employers by employerViewModel.employersAll.observeAsState(emptyList())
    var selectedEmployer by remember { mutableStateOf(mainViewModel.getEmployer()) }

    val extraTypesState = if (selectedEmployer != null) {
        remember(selectedEmployer) {
            workExtraViewModel.getWorkExtraTypeList(selectedEmployer!!.employerId)
        }
    } else {
        null
    }
    val extraTypes by (extraTypesState ?: remember {
        MutableLiveData(emptyList<WorkExtraTypes>())
    }).observeAsState(emptyList())

    var selectedExtraType by remember { mutableStateOf(mainViewModel.getWorkExtraType()) }

    val definitionsState = if (selectedExtraType != null) {
        remember(selectedExtraType) {
            workExtraViewModel.getActiveExtraDefinitionsFull(
                selectedExtraType!!.wetEmployerId,
                selectedExtraType!!.workExtraTypeId
            )
        }
    } else {
        null
    }
    val definitions by (definitionsState ?: remember {
        MutableLiveData(emptyList<ExtraDefTypeAndEmployer>())
    }).observeAsState(emptyList())

    val sortedDefinitions = remember(definitions) {
        definitions.sortedByDescending { it.definition.weEffectiveDate }
    }

    val columns = calculateGridColumns()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (selectedEmployer != null && selectedExtraType != null) {
                FloatingActionButton(
                    onClick = {
                        mainViewModel.setEmployer(selectedEmployer)
                        mainViewModel.setWorkExtraType(selectedExtraType)
                        onAddExtraDefinition(selectedEmployer!!, selectedExtraType!!)
                    },
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
                        mainViewModel.setEmployer(it)
                        selectedExtraType = null
                        mainViewModel.setWorkExtraType(null)
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
                        onItemSelected = {
                            selectedExtraType = it
                            mainViewModel.setWorkExtraType(it)
                        },
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
                    onClick = {
                        mainViewModel.setEmployer(selectedEmployer)
                        mainViewModel.setWorkExtraType(selectedExtraType)
                        onUpdateExtraType(selectedEmployer!!, selectedExtraType!!)
                    },
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Text(
                    text = stringResource(R.string.definitions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (sortedDefinitions.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_info_to_view),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING),
                    verticalItemSpacing = ELEMENT_SPACING,
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        sortedDefinitions,
//                        key = { it.definition.workExtraDefId }
                    ) { definition ->
                        ExtraDefinitionItem(
                            item = definition,
                            isCurrent = definition == sortedDefinitions.firstOrNull(),
                            onClick = { onUpdateExtraDefinition(definition) }
                        )
                    }
                }
            }
        }
    }
}