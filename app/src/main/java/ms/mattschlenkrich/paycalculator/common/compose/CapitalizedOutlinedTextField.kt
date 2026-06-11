package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization

@Composable
fun CapitalizedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    isError: Boolean = false,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words,
    contentPadding: PaddingValues = StandardTextFieldDefaults.contentPadding(),
) {
    SelectAllOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        modifier = modifier,
        singleLine = singleLine,
        isError = isError,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization
        ),
        contentPadding = contentPadding
    )
}