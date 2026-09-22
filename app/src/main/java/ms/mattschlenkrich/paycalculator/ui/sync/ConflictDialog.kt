package ms.mattschlenkrich.paycalculator.ui.sync

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R

@Composable
fun ConflictDialog(
    info: ConflictInfo,
    onChoice: (ConflictChoice, Boolean) -> Unit
) {
    var applyToAll by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { /* Not dismissible */ },
        title = { Text(stringResource(R.string.title_sync_conflict)) },
        text = {
            Column {
                Text(
                    stringResource(
                        info.messageRes ?: R.string.msg_sync_conflict,
                        info.tableName,
                        info.localName,
                        info.localId,
                        info.localTime,
                        info.driveId,
                        info.driveTime
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { applyToAll = !applyToAll }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = applyToAll,
                        onCheckedChange = { applyToAll = it }
                    )
                    Text(
                        text = stringResource(R.string.action_apply_to_all_conflicts),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onChoice(
                    ConflictChoice.KEEP_LOCAL,
                    applyToAll
                )
            }) {
                Text(stringResource(R.string.action_keep_local))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onChoice(
                    ConflictChoice.KEEP_DRIVE,
                    applyToAll
                )
            }) {
                Text(stringResource(R.string.action_keep_drive))
            }
        }
    )
}