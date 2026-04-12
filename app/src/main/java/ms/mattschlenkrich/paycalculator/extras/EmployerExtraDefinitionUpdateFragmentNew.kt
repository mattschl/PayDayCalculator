package ms.mattschlenkrich.paycalculator.extras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel

class EmployerExtraDefinitionUpdateFragmentNew : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workExtraViewModel: WorkExtraViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        workExtraViewModel = mainActivity.workExtraViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    val definitionFull = mainViewModel.getExtraDefinitionFull()

                    EmployerExtraDefinitionScreen(
                        initialDefinitionFull = definitionFull,
                        onUpdate = { definition ->
                            workExtraViewModel.updateWorkExtraDefinition(definition)
                            findNavController().navigate(
                                EmployerExtraDefinitionUpdateFragmentNewDirections.actionEmployerExtraDefinitionUpdateFragmentToEmployerExtraDefinitionsFragment()
                            )
                        },
                        onDelete = { definition ->
                            workExtraViewModel.deleteWorkExtraDefinition(
                                definition.workExtraDefId,
                                ms.mattschlenkrich.paycalculator.common.DateFunctions()
                                    .getCurrentTimeAsString()
                            )
                            findNavController().navigate(
                                EmployerExtraDefinitionUpdateFragmentNewDirections.actionEmployerExtraDefinitionUpdateFragmentToEmployerExtraDefinitionsFragment()
                            )
                        },
                        onCancel = { findNavController().navigateUp() }
                    )
                }
            }
        }
    }
}