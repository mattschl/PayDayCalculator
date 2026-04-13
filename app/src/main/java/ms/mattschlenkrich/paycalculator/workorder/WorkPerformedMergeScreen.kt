package ms.mattschlenkrich.paycalculator.workorder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.AutoCompleteTextField
import ms.mattschlenkrich.paycalculator.common.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.StandardTopAppBar
import ms.mattschlenkrich.paycalculator.data.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.WorkPerformedAndChild

@Composable
fun WorkPerformedMergeScreen(
    workPerformedList: List<WorkPerformed>,
    parentDescription: String,
    onParentDescriptionChange: (String) -> Unit,
    onParentSelected: (WorkPerformed) -> Unit,
    childList: List<WorkPerformedAndChild>,
    onRemoveChild: (WorkPerformedAndChild) -> Unit,
    childDescription: String,
    onChildDescriptionChange: (String) -> Unit,
    onChildSelected: (WorkPerformed) -> Unit,
    onMergeClick: () -> Unit,
    onDoneClick: () -> Unit,
    onListItemSelected: (WorkPerformed) -> Unit
) {
    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(R.string.merge_work_performed)
            )
        }
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
                        text = stringResource(R.string.master_work_performed_description),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    AutoCompleteTextField(
                        value = parentDescription,
                        onValueChange = onParentDescriptionChange,
                        label = stringResource(R.string.parent_work_performed),
                        suggestions = workPerformedList,
                        itemToString = { it.wpDescription },
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
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 20.sp
                                )
                            )
                            childList.forEach { child ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isDeleted = child.workPerformedChild.wpIsDeleted
                                    Text(
                                        text = if (isDeleted) {
                                            child.workPerformedChild.wpDescription + " " + stringResource(
                                                R.string._deleted_
                                            )
                                        } else {
                                            child.workPerformedChild.wpDescription
                                        },
                                        color = if (isDeleted) Color.Red else Color.Black,
                                        modifier = Modifier.weight(1f)
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
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    AutoCompleteTextField(
                        value = childDescription,
                        onValueChange = onChildDescriptionChange,
                        label = stringResource(R.string.child_work_performed),
                        suggestions = workPerformedList,
                        itemToString = { it.wpDescription },
                        onItemSelected = onChildSelected,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onMergeClick,
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

            items(workPerformedList) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onListItemSelected(item) }
                        .padding(8.dp)
                ) {
                    Text(
                        text = item.wpDescription,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}