package ms.mattschlenkrich.paycalculator.ui.sync.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun SyncLogDisplay(
    docContent: String,
    onDocContentChange: (String) -> Unit
) {
    BasicTextField(
        value = docContent,
        onValueChange = onDocContentChange,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(.75f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        readOnly = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
    )
}