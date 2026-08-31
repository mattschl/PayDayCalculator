package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardBottomSheet(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    if (showDialog) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING / 2)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                content()
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ConfirmationBottomSheet(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    message: String? = null,
    confirmButtonText: String = stringResource(R.string.label_yes),
    dismissButtonText: String = stringResource(R.string.label_no),
    isDelete: Boolean = false,
    onConfirm: () -> Unit,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    StandardBottomSheet(
        showDialog = showDialog,
        onDismissRequest = onDismissRequest,
        title = title
    ) {
        if (message != null) {
            Text(text = message)
        }
        content?.invoke(this)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismissRequest) {
                Text(dismissButtonText)
            }
            TextButton(
                onClick = {
                    onConfirm()
                    onDismissRequest()
                },
                colors = if (isDelete) {
                    ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.textButtonColors()
                }
            ) {
                Text(confirmButtonText)
            }
        }
    }
}