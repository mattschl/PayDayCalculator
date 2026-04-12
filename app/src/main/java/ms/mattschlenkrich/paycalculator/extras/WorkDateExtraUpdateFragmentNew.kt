package ms.mattschlenkrich.paycalculator.extras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.WorkDateExtras

class WorkDateExtraUpdateFragmentNew : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainActivity = requireActivity() as MainActivity
        val mainViewModel = mainActivity.mainViewModel
        val payDayViewModel = mainActivity.payDayViewModel

        val workDate = mainViewModel.getWorkDateObject()!!
        val initialExtra = mainViewModel.getWorkDateExtra()!!
        val employerName = mainViewModel.getEmployerString() ?: ""

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    val existingExtras = payDayViewModel.getWorkDateExtras(workDate.workDateId)
                        .observeAsState(emptyList()).value

                    WorkDateExtraScreen(
                        initialWorkDate = workDate,
                        employerName = employerName,
                        initialExtra = initialExtra,
                        existingExtras = existingExtras,
                        onUpdate = { updatedExtra ->
                            payDayViewModel.updateWorkDateExtra(updatedExtra)
                            findNavController().navigateUp()
                        },
                        onDelete = { extraToDelete ->
                            val deletedExtra = WorkDateExtras(
                                extraToDelete.workDateExtraId,
                                extraToDelete.wdeWorkDateId,
                                extraToDelete.wdeExtraTypeId,
                                extraToDelete.wdeName,
                                extraToDelete.wdeAppliesTo,
                                extraToDelete.wdeAttachTo,
                                extraToDelete.wdeValue,
                                extraToDelete.wdeIsFixed,
                                extraToDelete.wdeIsCredit,
                                true,
                                DateFunctions().getCurrentTimeAsString()
                            )
                            payDayViewModel.updateWorkDateExtra(deletedExtra)
                            findNavController().navigateUp()
                        },
                        onCancel = { findNavController().navigateUp() }
                    )
                }
            }
        }
    }
}