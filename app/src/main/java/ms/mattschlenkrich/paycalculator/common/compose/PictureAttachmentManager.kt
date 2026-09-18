package ms.mattschlenkrich.paycalculator.common.compose

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.SubcomposeAsyncImage
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderPictures
import java.io.File

@Composable
fun PictureAttachmentManager(
    pictures: List<WorkOrderPictures>,
    onPictureTaken: (File) -> Unit,
    onDeletePicture: (WorkOrderPictures) -> Unit,
    onDownloadPicture: (WorkOrderPictures) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showFullImage by remember { mutableStateOf<WorkOrderPictures?>(null) }
    var pictureToDelete by remember { mutableStateOf<WorkOrderPictures?>(null) }
    var showSelectionDialog by remember { mutableStateOf(value = false) }
    var pendingFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingFile?.let { onPictureTaken(it) }
        } else {
            pendingFile?.delete()
        }
        pendingFile = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val timeStamp = System.currentTimeMillis()
            val storageDir = File(context.cacheDir, "pictures")
            if (!storageDir.exists()) storageDir.mkdirs()
            val file = File(storageDir, "IMG_$timeStamp.webp")

            try {
                context.contentResolver.openInputStream(it)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                onPictureTaken(file)
            } catch (e: Exception) {
                Log.e("PictureAttachmentManager", "Failed to copy gallery image", e)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.pictures),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showSelectionDialog = true }) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = stringResource(R.string.take_picture)
                )
            }
        }

        if (pictures.isEmpty()) {
            Text(
                text = stringResource(R.string.no_pictures),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pictures, key = { it.pictureId }) { pic ->
                    PictureThumbnail(
                        picture = pic,
                        onClick = {
                            if (pic.localCachePath != null) showFullImage = pic
                            else onDownloadPicture(pic)
                        }
                    ) { pictureToDelete = pic }
                }
            }
        }
    }

    if (showSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showSelectionDialog = false },
            title = { Text(stringResource(R.string.camera_or_gallery)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showSelectionDialog = false
                            val timeStamp = System.currentTimeMillis()
                            val storageDir = File(context.cacheDir, "pictures")
                            if (!storageDir.exists()) storageDir.mkdirs()
                            val file = File(storageDir, "IMG_$timeStamp.webp")
                            val uri = FileProvider.getUriForFile(
                                context,
                                "ms.mattschlenkrich.paycalculator.fileprovider",
                                file
                            )
                            pendingFile = file
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 8.dp))
                            Text(stringResource(R.string.take_picture))
                        }
                    }
                    TextButton(
                        onClick = {
                            showSelectionDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 8.dp))
                            Text(stringResource(R.string.select_from_gallery))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSelectionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    showFullImage?.let { pic ->
        FullScreenImageDialog(
            picture = pic,
            onDismiss = { showFullImage = null }
        )
    }

    pictureToDelete?.let { pic ->
        AlertDialog(
            onDismissRequest = { pictureToDelete = null },
            title = { Text(stringResource(R.string.delete_picture)) },
            text = { Text(stringResource(R.string.confirm_delete_picture)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePicture(pic)
                        pictureToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pictureToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun PictureThumbnail(
    picture: WorkOrderPictures,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (picture.localCachePath != null) {
                SubcomposeAsyncImage(
                    model = picture.localCachePath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    },
                    error = {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Text(text = "Download", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.White.copy(alpha = 0.5f))
                    .size(24.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FullScreenImageDialog(
    picture: WorkOrderPictures,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = picture.localCachePath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                loading = {
                    CircularProgressIndicator()
                }
            )
        }
    }
}