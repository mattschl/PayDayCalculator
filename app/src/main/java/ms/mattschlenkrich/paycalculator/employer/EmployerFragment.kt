package ms.mattschlenkrich.paycalculator.employer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import ms.mattschlenkrich.paycalculator.common.compose.SelectAllOutlinedTextField
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.MainViewModel

class EmployerFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        employerViewModel = mainActivity.employerViewModel
        mainViewModel = mainActivity.mainViewModel
        mainActivity.topMenuBar.title = getString(R.string.view_employers)

        return ComposeView(requireContext()).apply {
            setContent {
                EmployerListScreen(
                    employerViewModel = employerViewModel,
                    onEmployerClick = { employer: ms.mattschlenkrich.paycalculator.data.Employers ->
                        mainViewModel.setEmployer(employer)
                        findNavController().navigate(
                            EmployerFragmentDirections.actionEmployerFragmentToEmployerUpdateFragment()
                        )
                    },
                    onAddClick = {
                        findNavController().navigate(
                            EmployerFragmentDirections.actionEmployerFragmentToEmployerAddFragment()
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun EmployerListScreen(
    employerViewModel: EmployerViewModel,
    onEmployerClick: (Employers) -> Unit,
    onAddClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val employers by if (searchQuery.isEmpty()) {
        employerViewModel.getEmployers().observeAsState(emptyList())
    } else {
        employerViewModel.searchEmployers("%$searchQuery%").observeAsState(emptyList())
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = colorResource(id = R.color.dark_green),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add_new)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SelectAllOutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                placeholder = { Text(stringResource(R.string.search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            if (employers.isEmpty()) {
                NoEmployersView()
            } else {
                EmployerList(
                    employers = employers,
                    onEmployerClick = onEmployerClick
                )
            }
        }
    }
}

@Composable
fun EmployerList(
    employers: List<Employers>,
    onEmployerClick: (Employers) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        items(employers) { employer ->
            EmployerItem(
                employer = employer,
                onClick = { onEmployerClick(employer) }
            )
        }
    }
}

@Composable
fun EmployerItem(
    employer: Employers,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = employer.employerName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            val display = if (employer.employerIsDeleted) {
                stringResource(R.string._deleted_)
            } else {
                employer.payFrequency
            }
            val textColor = if (employer.employerIsDeleted) Color.Red else Color.Black
            Text(
                text = display,
                fontSize = 16.sp,
                color = textColor,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
fun NoEmployersView() {
    Card(
        modifier = Modifier
            .padding(32.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            text = stringResource(R.string.no_employers_to_view),
            modifier = Modifier
                .padding(50.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            color = Color.Black
        )
    }
}