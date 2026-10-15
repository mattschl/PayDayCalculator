package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.data.model.PictureItem
import java.io.File

//@Composable
//fun FullScreenImageDialog(
//    picture: PictureItem,
//    onDelete: () -> Unit,
//    onDismiss: () -> Unit
//) {
//    FullScreenImageDialog(
//        pictures = listOf(picture),
//        initialIndex = 0,
//        onDelete = { onDelete() },
//        onDismiss = onDismiss
//    )
//}

@Composable
fun FullScreenImageDialog(
    pictures: List<PictureItem>,
    initialIndex: Int,
    onDelete: (PictureItem) -> Unit,
    onDismiss: () -> Unit,
    onDownloadPicture: (suspend (PictureItem) -> Unit)? = null
) {
    if (pictures.isEmpty()) return

    val context = LocalContext.current
    val startIndex = initialIndex.coerceIn(0, pictures.size - 1)
    val pagerState = rememberPagerState(initialPage = startIndex) { pictures.size }
    var downloadRefreshTrigger by remember { mutableIntStateOf(0) }

    val currentPic = pictures.getOrNull(pagerState.currentPage) ?: pictures[startIndex]

    // Auto-download image if active pager item is on Drive and not available locally
    LaunchedEffect(pagerState.currentPage, downloadRefreshTrigger) {
        val pic = pictures.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        val dirs = listOf(
            File(context.cacheDir, "pictures"),
            File(context.filesDir, "pictures")
        )
        val fullFile = dirs.map { File(it, "pic_${pic.pictureId}.webp") }
            .firstOrNull { (it.exists()) && (it.length() > 0L) }

        if (fullFile == null && !pic.driveFileId.isNullOrBlank() && onDownloadPicture != null) {
            try {
                onDownloadPicture(pic)
                downloadRefreshTrigger++
            } catch (_: Exception) {
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pic = pictures[page]
                val imageFile = remember(pic.pictureId, downloadRefreshTrigger) {
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

                    foundFull ?: foundThumb ?: File(
                        context.cacheDir,
                        "pictures/pic_${pic.pictureId}.webp"
                    )
                }

                ZoomableImage(
                    model = imageFile,
                    contentDescription = null,
                    onDismiss = onDismiss
                )
            }

            // Top Bar with Page Counter & Delete Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${pictures.size}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                IconButton(
                    onClick = { onDelete(currentPic) },
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_picture),
                        tint = Color.Red
                    )
                }
            }
        }
    }
}