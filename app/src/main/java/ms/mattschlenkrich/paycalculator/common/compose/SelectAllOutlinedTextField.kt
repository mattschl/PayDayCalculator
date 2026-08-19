package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun SelectAllOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
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

    val combinedModifier = modifier.then(
        if (onLongClick != null) {
            Modifier.pointerInput(onLongClick) {
                detectTapGestures(
                    onLongPress = { onLongClick.invoke() }
                )
            }
        } else {
            Modifier
        }
    )

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
                modifier = (if (focusRequester != null)
                    Modifier.focusRequester(focusRequester)
                else
                    Modifier)
                    .fillMaxWidth()
                    .heightIn(min = StandardTextFieldDefaults.minHeight()),
                singleLine = singleLine,
                readOnly = readOnly,
                keyboardOptions = keyboardOptions,
                textStyle = StandardTextFieldDefaults.textStyle(),
                interactionSource = interactionSource
            )
        },
        interactionSource = interactionSource,
        modifier = combinedModifier,
        singleLine = singleLine,
        isError = isError,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        contentPadding = contentPadding,
    )
}