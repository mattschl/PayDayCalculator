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
import ms.mattschlenkrich.paycalculator.common.FRAG_EXTRA_DEFINITIONS
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel

class EmployerExtraDefinitionsFragmentNew : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var workExtraViewModel: WorkExtraViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        employerViewModel = mainActivity.employerViewModel
        mainViewModel = mainActivity.mainViewModel
        workExtraViewModel = mainActivity.workExtraViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    EmployerExtraDefinitionsScreen(
                        employerViewModel = employerViewModel,
                        workExtraViewModel = workExtraViewModel,
                        initialEmployer = mainViewModel.getEmployer(),
                        initialExtraType = mainViewModel.getWorkExtraType(),
                        onAddExtraDefinition = { employer, extraType ->
                            mainViewModel.apply {
                                setEmployer(employer)
                                setWorkExtraType(extraType)
                                addCallingFragment(FRAG_EXTRA_DEFINITIONS)
                            }
                            findNavController().navigate(
                                EmployerExtraDefinitionsFragmentNewDirections.actionEmployerExtraDefinitionsFragmentToEmployerExtraDefinitionsAddFragment()
                            )
                        },
                        onUpdateExtraDefinition = { definition ->
                            mainViewModel.apply {
                                setEmployer(definition.employer)
                                setExtraDefinitionFull(definition)
                            }
                            findNavController().navigate(
                                EmployerExtraDefinitionsFragmentNewDirections.actionEmployerExtraDefinitionsFragmentToEmployerExtraDefinitionUpdateFragment()
                            )
                        },
                        onUpdateExtraType = { employer, extraType ->
                            mainViewModel.apply {
                                setEmployer(employer)
                                setWorkExtraType(extraType)
                                addCallingFragment(FRAG_EXTRA_DEFINITIONS)
                            }
                            findNavController().navigate(
                                EmployerExtraDefinitionsFragmentNewDirections.actionEmployerExtraDefinitionsFragmentToWorkExtraTypeUpdateFragment()
                            )
                        },
                        onAddNewEmployer = {
                            findNavController().navigate(
                                EmployerExtraDefinitionsFragmentNewDirections.actionEmployerExtraDefinitionsFragmentToEmployerAddFragment()
                            )
                        },
                        onAddNewExtraType = { employer ->
                            mainViewModel.apply {
                                setEmployer(employer)
                                addCallingFragment(FRAG_EXTRA_DEFINITIONS)
                            }
                            findNavController().navigate(
                                EmployerExtraDefinitionsFragmentNewDirections.actionEmployerExtraDefinitionsFragmentToWorkExtraTypeAddFragment()
                            )
                        },
                        onDeleteExtraDefinition = { definition ->
                            workExtraViewModel.deleteWorkExtraDefinition(
                                definition.definition.workExtraDefId,
                                ms.mattschlenkrich.paycalculator.common.DateFunctions()
                                    .getCurrentTimeAsString()
                            )
                        },
                        onCancel = {
                            findNavController().navigateUp()
                        }
                    )
                }
            }
        }
    }
}