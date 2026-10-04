package ms.mattschlenkrich.paycalculator.ui.sync.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.ui.sync.ConflictDialog
import ms.mattschlenkrich.paycalculator.ui.sync.DriveFileMeta
import ms.mattschlenkrich.paycalculator.ui.sync.SyncViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    viewModel: SyncViewModel,
    onBack: () -> Unit,
    onConnect: () -> Unit,
    onConnectLegacy: () -> Unit,
    onDisconnect: () -> Unit,
    onSync: () -> Unit,
    onRestore: (String) -> Unit,
    onRepairLocal: () -> Unit,
    onManualUpload: () -> Unit,
    onDeleteBackup: (DriveFileMeta) -> Unit,
    onPurgeOrphanPictures: () -> Unit = {},
    onClearBackups: () -> Unit,
) {
    var showRestoreConfirm by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<DriveFileMeta?>(null) }
    var showRepairConfirm by remember { mutableStateOf(value = false) }
    var showPurgeOrphanConfirm by remember { mutableStateOf(value = false) }
    var showBackupList by remember { mutableStateOf(value = false) }
    var showAdvancedOptions by remember { mutableStateOf(value = false) }
    var isDownloadMode by remember { mutableStateOf(value = false) }

    LaunchedEffect(viewModel.deviceId) {
        viewModel.loadLastSyncTime()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.title_sync),
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
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.label_last_sync_on_this_device,
                        viewModel.lastSyncTimeDisplay ?: stringResource(R.string.never)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SelectAllOutlinedTextField(
                    value = viewModel.docContent,
                    onValueChange = { viewModel.docContent = it },
                    label = { Text(stringResource(R.string.label_document_content)) },
                    modifier = Modifier.weight(1f),
                    singleLine = false
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (viewModel.driveServiceHelper == null) {
                            Button(
                                onClick = onConnect,
                                modifier = Modifier.weight(1f)
                            ) { Text(stringResource(R.string.action_connect_to_drive)) }
                            Button(
                                onClick = onConnectLegacy,
                                modifier = Modifier.weight(1f)
                            ) { Text(stringResource(R.string.action_connect_to_drive_legacy)) }
                        } else {
                            Button(
                                onClick = onSync,
                                modifier = Modifier.weight(1f)
                            ) { Text(stringResource(R.string.sync)) }
                            Button(
                                onClick = {
                                    viewModel.queryDriveFiles()
                                    showAdvancedOptions = true
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Advanced") }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (viewModel.driveServiceHelper != null) {
                            Button(
                                onClick = onDisconnect,
                                modifier = Modifier.weight(1f)
                            ) { Text(stringResource(R.string.action_disconnect)) }
                        }
                        Button(
                            onClick = onBack,
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.action_done)) }
                    }
                }
            }

            SyncAdvancedOptionsDialog(
                showDialog = showAdvancedOptions,
                onDismiss = { showAdvancedOptions = false },
                onRestoreFromDriveClick = {
                    showAdvancedOptions = false
                    isDownloadMode = false
                    showBackupList = true
                },
                onManualUploadClick = {
                    showAdvancedOptions = false
                    onManualUpload()
                },
                onRepairLocalClick = {
                    showAdvancedOptions = false
                    showRepairConfirm = true
                },
                onPurgeOrphanPicturesClick = {
                    showAdvancedOptions = false
                    showPurgeOrphanConfirm = true
                },
                onClearBackupsClick = {
                    showAdvancedOptions = false
                    onClearBackups()
                }
            )

            if (showRepairConfirm) {
                AlertDialog(
                    onDismissRequest = { showRepairConfirm = false },
                    title = { Text(stringResource(R.string.title_repair_local_database)) },
                    text = { Text(stringResource(R.string.msg_repair_local_database_prompt)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showRepairConfirm = false
                                onRepairLocal()
                            }
                        ) {
                            Text(stringResource(R.string.action_repair_now))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRepairConfirm = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if (showPurgeOrphanConfirm) {
                AlertDialog(
                    onDismissRequest = { showPurgeOrphanConfirm = false },
                    title = { Text(stringResource(R.string.action_purge_orphan_pictures)) },
                    text = { Text(stringResource(R.string.msg_purge_orphan_pictures_prompt)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showPurgeOrphanConfirm = false
                                onPurgeOrphanPictures()
                            }
                        ) {
                            Text(
                                stringResource(R.string.action_clean_up),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPurgeOrphanConfirm = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            SyncBackupListDialog(
                showDialog = showBackupList,
                availableBackups = viewModel.availableBackups,
                onDismiss = { showBackupList = false },
                onSelectBackup = { meta ->
                    showBackupList = false
                    showRestoreConfirm = meta.name
                },
                onDeleteBackup = { meta ->
                    showDeleteConfirm = meta
                }
            )

            showDeleteConfirm?.let { meta ->
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = null },
                    title = { Text(stringResource(R.string.title_confirm_delete)) },
                    text = { Text(stringResource(R.string.msg_confirm_delete_backup, meta.name)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteConfirm = null
                            onDeleteBackup(meta)
                        }) {
                            Text(
                                stringResource(R.string.delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = null }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            showRestoreConfirm?.let { fileName ->
                AlertDialog(
                    onDismissRequest = { showRestoreConfirm = null },
                    title = { Text(stringResource(R.string.title_confirm_restore)) },
                    text = { Text(stringResource(R.string.msg_confirm_restore_backup, fileName)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showRestoreConfirm = null
                            onRestore(fileName)
                        }) {
                            Text(
                                stringResource(R.string.action_restore_now),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRestoreConfirm = null }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if (viewModel.progressMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = viewModel.progressMessage ?: "",
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            viewModel.showConflictDialog?.let { info ->
                ConflictDialog(
                    info = info,
                    onChoice = { choice, applyToAll ->
                        viewModel.onConflictChoice(choice, applyToAll)
                    }
                )
            }

            if (viewModel.errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.errorMessage = null },
                    title = { Text("Error") },
                    text = { Text(viewModel.errorMessage!!) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.errorMessage = null }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}