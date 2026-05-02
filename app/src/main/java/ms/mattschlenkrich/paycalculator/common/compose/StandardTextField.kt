package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun getTextFieldDimensions() = LocalExtendedDimensions.current

object StandardTextFieldDefaults {
    @Composable
    fun minHeight() = getTextFieldDimensions().textFieldMinHeight

    @Composable
    fun borderThickness() = getTextFieldDimensions().textFieldBorderThickness

    @Composable
    fun contentPadding() = getTextFieldDimensions().textFieldContentPadding

    @Composable
    fun dropdownItemPadding() = getTextFieldDimensions().dropdownItemPadding

    @Composable
    fun textStyle() = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardDecorationBox(
    value: String,
    innerTextField: @Composable () -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    contentPadding: PaddingValues = StandardTextFieldDefaults.contentPadding(),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    Box(modifier = modifier) {
        OutlinedTextFieldDefaults.DecorationBox(
            value = value,
            innerTextField = innerTextField,
            enabled = enabled,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            contentPadding = contentPadding,
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = colors,
                    shape = OutlinedTextFieldDefaults.shape,
                    focusedBorderThickness = StandardTextFieldDefaults.borderThickness(),
                    unfocusedBorderThickness = StandardTextFieldDefaults.borderThickness()
                )
            }
        )
    }
}