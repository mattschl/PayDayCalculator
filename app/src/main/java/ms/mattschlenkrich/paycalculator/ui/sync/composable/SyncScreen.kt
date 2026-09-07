package ms.mattschlenkrich.paycalculator.ui.sync.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.ui.sync.ConflictDialog
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
    onClearBackups: () -> Unit,
) {
    var showRestoreConfirm by remember { mutableStateOf<String?>(null) }
    var showRepairConfirm by remember { mutableStateOf(value = false) }
    var showBackupList by remember { mutableStateOf(value = false) }
    var showAdvancedOptions by remember { mutableStateOf(value = false) }
    var isDownloadMode by remember { mutableStateOf(value = false) }

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

            if (showAdvancedOptions) {
                AlertDialog(
                    onDismissRequest = { showAdvancedOptions = false },
                    title = { Text("Advanced Options") },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    showAdvancedOptions = false
                                    isDownloadMode = false
                                    showBackupList = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) { Text("Restore from Drive") }

                            Button(
                                onClick = {
                                    showAdvancedOptions = false
                                    onManualUpload()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Upload Current State to Drive") }

                            Button(
                                onClick = {
                                    showAdvancedOptions = false
                                    showRepairConfirm = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Repair Local Database") }

                            Button(
                                onClick = {
                                    showAdvancedOptions = false
                                    onClearBackups()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) { Text("Clear All Backups from Drive") }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAdvancedOptions = false }) {
                            Text("Close")
                        }
                    }
                )
            }

            if (showRepairConfirm) {
                AlertDialog(
                    onDismissRequest = { showRepairConfirm = false },
                    title = { Text("Repair Local Database") },
                    text = { Text("This will attempt to fix metadata errors in your current local database file. Use this if you have manually replaced the database file but the app isn't recognizing it.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showRepairConfirm = false
                                onRepairLocal()
                            }
                        ) {
                            Text("Repair Now")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRepairConfirm = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showBackupList && viewModel.availableBackups.isNotEmpty()) {
                AlertDialog(
                    onDismissRequest = { showBackupList = false },
                    title = { Text("Select Backup to Restore") },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            viewModel.availableBackups.forEach { meta ->
                                TextButton(
                                    onClick = {
                                        showBackupList = false
                                        showRestoreConfirm = meta.name
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        meta.name,
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showBackupList = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            showRestoreConfirm?.let { fileName ->
                AlertDialog(
                    onDismissRequest = { showRestoreConfirm = null },
                    title = { Text("Confirm Restore") },
                    text = { Text("This will overwrite your local records with '$fileName'. This cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showRestoreConfirm = null
                            onRestore(fileName)
                        }) {
                            Text("Restore Now", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRestoreConfirm = null }) {
                            Text("Cancel")
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