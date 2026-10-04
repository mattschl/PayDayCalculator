package ms.mattschlenkrich.paycalculator.common.compose

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.model.PictureItem
import java.io.File

@Composable
fun PictureAttachmentManager(
    pictures: List<PictureItem>,
    onPictureTaken: (File) -> Unit,
    onDeletePicture: (PictureItem) -> Unit,
    onDownloadPicture: (PictureItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var downloadingPictureIds by remember { mutableStateOf(setOf<Long>()) }
    var downloadRefreshTrigger by remember { mutableIntStateOf(0) }
    var showFullImage by remember { mutableStateOf<PictureItem?>(null) }
    var pictureToDelete by remember { mutableStateOf<PictureItem?>(null) }
    var showSelectionDialog by remember { mutableStateOf(value = false) }
    var pendingFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
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
            val pictureId = NumberFunctions().generateRandomIdAsLong()
            val storageDir = File(context.cacheDir, "pictures").apply { if (!exists()) mkdirs() }
            val file = File(storageDir, "pic_$pictureId.webp")

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
                    val displayFile = remember(pic.pictureId, downloadRefreshTrigger) {
                        val thumbFile =
                            File(context.cacheDir, "pictures/thumb_${pic.pictureId}.webp")
                        val fullFile = File(context.cacheDir, "pictures/pic_${pic.pictureId}.webp")
                        when {
                            (thumbFile.exists()) && (thumbFile.length() > 0L) -> thumbFile
                            (fullFile.exists()) && (fullFile.length() > 0L) -> fullFile
                            else -> null
                        }
                    }
                    val hasLocalFile = displayFile != null
                    val isDownloading = downloadingPictureIds.contains(pic.pictureId)

                    PictureThumbnail(
                        picture = pic,
                        imageFile = displayFile,
                        isDownloading = isDownloading,
                        onClick = {
                            if (hasLocalFile) {
                                showFullImage = pic
                            } else if (!pic.driveFileId.isNullOrBlank()) {
                                if (!isDownloading) {
                                    downloadingPictureIds = downloadingPictureIds + pic.pictureId
                                    coroutineScope.launch {
                                        try {
                                            onDownloadPicture(pic)
                                        } finally {
                                            downloadingPictureIds =
                                                downloadingPictureIds - pic.pictureId
                                            downloadRefreshTrigger++
                                        }
                                        val downloadedFile = File(
                                            context.cacheDir,
                                            "pictures/pic_${pic.pictureId}.webp"
                                        )
                                        if ((downloadedFile.exists()) && (downloadedFile.length() > 0L)) {
                                            showFullImage = pic
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    R.string.msg_picture_link_broken,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
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
            text = { Text(stringResource(R.string.camera_or_gallery)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSelectionDialog = false
                        val pictureId = NumberFunctions().generateRandomIdAsLong()
                        val storageDir =
                            File(context.cacheDir, "pictures").apply { if (!exists()) mkdirs() }
                        val file = File(storageDir, "pic_$pictureId.webp")
                        pendingFile = file
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        cameraLauncher.launch(uri)
                    }
                ) {
                    Text(stringResource(R.string.take_picture))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSelectionDialog = false
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Text(stringResource(R.string.pictures))
                }
            }
        )
    }

    showFullImage?.let { pic ->
        FullScreenImageDialog(
            picture = pic,
            onDelete = {
                showFullImage = null
                pictureToDelete = pic
            },
            onDismiss = { showFullImage = null }
        )
    }

    pictureToDelete?.let { pic ->
        AlertDialog(
            onDismissRequest = { pictureToDelete = null },
            title = { Text(stringResource(R.string.delete_picture)) },
            text = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePicture(pic)
                        pictureToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
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