package ms.mattschlenkrich.paycalculator.ui.sync.composable

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.FullScreenImageDialog
import ms.mattschlenkrich.paycalculator.data.model.PictureItem
import ms.mattschlenkrich.paycalculator.ui.sync.DriveFileItem
import ms.mattschlenkrich.paycalculator.ui.sync.SyncViewModel
import java.io.File

enum class FileFilterCategory {
    ALL, PHOTOS, BACKUPS, ORPHANS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveCleanupScreen(
    viewModel: SyncViewModel,
    onBack: () -> Unit,
    onAuthError: (Exception) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(FileFilterCategory.ALL) }
    var fileToDelete by remember { mutableStateOf<DriveFileItem?>(null) }
    var showAutoCleanupConfirm by remember { mutableStateOf(false) }
    var selectedPictureIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadDriveFilesDetails()
    }

    val driveFiles = viewModel.driveFilesList
    val photoCount = driveFiles.count { it.isPicture }
    val backupCount = driveFiles.count { it.isBackup }
    val orphanCount = driveFiles.count { it.isOrphan }

    val filteredFiles = when (selectedFilter) {
        FileFilterCategory.ALL -> driveFiles
        FileFilterCategory.PHOTOS -> driveFiles.filter { it.isPicture }
        FileFilterCategory.BACKUPS -> driveFiles.filter { it.isBackup }
        FileFilterCategory.ORPHANS -> driveFiles.filter { it.isOrphan }
    }

    val pictureItemsInFiltered = remember(filteredFiles) {
        filteredFiles.filter { it.isPicture }.mapNotNull { item ->
            val picIdStr =
                item.name.removePrefix("pic_").removePrefix("thumb_").substringBefore(".")
            val picId = picIdStr.toLongOrNull() ?: return@mapNotNull null
            PictureItem(pictureId = picId, driveFileId = item.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title_cleanup_drive),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_go_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Action Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Drive Storage & Photo Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total Files: ${driveFiles.size} ($photoCount photos, $backupCount backups, $orphanCount orphans)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { showAutoCleanupConfirm = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.action_auto_cleanup_photos))
                        }
                    }
                }

                // Category Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == FileFilterCategory.ALL,
                        onClick = { selectedFilter = FileFilterCategory.ALL },
                        label = { Text("All (${driveFiles.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == FileFilterCategory.PHOTOS,
                        onClick = { selectedFilter = FileFilterCategory.PHOTOS },
                        label = { Text("Photos ($photoCount)") }
                    )
                    FilterChip(
                        selected = selectedFilter == FileFilterCategory.BACKUPS,
                        onClick = { selectedFilter = FileFilterCategory.BACKUPS },
                        label = { Text("Backups ($backupCount)") }
                    )
                    FilterChip(
                        selected = selectedFilter == FileFilterCategory.ORPHANS,
                        onClick = { selectedFilter = FileFilterCategory.ORPHANS },
                        label = { Text("Orphans ($orphanCount)") }
                    )
                }

                if (filteredFiles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.label_no_drive_files),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredFiles, key = { it.id }) { item ->
                            DriveFileItemRow(
                                item = item,
                                viewModel = viewModel,
                                onDelete = { fileToDelete = item },
                                onImageClick = {
                                    val idx =
                                        pictureItemsInFiltered.indexOfFirst { it.driveFileId == item.id }
                                    if (idx >= 0) {
                                        selectedPictureIndex = idx
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (viewModel.isLoading) {
                SyncLoadingOverlay(
                    progressMessage = viewModel.progressMessage ?: "Processing..."
                )
            }
        }

        // Full Screen Picture Viewer Dialog
        selectedPictureIndex?.let { index ->
            if (index >= 0 && pictureItemsInFiltered.isNotEmpty()) {
                FullScreenImageDialog(
                    pictures = pictureItemsInFiltered,
                    initialIndex = index,
                    onDelete = { },
                    onDismiss = { selectedPictureIndex = null }
                )
            }
        }

        // Delete Single File Confirmation Dialog
        fileToDelete?.let { item ->
            AlertDialog(
                onDismissRequest = { fileToDelete = null },
                title = { Text("Delete Drive File") },
                text = {
                    Text(
                        stringResource(R.string.msg_confirm_delete_drive_file, item.name)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            fileToDelete = null
                            viewModel.deleteDriveFileItem(item, onAuthError)
                        }
                    ) {
                        Text(
                            stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { fileToDelete = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Auto Cleanup Drive Photos Confirmation Dialog
        if (showAutoCleanupConfirm) {
            AlertDialog(
                onDismissRequest = { showAutoCleanupConfirm = false },
                title = { Text(stringResource(R.string.action_auto_cleanup_photos)) },
                text = {
                    Text(stringResource(R.string.msg_auto_cleanup_photos_summary))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showAutoCleanupConfirm = false
                            viewModel.autoCleanupDrivePhotos(onAuthError)
                        }
                    ) {
                        Text(
                            stringResource(R.string.action_clean_up),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAutoCleanupConfirm = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

private fun findLocalPictureFile(context: Context, fileName: String): File? {
    val dirs = listOf(
        File(context.cacheDir, "pictures"),
        File(context.filesDir, "pictures")
    )
    val picIdStr = fileName.removePrefix("pic_").removePrefix("thumb_").substringBefore(".")
    val picId = picIdStr.toLongOrNull() ?: return null
    val extensions = listOf("webp", "jpg", "jpeg", "png")

    return dirs.flatMap { dir ->
        extensions.map { ext -> File(dir, "pic_${picId}.$ext") } +
                extensions.map { ext -> File(dir, "thumb_${picId}.$ext") }
    }.firstOrNull { (it.exists()) && (it.length() > 0L) }
}

@Composable
private fun DriveFileItemRow(
    item: DriveFileItem,
    viewModel: SyncViewModel,
    onDelete: () -> Unit,
    onImageClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.isPicture) {
                var imageFile by remember(item.id, item.name) {
                    mutableStateOf(findLocalPictureFile(context, item.name))
                }

                LaunchedEffect(item.id, item.name) {
                    if (imageFile == null) {
                        imageFile = viewModel.downloadPictureToCache(item.id, item.name)
                    }
                }

                Card(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(enabled = imageFile != null) { onImageClick() },
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    if (imageFile != null) {
                        SubcomposeAsyncImage(
                            model = imageFile,
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                    } else {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else {
                val icon = when {
                    item.isBackup -> Icons.Default.Storage
                    else -> Icons.AutoMirrored.Filled.InsertDriveFile
                }

                val iconColor = when {
                    item.isOrphan -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.secondary
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${item.sizeFormatted} • ${item.modifiedTimeFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (item.isPicture && item.workOrderReference != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (item.isOrphan) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ) {
                        Text(
                            text = item.workOrderReference,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (item.isOrphan) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}