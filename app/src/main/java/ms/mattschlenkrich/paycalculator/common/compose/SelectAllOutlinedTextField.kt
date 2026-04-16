package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

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
    contentPadding: PaddingValues = StandardTextFieldDefaults.contentPadding(),
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

    StandardDecorationBox(
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
                modifier = modifier.heightIn(min = StandardTextFieldDefaults.minHeight()),
                singleLine = singleLine,
                readOnly = readOnly,
                keyboardOptions = keyboardOptions,
                textStyle = StandardTextFieldDefaults.textStyle(),
                interactionSource = interactionSource
            )
        },
        interactionSource = interactionSource,
        singleLine = singleLine,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        contentPadding = contentPadding,
    )
}