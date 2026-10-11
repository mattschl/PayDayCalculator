package ms.mattschlenkrich.paycalculator.ui.sync.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.ui.sync.SyncWarningInfo
import ms.mattschlenkrich.paycalculator.ui.sync.SyncWarningType

@Composable
fun SyncWarningModalDialog(
    warningInfo: SyncWarningInfo,
    onSyncNow: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = when (warningInfo.type) {
        SyncWarningType.NEW_INSTALL_OR_BLANK -> stringResource(R.string.title_new_install_sync_warning)
        SyncWarningType.STALE_SYNC -> stringResource(R.string.title_stale_sync_warning)
    }

    val message = when (warningInfo.type) {
        SyncWarningType.NEW_INSTALL_OR_BLANK -> stringResource(R.string.msg_new_install_sync_warning)
        SyncWarningType.STALE_SYNC -> stringResource(
            R.string.msg_stale_sync_warning,
            warningInfo.daysSinceLastSync,
            warningInfo.thresholdDays
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            Button(onClick = {
                onDismiss()
                onSyncNow()
            }) {
                Text(text = stringResource(R.string.sync_now))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_ignore))
            }
        }
    )
}