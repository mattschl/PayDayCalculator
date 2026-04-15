package ms.mattschlenkrich.paycalculator.tax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.FRAG_TAX_TYPE
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.compose.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.compose.StandardTopAppBar
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.TaxTypes
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel

private const val TAG = FRAG_TAX_TYPE

class TaxTypeFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var workTaxViewModel: WorkTaxViewModel
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        workTaxViewModel = mainActivity.workTaxViewModel
        mainViewModel = mainActivity.mainViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    TaxTypeContent(
                        workTaxViewModel = workTaxViewModel,
                        onTaxTypeSelected = { taxType ->
                            mainViewModel.setTaxType(taxType)
                            gotoTaxTypeUpdate()
                        },
                        onAddTaxType = { gotoTaxTypeAdd() },
                        onBack = { findNavController().navigateUp() }
                    )
                }
            }
        }
    }

    @Composable
    fun TaxTypeContent(
        workTaxViewModel: WorkTaxViewModel,
        onTaxTypeSelected: (TaxTypes) -> Unit,
        onAddTaxType: () -> Unit,
        onBack: () -> Unit
    ) {
        var searchQuery by remember { mutableStateOf("") }
        val taxTypes by if (searchQuery.isEmpty()) {
            workTaxViewModel.getTaxTypes().observeAsState(initial = emptyList())
        } else {
            workTaxViewModel.searchTaxTypes("%$searchQuery%")
                .observeAsState(initial = emptyList())
        }

        Scaffold(
            topBar = {
                StandardTopAppBar(
                    title = stringResource(R.string.choose_a_tax_type),
                    onBackClicked = onBack
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddTaxType,
                    containerColor = colorResource(id = R.color.dark_green),
                    contentColor = colorResource(id = R.color.white)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_new))
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(
                        horizontal = SCREEN_PADDING_HORIZONTAL,
                        vertical = SCREEN_PADDING_VERTICAL
                    )
            ) {
                SelectAllOutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.search)) },
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )

                if (taxTypes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.no_info_to_view),
                                modifier = Modifier.padding(32.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(top = 16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(taxTypes, key = { it.taxTypeId }) { taxType ->
                            TaxTypeItem(taxType, onTaxTypeSelected)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TaxTypeItem(
        taxType: TaxTypes,
        onLongPress: (TaxTypes) -> Unit
    ) {
        Card(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongPress(taxType) }
                    )
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            val display = if (taxType.ttIsDeleted) {
                taxType.taxType + stringResource(R.string._deleted_)
            } else {
                taxType.taxType
            }
            Text(
                text = display,
                modifier = Modifier.padding(16.dp),
                color = if (taxType.ttIsDeleted) {
                    colorResource(id = R.color.red)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }

    private fun gotoTaxTypeAdd() {
        mainViewModel.setCallingFragment(TAG)
        findNavController().navigate(
            TaxTypeFragmentDirections.actionTaxTypeFragmentToTaxTypeAddFragment()
        )
    }

    private fun gotoTaxTypeUpdate() {
        findNavController().navigate(
            TaxTypeFragmentDirections.actionTaxTypeFragmentToTaxTypeUpdateFragment()
        )
    }
}