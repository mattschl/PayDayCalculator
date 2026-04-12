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

class WorkDateExtraAddFragmentNew : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainActivity = requireActivity() as MainActivity
        val mainViewModel = mainActivity.mainViewModel
        val payDayViewModel = mainActivity.payDayViewModel

        val workDate = mainViewModel.getWorkDateObject()!!

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    val employerName = mainViewModel.getEmployerString() ?: ""
                    val existingExtras = payDayViewModel.getWorkDateExtras(workDate.workDateId)
                        .observeAsState(emptyList()).value

                    WorkDateExtraScreen(
                        initialWorkDate = workDate,
                        employerName = employerName,
                        initialExtra = null,
                        existingExtras = existingExtras,
                        onUpdate = { newExtra ->
                            payDayViewModel.insertWorkDateExtra(newExtra)
                            findNavController().navigateUp()
                        },
                        onDelete = { /* Add fragment doesn't delete */ },
                        onCancel = { findNavController().navigateUp() }
                    )
                }
            }
        }
    }
}