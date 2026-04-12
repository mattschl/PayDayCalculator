package ms.mattschlenkrich.paycalculator.extras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions

class PayPeriodExtraUpdateFragmentNew : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainActivity = requireActivity() as MainActivity
        val mainViewModel = mainActivity.mainViewModel
        val payDayViewModel = mainActivity.payDayViewModel
        val workExtraViewModel = mainActivity.workExtraViewModel

        val curPayPeriod = mainViewModel.getPayPeriod()
        val curEmployer = mainViewModel.getEmployer()
        val initialExtra = mainViewModel.getPayPeriodExtra()

        if (curPayPeriod == null || curEmployer == null || initialExtra == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.an_unknown_error_occurred),
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigateUp()
            return View(requireContext())
        }

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    val employerName = curEmployer.employerName
                    val existingPayPeriodExtras =
                        payDayViewModel.getPayPeriodExtras(curPayPeriod.payPeriodId)
                            .observeAsState(emptyList()).value
                    val existingWorkDateExtras = payDayViewModel.getWorkDateExtrasPerPay(
                        curEmployer.employerId, curPayPeriod.ppCutoffDate
                    ).observeAsState(emptyList()).value
                    val defaultExtras = workExtraViewModel.getDefaultExtraTypesAndCurrentDef(
                        curEmployer.employerId, curPayPeriod.ppCutoffDate
                    ).observeAsState(emptyList()).value

                    PayPeriodExtraScreen(
                        curPayPeriod = curPayPeriod,
                        employerName = employerName,
                        initialExtra = initialExtra,
                        existingPayPeriodExtras = existingPayPeriodExtras,
                        existingWorkDateExtras = existingWorkDateExtras,
                        defaultExtras = defaultExtras,
                        onUpdate = { updatedExtra ->
                            payDayViewModel.updatePayPeriodExtra(updatedExtra)
                            mainViewModel.setPayPeriodExtra(null)
                            findNavController().navigateUp()
                        },
                        onDelete = { extraToDelete ->
                            payDayViewModel.deletePayPeriodExtra(
                                extraToDelete.workPayPeriodExtraId,
                                DateFunctions().getCurrentTimeAsString()
                            )
                            mainViewModel.setPayPeriodExtra(null)
                            findNavController().navigateUp()
                        },
                        onCancel = {
                            mainViewModel.setPayPeriodExtra(null)
                            findNavController().navigateUp()
                        }
                    )
                }
            }
        }
    }
}