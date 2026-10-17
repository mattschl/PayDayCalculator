package ms.mattschlenkrich.paycalculator.ui.material.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.compose.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.FullScreenImageDialog
import ms.mattschlenkrich.paycalculator.common.compose.PictureThumbnail
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.data.entity.MaterialCostHistory
import ms.mattschlenkrich.paycalculator.data.model.PictureItem
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialUpdateScreen(
    name: String,
    onNameChange: (String) -> Unit,
    cost: String,
    onCostChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    onCostCalcClick: () -> Unit,
    onPriceCalcClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onMergeClick: () -> Unit,
    onCancelClick: () -> Unit,
    title: String,
    pictures: List<PictureItem> = emptyList(),
    costHistoryList: List<MaterialCostHistory> = emptyList(),
    onAddCostHistoryClick: () -> Unit = {},
    onDeleteCostHistoryClick: (MaterialCostHistory) -> Unit = {}
) {
    val context = LocalContext.current
    val df = remember { DateFunctions() }
    val nf = remember { NumberFunctions() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = SCREEN_PADDING_HORIZONTAL,
                    vertical = SCREEN_PADDING_VERTICAL
                ),
            verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontStyle = FontStyle.Italic
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            SelectAllOutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.material_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DecimalOutlinedTextField(
                    value = cost,
                    onValueChange = onCostChange,
                    label = { Text(stringResource(R.string.cost)) },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = onCostCalcClick) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Calculate Cost"
                            )
                        }
                    }
                )
                DecimalOutlinedTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    label = { Text(stringResource(R.string.price)) },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = onPriceCalcClick) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Calculate Price"
                            )
                        }
                    }
                )
            }

            // Cost History Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.cost_history),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAddCostHistoryClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(stringResource(R.string.add_cost_history))
                }
            }

            if (costHistoryList.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_cost_history),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    costHistoryList.forEach { history ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = df.getDisplayDate(history.mehPurchaseDate),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${stringResource(R.string.cost)}: ${
                                            nf.displayDollars(
                                                history.mehRawCost
                                            )
                                        }",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(onClick = { onDeleteCostHistoryClick(history) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Cost Entry",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (pictures.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.pictures),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
                var downloadRefreshTrigger by remember { mutableIntStateOf(0) }

                LazyRow(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = pictures,
                        key = { _, pic: PictureItem -> pic.pictureId }
                    ) { index, pic ->
                        val displayFile = remember(pic.pictureId, downloadRefreshTrigger) {
                            val dirs = listOf(
                                File(context.cacheDir, "pictures"),
                                File(context.filesDir, "pictures")
                            )
                            val extensions = listOf("webp", "jpg", "jpeg", "png")

                            val foundFull = dirs.flatMap { dir ->
                                extensions.map { ext -> File(dir, "pic_${pic.pictureId}.$ext") }
                            }.firstOrNull { (it.exists()) && (it.length() > 0L) }

                            val foundThumb = dirs.flatMap { dir ->
                                extensions.map { ext -> File(dir, "thumb_${pic.pictureId}.$ext") }
                            }.firstOrNull { (it.exists()) && (it.length() > 0L) }

                            foundFull ?: foundThumb
                        }

                        PictureThumbnail(
                            picture = pic,
                            imageFile = displayFile,
                            isDownloading = false,
                            onClick = {
                                selectedImageIndex = index
                            },
                            onDelete = {}
                        )
                    }
                }

                selectedImageIndex?.let { index ->
                    FullScreenImageDialog(
                        pictures = pictures,
                        initialIndex = index,
                        onDelete = {},
                        onDismiss = { selectedImageIndex = null }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
            ) {
                Button(
                    onClick = onUpdateClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.update))
                }

                Button(
                    onClick = onMergeClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(stringResource(R.string.merge))
                }

                Button(
                    onClick = onCancelClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}