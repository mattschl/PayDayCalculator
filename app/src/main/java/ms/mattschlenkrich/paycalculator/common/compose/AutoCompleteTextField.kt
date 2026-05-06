package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun <T> AutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    itemToString: (T) -> String = { it.toString() },
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    // Internal state for immediate feedback
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    // Sync with external value updates
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    // Snappy filtering logic using derivedStateOf
    val filteredSuggestions by remember(textFieldValue.text, suggestions) {
        derivedStateOf {
            if (textFieldValue.text.isEmpty()) {
                emptyList()
            } else {
                suggestions.asSequence()
                    .filter { itemToString(it).contains(textFieldValue.text, ignoreCase = true) }
                    .take(50)
                    .toList()
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                if (it.text != value) {
                    onValueChange(it.text)
                }
                expanded = it.text.isNotEmpty() && filteredSuggestions.isNotEmpty()
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                .fillMaxWidth()
                .heightIn(min = StandardTextFieldDefaults.minHeight())
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { expanded = !expanded },
                    onLongClick = onLongClick
                ),
            textStyle = StandardTextFieldDefaults.textStyle(),
            interactionSource = interactionSource,
            keyboardOptions = keyboardOptions
        ) { innerTextField ->
            StandardDecorationBox(
                value = textFieldValue.text,
                innerTextField = innerTextField,
                interactionSource = interactionSource,
                label = { Text(label) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                isError = isError,
            )
        }

        if (expanded && filteredSuggestions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                filteredSuggestions.forEach { suggestion ->
                    val suggestionText = itemToString(suggestion)
                    DropdownMenuItem(
                        text = { Text(suggestionText) },
                        onClick = {
                            onItemSelected(suggestion)
                            // Selection update with cursor management
                            textFieldValue = TextFieldValue(
                                text = suggestionText,
                                selection = TextRange(suggestionText.length)
                            )
                            expanded = false
                        },
                        modifier = Modifier
                            .heightIn(min = StandardTextFieldDefaults.minHeight())
                            .fillMaxWidth(),
                        contentPadding = StandardTextFieldDefaults.dropdownItemPadding()
                    )
                }
            }
        }
    }
}