package ms.mattschlenkrich.paycalculator.ui.jobspec

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.AutoCompleteTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.data.JobSpec
import ms.mattschlenkrich.paycalculator.data.JobSpecAndChild

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSpecMergeScreen(
    jobSpecList: List<JobSpec>,
    parentName: String,
    onParentNameChange: (String) -> Unit,
    onParentSelected: (JobSpec) -> Unit,
    childList: List<JobSpecAndChild>,
    onRemoveChild: (JobSpecAndChild) -> Unit,
    childName: String,
    onChildNameChange: (String) -> Unit,
    onChildSelected: (JobSpec) -> Unit,
    onMergeAction: (Int) -> Unit,
    onDoneClick: () -> Unit,
    onListItemSelected: (JobSpec) -> Unit
) {
    var showMergeOptionsDialog by remember {
        mutableStateOf(
            false
        )
    }

    if (showMergeOptionsDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showMergeOptionsDialog = false },
            title = { Text(stringResource(R.string.choose_merge_option)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                ) {
                    Button(
                        onClick = {
                            onMergeAction(1) // Keep
                            showMergeOptionsDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.merge_keep_description))
                    }
                    Button(
                        onClick = {
                            onMergeAction(2) // Replace
                            showMergeOptionsDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.merge_replace_all))
                    }
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showMergeOptionsDialog = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        /* topBar = {
             TopAppBar(
                 title = { Text("JobSpecMergeScreen") },
                 navigationIcon = {
                     IconButton(onClick = onDoneClick) {
                         Icon(
                             imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                             contentDescription = stringResource(R.string.back)
                         )
                     }
                 }
             )
         },*/
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = SCREEN_PADDING_HORIZONTAL,
                    vertical = SCREEN_PADDING_VERTICAL
                ),
            horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING),
            verticalItemSpacing = ELEMENT_SPACING
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                ) {
                    Text(
                        text = stringResource(R.string.master_job_spec),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    AutoCompleteTextField(
                        value = parentName,
                        onValueChange = onParentNameChange,
                        label = stringResource(R.string.parent_job_spec),
                        suggestions = jobSpecList,
                        itemToString = { it.jsName },
                        onItemSelected = onParentSelected,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (childList.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.existing_children),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontStyle = FontStyle.Italic
                                )
                            )
                            childList.forEach { child ->
                                JobSpecChildItem(child, onRemoveChild)
                            }
                        }
                    }
                }
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
                ) {
                    Text(
                        text = stringResource(R.string.choose_the_other_description_to_merge),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    AutoCompleteTextField(
                        value = childName,
                        onValueChange = onChildNameChange,
                        label = stringResource(R.string.child_job_spec),
                        suggestions = jobSpecList,
                        itemToString = { it.jsName },
                        onItemSelected = onChildSelected,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showMergeOptionsDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)) // Dark Green
                        ) {
                            Text(stringResource(R.string.merge))
                        }
                        Button(
                            onClick = onDoneClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)) // Deep Red
                        ) {
                            Text(stringResource(R.string.done))
                        }
                    }
                }
            }

            items(jobSpecList) { item ->
                JobSpecSelectionItem(item, onListItemSelected)
            }
        }
    }
}

@Composable
fun JobSpecChildItem(child: JobSpecAndChild, onRemoveChild: (JobSpecAndChild) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isDeleted = child.jobSpecChild.jsIsDeleted
        Text(
            text = if (isDeleted) {
                child.jobSpecChild.jsName + " " + stringResource(R.string._deleted_)
            } else {
                child.jobSpecChild.jsName
            },
            color = if (isDeleted) Color.Red else Color.Black,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(onClick = { onRemoveChild(child) }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete),
                tint = Color.Red
            )
        }
    }
}

@Composable
fun JobSpecSelectionItem(item: JobSpec, onListItemSelected: (JobSpec) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onListItemSelected(item) }
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = item.jsName,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        HorizontalDivider()
    }
}