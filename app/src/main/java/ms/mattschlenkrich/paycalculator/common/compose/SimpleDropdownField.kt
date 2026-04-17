package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SimpleDropdownField(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemToString: (T) -> String = { it?.toString() ?: "" }
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        BasicTextField(
            value = if (selectedItem != null) itemToString(selectedItem) else "",
            onValueChange = {},
            readOnly = true,
            textStyle = StandardTextFieldDefaults.textStyle(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .height(StandardTextFieldDefaults.minHeight())
                .fillMaxWidth(),
            interactionSource = interactionSource
        ) { innerTextField ->
            StandardDecorationBox(
                value = if (selectedItem != null) itemToString(selectedItem) else "",
                innerTextField = innerTextField,
                interactionSource = interactionSource,
                label = if (label.isNotEmpty()) {
                    { Text(label) }
                } else null,
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemToString(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    modifier = Modifier.height(StandardTextFieldDefaults.minHeight()),
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}