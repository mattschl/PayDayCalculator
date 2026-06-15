package ms.mattschlenkrich.paycalculator.ui.sync.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R

@Composable
fun SyncActionButtons(
    isConnected: Boolean,
    isLoading: Boolean,
    onSyncClick: () -> Unit,
    onQueryClick: () -> Unit,
    onReturnClick: () -> Unit,
    onClearBackupsClick: () -> Unit,
    onChangeAccountClick: () -> Unit,
    onLegacyConnectClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onSyncClick,
                enabled = isConnected && !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(stringResource(R.string.sync))
            }
            Button(
                onClick = onQueryClick,
                enabled = isConnected && !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(stringResource(R.string.query))
            }
            Button(
                onClick = onReturnClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(stringResource(R.string.done))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onClearBackupsClick,
            enabled = isConnected && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Clear All Backups from Google Drive")
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (!isConnected) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onChangeAccountClick,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Connect (New)")
                }
                Button(
                    onClick = onLegacyConnectClick,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Connect (Legacy)")
                }
            }
        } else {
            Button(
                onClick = onChangeAccountClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Change Google Account")
            }
        }
    }
}