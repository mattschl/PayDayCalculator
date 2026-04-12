package ms.mattschlenkrich.paycalculator.extras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel

class WorkExtraTypeAddFragmentNew : Fragment() {

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
                androidx.compose.material3.MaterialTheme {
                    val curEmployer = mainViewModel.getEmployer()
                    if (curEmployer != null) {
                        val extraTypeList by workExtraViewModel.getExtraDefTypes(curEmployer.employerId)
                            .observeAsState(emptyList())

                        WorkExtraTypeScreen(
                            initialEmployer = curEmployer,
                            initialExtraType = null,
                            existingExtraTypes = extraTypeList,
                            onUpdate = { newExtraType ->
                                workExtraViewModel.insertWorkExtraType(newExtraType)
                                mainViewModel.setWorkExtraType(newExtraType)
                                findNavController().navigate(
                                    WorkExtraTypeAddFragmentNewDirections.actionWorkExtraTypeAddFragmentToEmployerExtraDefinitionsFragment()
                                )
                            },
                            onDelete = {},
                            onCancel = { findNavController().navigateUp() }
                        )
                    }
                }
            }
        }
    }
}