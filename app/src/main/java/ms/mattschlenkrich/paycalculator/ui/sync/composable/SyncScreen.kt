package ms.mattschlenkrich.paycalculator.ui.sync.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
    onDocContentChange: (String) -> Unit,
    onQueryClick: () -> Unit,
    onSyncClick: () -> Unit,
    onReturnClick: () -> Unit,
    onChangeAccountClick: () -> Unit,
    onClearBackupsClick: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                ) {
                    SyncLogDisplay(
                        docContent = docContent,
                        onDocContentChange = onDocContentChange
                    )

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
                        onChangeAccountClick = onChangeAccountClick
                    )
                }

                if (showDeleteConfirmation) {
                    SyncDeleteConfirmationDialog(
                        onDismiss = { showDeleteConfirmation = false },
                        onConfirm = {
                            showDeleteConfirmation = false
                            onClearBackupsClick()
                        }
                    )
                }

                if (isLoading) {
                    SyncLoadingOverlay(progressMessage = progressMessage)
                }
            }
        }
    }
}