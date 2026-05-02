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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

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
    val filteredSuggestions = remember { mutableStateListOf<T>() }

    LaunchedEffect(value, suggestions) {
        snapshotFlow { value }
            .debounce(100)
            .collectLatest { query ->
                val result = suggestions.asSequence()
                    .filter { itemToString(it).contains(query, ignoreCase = true) }
                    .take(50)
                    .toList()
                filteredSuggestions.clear()
                filteredSuggestions.addAll(result)
            }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        BasicTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = it.isNotEmpty()
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
                value = value,
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
                    DropdownMenuItem(
                        text = { Text(itemToString(suggestion)) },
                        onClick = {
                            onItemSelected(suggestion)
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