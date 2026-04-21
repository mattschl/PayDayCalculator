package ms.mattschlenkrich.paycalculator.ui.tax

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.TaxBasedOn
import ms.mattschlenkrich.paycalculator.common.compose.CapitalizedOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SimpleDropdownField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxTypeScreen(
    taxType: String,
    onTaxTypeChange: (String) -> Unit,
    selectedBasedOn: Int,
    onBasedOnChange: (Int) -> Unit,
    title: String,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        /* topBar = {
             TopAppBar(
                 title = {
                     Text(
                         text = "TaxTypeScreen", // title,
                         style = MaterialTheme.typography.titleLarge,
                     )
                 },
                 navigationIcon = {
                     IconButton(onClick = onBackClick) {
                         Icon(
                             imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                             contentDescription = stringResource(R.string.go_back)
                         )
                     }
                 },
                 actions = {
                     if (onDeleteClick != null) {
                         IconButton(onClick = onDeleteClick) {
                             Icon(
                                 imageVector = Icons.Default.Delete,
                                 contentDescription = stringResource(R.string.delete)
                             )
                         }
                     }
                 }
             )
         },*/
        floatingActionButton = {
            FloatingActionButton(onClick = onSaveClick) {
                Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = SCREEN_PADDING_HORIZONTAL,
                    vertical = SCREEN_PADDING_VERTICAL
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.tax_type),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(100.dp)
                )
                CapitalizedOutlinedTextField(
                    value = taxType,
                    onValueChange = {
                        onTaxTypeChange(
                            it.split(" ").joinToString(" ") { word ->
                                word.replaceFirstChar { char ->
                                    if (char.isLowerCase()) char.titlecase() else char.toString()
                                }
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.enter_tax_type)) },
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(ELEMENT_SPACING))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.base_on),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(100.dp)
                )
                SimpleDropdownField(
                    label = stringResource(R.string.base_on),
                    items = TaxBasedOn.entries,
                    selectedItem = TaxBasedOn.entries[selectedBasedOn],
                    onItemSelected = { onBasedOnChange(TaxBasedOn.entries.indexOf(it)) },
                    itemToString = { it.basedOn },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}