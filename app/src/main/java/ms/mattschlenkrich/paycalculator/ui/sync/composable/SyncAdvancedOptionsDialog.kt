package ms.mattschlenkrich.paycalculator.ui.sync.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R

@Composable
fun SyncAdvancedOptionsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onRestoreFromDriveClick: () -> Unit,
    onManualUploadClick: () -> Unit,
    onRepairLocalClick: () -> Unit,
    onPurgeOrphanPicturesClick: () -> Unit,
    onClearBackupsClick: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.title_advanced_options)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRestoreFromDriveClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) { Text(stringResource(R.string.action_restore_from_drive)) }

                    Button(
                        onClick = onManualUploadClick,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.action_upload_current_state)) }

                    Button(
                        onClick = onRepairLocalClick,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.action_repair_local_database)) }

                    Button(
                        onClick = onPurgeOrphanPicturesClick,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.action_purge_orphan_pictures)) }

                    Button(
                        onClick = onClearBackupsClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text(stringResource(R.string.action_clear_all_backups)) }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }
}