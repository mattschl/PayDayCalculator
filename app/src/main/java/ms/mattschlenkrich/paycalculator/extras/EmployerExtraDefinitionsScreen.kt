package ms.mattschlenkrich.paycalculator.extras

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.ExtraAppliesToFrequencies
import ms.mattschlenkrich.paycalculator.common.ExtraAttachToFrequencies
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.SimpleDropdownField
import ms.mattschlenkrich.paycalculator.common.StandardTopAppBar
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.ExtraDefTypeAndEmployer
import ms.mattschlenkrich.paycalculator.data.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel

@Composable
fun EmployerExtraDefinitionsScreen(
    employerViewModel: EmployerViewModel,
    workExtraViewModel: WorkExtraViewModel,
    initialEmployer: Employers? = null,
    initialExtraType: WorkExtraTypes? = null,
    onAddExtraDefinition: (Employers, WorkExtraTypes) -> Unit,
    onUpdateExtraDefinition: (ExtraDefTypeAndEmployer) -> Unit,
    onUpdateExtraType: (Employers, WorkExtraTypes) -> Unit,
    onAddNewEmployer: () -> Unit,
    onAddNewExtraType: (Employers) -> Unit,
    onDeleteExtraDefinition: (ExtraDefTypeAndEmployer) -> Unit,
    onCancel: () -> Unit
) {
    val employers by employerViewModel.getEmployers().observeAsState(emptyList())
    var selectedEmployer by remember { mutableStateOf(initialEmployer) }

    LaunchedEffect(employers) {
        if (selectedEmployer == null && employers.isNotEmpty()) {
            selectedEmployer = employers.first()
        }
    }

    val extraTypes by selectedEmployer?.let {
        workExtraViewModel.getExtraDefTypes(it.employerId).observeAsState(emptyList())
    } ?: remember { mutableStateOf(emptyList<WorkExtraTypes>()) }

    var selectedExtraType by remember { mutableStateOf(initialExtraType) }

    LaunchedEffect(extraTypes) {
        if (selectedExtraType == null && extraTypes.isNotEmpty()) {
            selectedExtraType = extraTypes.first()
        } else if (selectedExtraType != null && !extraTypes.contains(selectedExtraType)) {
            selectedExtraType = extraTypes.firstOrNull()
        }
    }

    val extraDefinitions by (selectedEmployer?.let { employer ->
        selectedExtraType?.let { extraType ->
            workExtraViewModel.getActiveExtraDefinitionsFull(
                employer.employerId,
                extraType.workExtraTypeId
            ).observeAsState(emptyList())
        }
    } ?: remember { mutableStateOf(emptyList<ExtraDefTypeAndEmployer>()) })

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(R.string.pay_extras),
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedEmployer != null && selectedExtraType != null) {
                FloatingActionButton(
                    onClick = { onAddExtraDefinition(selectedEmployer!!, selectedExtraType!!) },
                    containerColor = Color(0xFF006400), // dark_green
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_new))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = SCREEN_PADDING_HORIZONTAL)
        ) {
            SimpleDropdownField(
                label = stringResource(R.string.employer),
                items = employers + null, // null represents "Add new employer"
                selectedItem = selectedEmployer,
                onItemSelected = {
                    if (it == null) {
                        onAddNewEmployer()
                    } else {
                        selectedEmployer = it
                    }
                },
                itemToString = { it?.employerName ?: "Add new employer" },
                modifier = Modifier.fillMaxWidth()
            )

            SimpleDropdownField(
                label = stringResource(R.string.extra_type),
                items = extraTypes + null, // null represents "Add a new extra type"
                selectedItem = selectedExtraType,
                onItemSelected = {
                    if (it == null) {
                        selectedEmployer?.let { employer ->
                            onAddNewExtraType(employer)
                        }
                    } else {
                        selectedExtraType = it
                    }
                },
                itemToString = { it?.wetName ?: "Add a new extra type" },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ELEMENT_SPACING)
            )

            selectedExtraType?.let { extraType ->
                ExtraTypeInfoCard(
                    extraType = extraType,
                    onClick = {
                        selectedEmployer?.let { employer ->
                            onUpdateExtraType(employer, extraType)
                        }
                    },
                    modifier = Modifier.padding(top = ELEMENT_SPACING)
                )
            }

            Text(
                text = stringResource(R.string.rates),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = ELEMENT_SPACING),
                textAlign = TextAlign.Center
            )

            if (extraDefinitions.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = stringResource(R.string.no_values_have_been_entered_add_them_now),
                        modifier = Modifier
                            .padding(50.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING),
                    horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                ) {
                    items(extraDefinitions) { definition ->
                        ExtraDefinitionItem(
                            definition = definition,
                            isCurrent = extraDefinitions.indexOf(definition) == 0,
                            onClick = { onUpdateExtraDefinition(definition) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExtraTypeInfoCard(
    extraType: WorkExtraTypes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (extraType.wetIsDeleted) {
                Text(
                    text = stringResource(R.string.deleted),
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.calculated) + " " +
                                (ExtraAppliesToFrequencies.entries.getOrNull(extraType.wetAppliesTo)?.frequency
                                    ?: ""),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.attaches_to) + " " +
                                (ExtraAttachToFrequencies.entries.getOrNull(extraType.wetAttachTo)?.frequency
                                    ?: ""),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.this_is_a) + " " +
                                if (extraType.wetIsCredit) stringResource(R.string.credit) else stringResource(
                                    R.string.deduction
                                ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.applied) + " " +
                                if (extraType.wetIsDefault) stringResource(R.string.by_default) else stringResource(
                                    R.string.manually
                                ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ExtraDefinitionItem(
    definition: ExtraDefTypeAndEmployer,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val nf = remember { NumberFunctions() }
    val deletedLabel = stringResource(R.string._deleted_)
    val currentLabel = stringResource(R.string.__current)
    val addLabel = stringResource(R.string.add)
    val deductLabel = stringResource(R.string.deduct)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val effectiveDateText = remember(
                definition.definition.weEffectiveDate,
                definition.definition.weIsDeleted,
                isCurrent
            ) {
                var text = definition.definition.weEffectiveDate ?: ""
                if (definition.definition.weIsDeleted) {
                    text = "* $text $deletedLabel"
                } else if (isCurrent) {
                    text += " $currentLabel"
                }
                text
            }

            Text(
                text = effectiveDateText,
                style = MaterialTheme.typography.bodySmall,
                color = if (definition.definition.weIsDeleted) Color.Red else Color.Unspecified,
                textAlign = TextAlign.Center
            )

            val valueText = remember(
                definition.extraType.wetIsCredit,
                definition.definition.weIsFixed,
                definition.definition.weValue
            ) {
                val action = if (definition.extraType.wetIsCredit) addLabel else deductLabel
                val value = if (definition.definition.weIsFixed) {
                    nf.displayDollars(definition.definition.weValue)
                } else {
                    nf.getPercentStringFromDouble(definition.definition.weValue / 100)
                }
                "$action $value"
            }

            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (definition.extraType.wetIsCredit) Color.Black else Color.Red,
                textAlign = TextAlign.Center
            )

            if (definition.extraType.wetIsDefault) {
                Text(
                    text = stringResource(R.string._default),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}