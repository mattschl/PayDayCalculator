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

class PayPeriodExtraAddFragmentNew : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainActivity = requireActivity() as MainActivity
        val mainViewModel = mainActivity.mainViewModel
        val payDayViewModel = mainActivity.payDayViewModel
        val workExtraViewModel = mainActivity.workExtraViewModel

        val curPayPeriod = mainViewModel.getPayPeriod()!!
        val curEmployer = mainViewModel.getEmployer()!!

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
                        initialExtra = null,
                        existingPayPeriodExtras = existingPayPeriodExtras,
                        existingWorkDateExtras = existingWorkDateExtras,
                        defaultExtras = defaultExtras,
                        onUpdate = { newExtra ->
                            payDayViewModel.insertPayPeriodExtra(newExtra)
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