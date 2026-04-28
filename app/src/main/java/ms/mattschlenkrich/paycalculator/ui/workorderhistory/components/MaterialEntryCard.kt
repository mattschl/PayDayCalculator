package ms.mattschlenkrich.paycalculator.ui.workorderhistory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.compose.AutoCompleteTextField
import ms.mattschlenkrich.paycalculator.common.compose.DecimalOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.ELEMENT_SPACING
import ms.mattschlenkrich.paycalculator.data.Material

@Composable
fun MaterialEntryCard(
    materialQty: String,
    onMaterialQtyChange: (String) -> Unit,
    material: String,
    onMaterialChange: (String) -> Unit,
    materialList: List<Material>,
    onMaterialSelected: (Material) -> Unit,
    onAddMaterial: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
//                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ELEMENT_SPACING)
        ) {
            Text(
                text = stringResource(id = R.string.add_materials_used_below),
                style = MaterialTheme.typography.titleMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Row() {
                DecimalOutlinedTextField(
                    value = materialQty,
                    onValueChange = onMaterialQtyChange,
                    label = { Text(stringResource(id = R.string.qty)) },
                    modifier = Modifier.width(40.dp)
                )
                AutoCompleteTextField(
                    value = material,
                    onValueChange = onMaterialChange,
                    label = stringResource(id = R.string.material),
                    suggestions = materialList,
                    onItemSelected = onMaterialSelected,
                    modifier = Modifier.fillMaxWidth(),
                    itemToString = { it.mName }
                )
            }
            Button(
                onClick = onAddMaterial,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(id = R.string.add))
            }
        }
    }
}