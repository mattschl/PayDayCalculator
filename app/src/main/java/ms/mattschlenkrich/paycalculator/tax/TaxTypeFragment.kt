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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.FRAG_TAX_TYPE
import ms.mattschlenkrich.paycalculator.common.SCREEN_PADDING_HORIZONTAL
import ms.mattschlenkrich.paycalculator.common.SCREEN_PADDING_VERTICAL
import ms.mattschlenkrich.paycalculator.common.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.common.StandardTopAppBar
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
        mainActivity.topMenuBar.title = getString(R.string.choose_a_tax_type)

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    TaxTypeScreen()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TaxTypeScreen() {
        var searchQuery by remember { mutableStateOf("") }
        val taxTypes by if (searchQuery.isEmpty()) {
            workTaxViewModel.getTaxTypes().observeAsState(initial = emptyList<TaxTypes>())
        } else {
            workTaxViewModel.searchTaxTypes("%$searchQuery%")
                .observeAsState(initial = emptyList<TaxTypes>())
        }

        Scaffold(
            topBar = {
                StandardTopAppBar(
                    title = stringResource(R.string.choose_a_tax_type)
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { gotoTaxTypeAdd() }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Tax Type")
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
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(top = 16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(taxTypes, key = { it.taxType }) { taxType ->
                            TaxTypeItem(taxType)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TaxTypeItem(taxType: TaxTypes) {
        Card(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            mainViewModel.setTaxType(taxType)
                            gotoTaxTypeUpdateFragment()
                        }
                    )
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            val display = if (taxType.ttIsDeleted) {
                taxType.taxType + stringResource(R.string._deleted_)
            } else {
                taxType.taxType
            }
            Text(
                text = display,
                modifier = Modifier.padding(16.dp),
                color = if (taxType.ttIsDeleted) Color.Red else Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }

    private fun gotoTaxTypeAdd() {
        mainViewModel.setCallingFragment(TAG)
        view?.findNavController()?.navigate(
            TaxTypeFragmentDirections.actionTaxTypeFragmentToTaxTypeAddFragment()
        )
    }

    fun gotoTaxTypeUpdateFragment() {
        view?.findNavController()?.navigate(
            TaxTypeFragmentDirections.actionTaxTypeFragmentToTaxTypeUpdateFragment()
        )
    }
}