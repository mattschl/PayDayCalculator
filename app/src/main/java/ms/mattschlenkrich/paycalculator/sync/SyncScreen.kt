package ms.mattschlenkrich.paycalculator.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ms.mattschlenkrich.paycalculator.R

@Composable
fun SyncScreen(
    docContent: String,
    isLoading: Boolean,
    progressMessage: String,
    syncProgress: Int,
    syncMax: Int,
    errorMessage: String?,
    onDocContentChange: (String) -> Unit,
    onQueryClick: () -> Unit,
    onSyncClick: () -> Unit,
    onReturnClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = docContent,
                    onValueChange = onDocContentChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    readOnly = true,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
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
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        color = Color(0xFFFFF0F0)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(4.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = errorMessage,
                                color = Color(0xFFB00020),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onSyncClick,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(stringResource(R.string.sync))
                        }
                        Button(
                            onClick = onQueryClick,
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
                            Text(stringResource(R.string.return_text))
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = progressMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}