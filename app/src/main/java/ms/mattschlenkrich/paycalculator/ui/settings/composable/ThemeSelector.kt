package ms.mattschlenkrich.paycalculator.ui.settings.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ThemeSelector(
    isSystemTheme: Boolean,
    isDarkTheme: Boolean,
    onIsSystemThemeChange: (Boolean) -> Unit,
    onIsDarkThemeChange: (Boolean) -> Unit,
    itemPadding: Dp = 8.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSystemTheme,
                onClick = { onIsSystemThemeChange(!isSystemTheme) },
                role = Role.Switch
            )
            .padding(vertical = itemPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Use System Theme")
        Switch(
            checked = isSystemTheme,
            onCheckedChange = null
        )
    }

    if (!isSystemTheme) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.selectable(
                    selected = !isDarkTheme,
                    onClick = { onIsDarkThemeChange(false) },
                    role = Role.RadioButton
                )
            ) {
                RadioButton(selected = !isDarkTheme, onClick = null)
                Text("Light", modifier = Modifier.padding(start = 8.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.selectable(
                    selected = isDarkTheme,
                    onClick = { onIsDarkThemeChange(true) },
                    role = Role.RadioButton
                )
            ) {
                RadioButton(selected = isDarkTheme, onClick = null)
                Text("Dark", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}