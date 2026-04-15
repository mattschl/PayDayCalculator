package ms.mattschlenkrich.paycalculator.common.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R

@Composable
fun StandardTopAppBar(
    title: String,
    onBackClicked: (() -> Unit)? = null,
    onSettingsClicked: (() -> Unit)? = null,
    onMenuAction: ((String) -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClicked != null) {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back"
                    )
                }
            } else if (navigationIcon != null) {
                navigationIcon()
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            Row {
                actions()
                if (onMenuAction != null || onSettingsClicked != null) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings),
                                contentDescription = "Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (onMenuAction != null) {
                                val menuItems = listOf(
                                    R.string.view_work_order_list,
                                    R.string.view_job_spec_list,
                                    R.string.view_areas_list,
                                    R.string.view_work_performed_list,
                                    R.string.view_material_list,
                                    R.string.sync_data
                                )
                                menuItems.forEach { itemRes ->
                                    val label = stringResource(itemRes)
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            showMenu = false
                                            onMenuAction(label)
                                        }
                                    )
                                }
                            }
                            if (onSettingsClicked != null) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.settings)) },
                                    onClick = {
                                        showMenu = false
                                        onSettingsClicked()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}