package ms.mattschlenkrich.paycalculator.ui.sync.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.common.compose.ConfirmationBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    docContent: String,
    isLoading: Boolean,
    isConnected: Boolean,
    progressMessage: String,
    syncProgress: Int,
    syncMax: Int,
    errorMessage: String?,
    onQueryClick: () -> Unit,
    onSyncClick: () -> Unit,
    onReturnClick: () -> Unit,
    onChangeAccountClick: () -> Unit,
    onClearBackupsClick: () -> Unit,
    onLegacyConnectClick: () -> Unit = {}
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.safeDrawingPadding()
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(modifier = Modifier.height(300.dp)) {
                        SyncLogDisplay(
                            docContent = docContent
                        )
                    }

                    if (syncMax > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { syncProgress.toFloat() / syncMax.toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SyncErrorDisplay(errorMessage = errorMessage)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SyncActionButtons(
                        isConnected = isConnected,
                        isLoading = isLoading,
                        onSyncClick = onSyncClick,
                        onQueryClick = onQueryClick,
                        onReturnClick = onReturnClick,
                        onClearBackupsClick = { showDeleteConfirmation = true },
                        onChangeAccountClick = onChangeAccountClick,
                        onLegacyConnectClick = onLegacyConnectClick
                    )

                    // Extra padding at the bottom to ensure buttons are clear of the nav bar
                    Spacer(modifier = Modifier.height(16.dp))
                }

                ConfirmationBottomSheet(
                    showDialog = showDeleteConfirmation,
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = "Confirm Deletion",
                    message = "Are you sure you want to delete all backup files from Google Drive? This action cannot be undone.",
                    confirmButtonText = "Delete",
                    dismissButtonText = "Cancel",
                    isDelete = true,
                    onConfirm = { onClearBackupsClick() }
                )

                if (isLoading) {
                    SyncLoadingOverlay(progressMessage = progressMessage)
                }
            }
        }
    }
}