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
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel

class EmployerExtraDefinitionsAddFragmentNew : Fragment() {

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
                    val curEmployer = mainViewModel.getEmployer()
                    val curExtraType = mainViewModel.getWorkExtraType()

                    EmployerExtraDefinitionScreen(
                        initialDefinitionFull = if (curEmployer != null && curExtraType != null) {
                            ms.mattschlenkrich.paycalculator.data.ExtraDefTypeAndEmployer(
                                definition = ms.mattschlenkrich.paycalculator.data.WorkExtrasDefinitions(
                                    workExtraDefId = 0L,
                                    weEmployerId = curEmployer.employerId,
                                    weExtraTypeId = curExtraType.workExtraTypeId,
                                    weValue = 0.0,
                                    weIsFixed = true,
                                    weEffectiveDate = ms.mattschlenkrich.paycalculator.common.DateFunctions()
                                        .getCurrentDateAsString(),
                                    weIsDeleted = false,
                                    weUpdateTime = ms.mattschlenkrich.paycalculator.common.DateFunctions()
                                        .getCurrentTimeAsString()
                                ),
                                employer = curEmployer,
                                extraType = curExtraType
                            )
                        } else null,
                        onUpdate = { definition ->
                            workExtraViewModel.insertWorkExtraDefinition(definition)
                            gotoCallingFragment()
                        },
                        onDelete = { /* Not applicable for Add */ },
                        onCancel = { findNavController().navigateUp() }
                    )
                }
            }
        }
    }

    private fun gotoCallingFragment() {
        if (mainViewModel.getCallingFragment() != null) {
            if (mainViewModel.getCallingFragment()!!.contains(FRAG_EXTRA_DEFINITIONS)) {
                findNavController().navigate(
                    EmployerExtraDefinitionsAddFragmentNewDirections.actionEmployerExtraDefinitionsAddFragmentToEmployerExtraDefinitionsFragment()
                )
                return
            }
        }
        findNavController().navigate(
            EmployerExtraDefinitionsAddFragmentNewDirections.actionEmployerExtraDefinitionsAddFragmentToEmployerUpdateFragment()
        )
    }
}