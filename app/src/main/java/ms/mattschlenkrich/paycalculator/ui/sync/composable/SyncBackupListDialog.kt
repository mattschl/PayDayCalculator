package ms.mattschlenkrich.paycalculator.ui.sync.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.ui.sync.DriveFileMeta

@Composable
fun SyncBackupListDialog(
    showDialog: Boolean,
    availableBackups: List<DriveFileMeta>,
    onDismiss: () -> Unit,
    onSelectBackup: (DriveFileMeta) -> Unit,
    onDeleteBackup: (DriveFileMeta) -> Unit
) {
    if (showDialog && availableBackups.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.title_select_backup_to_restore)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    availableBackups.forEach { meta ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = { onSelectBackup(meta) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    meta.name,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            IconButton(onClick = { onDeleteBackup(meta) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete_picture),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}