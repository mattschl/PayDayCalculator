package ms.mattschlenkrich.paycalculator.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

val SCREEN_PADDING_HORIZONTAL = 12.dp
val SCREEN_PADDING_VERTICAL = 4.dp
val ELEMENT_SPACING = 8.dp

@Composable
fun StandardTopAppBar(
    title: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationIcon()
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
            Row(content = actions)
        }
    }
}

@Composable
fun SelectAllOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    var textFieldValueState by remember {
        mutableStateOf(TextFieldValue(text = value))
    }

    if (textFieldValueState.text != value) {
        textFieldValueState = textFieldValueState.copy(text = value)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        if (isFocused) {
            textFieldValueState = textFieldValueState.copy(
                selection = TextRange(0, textFieldValueState.text.length)
            )
        }
    }

    OutlinedTextFieldDefaults.DecorationBox(
        value = textFieldValueState.text,
        innerTextField = {
            BasicTextField(
                value = textFieldValueState,
                onValueChange = {
                    textFieldValueState = it
                    if (value != it.text) {
                        onValueChange(it.text)
                    }
                },
                modifier = modifier,
                singleLine = singleLine,
                readOnly = readOnly,
                keyboardOptions = keyboardOptions,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                interactionSource = interactionSource
            )
        },
        enabled = true,
        singleLine = singleLine,
        visualTransformation = VisualTransformation.None,
        interactionSource = interactionSource,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        contentPadding = PaddingValues(
            start = 8.dp,
            end = 8.dp,
            top = 4.dp,
            bottom = 4.dp
        ),
        container = {
            OutlinedTextFieldDefaults.Container(
                enabled = true,
                isError = false,
                interactionSource = interactionSource,
                colors = OutlinedTextFieldDefaults.colors(),
                shape = OutlinedTextFieldDefaults.shape,
                focusedBorderThickness = 1.dp,
                unfocusedBorderThickness = 1.dp
            )
        }
    )
}

@Composable
fun DecimalOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
) {
    SelectAllOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        )
    )
}

@Composable
fun NumberOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
) {
    SelectAllOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        )
    )
}

@Composable
fun CapitalizedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
) {
    SelectAllOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words
        )
    )
}

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
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth()
                .height(28.dp),
            interactionSource = interactionSource
        ) { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = if (selectedItem != null) itemToString(selectedItem) else "",
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                label = if (label.isNotEmpty()) {
                    { Text(label) }
                } else null,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 4.dp,
                    top = 0.dp,
                    bottom = 0.dp
                ),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        shape = OutlinedTextFieldDefaults.shape,
                        focusedBorderThickness = 1.dp,
                        unfocusedBorderThickness = 1.dp
                    )
                }
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
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}