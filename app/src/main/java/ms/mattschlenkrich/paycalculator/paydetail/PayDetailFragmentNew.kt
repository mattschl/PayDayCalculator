package ms.mattschlenkrich.paycalculator.paydetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_PAY_DETAILS
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.ExtraContainer
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.data.TaxAndAmount
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.logic.PayCalculationsAsync
import java.time.LocalDate

class PayDetailFragmentNew : Fragment(), IPayDetailsFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var payDayViewModel: PayDayViewModel
    private lateinit var payDetailViewModel: PayDetailViewModel
    private lateinit var payCalculationsViewModel: PayCalculationsViewModel
    private lateinit var workExtraViewModel: WorkExtraViewModel

    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        employerViewModel = mainActivity.employerViewModel
        payDayViewModel = mainActivity.payDayViewModel
        payDetailViewModel = mainActivity.payDetailViewModel
        payCalculationsViewModel = mainActivity.payCalculationsViewModel
        workExtraViewModel = mainActivity.workExtraViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                val employers by employerViewModel.getEmployers().observeAsState(emptyList())
                var selectedEmployer by remember { mutableStateOf<Employers?>(null) }
                val cutOffDates by if (selectedEmployer != null) {
                    payDayViewModel.getCutOffDates(selectedEmployer!!.employerId)
                        .observeAsState(emptyList())
                } else {
                    remember { mutableStateOf(emptyList()) }
                }
                var selectedCutOffDate by remember { mutableStateOf("") }

                var paySummary by remember { mutableStateOf(PaySummaryData()) }
                var hourlyBreakdown by remember { mutableStateOf(HourlyBreakdownData()) }
                var credits by remember { mutableStateOf<List<ExtraContainer>>(emptyList()) }
                var deductions by remember { mutableStateOf<List<ExtraContainer>>(emptyList()) }
                var taxes by remember { mutableStateOf<List<TaxAndAmount>>(emptyList()) }

                val trigger by recalculationTrigger

                // Initial selection from history
                LaunchedEffect(employers) {
                    if (selectedEmployer == null && employers.isNotEmpty()) {
                        val historyEmployer = mainViewModel.getEmployer()
                        selectedEmployer = historyEmployer ?: employers.first()
                    }
                }

                LaunchedEffect(cutOffDates, selectedEmployer) {
                    if (selectedEmployer != null && cutOffDates.isNotEmpty()) {
                        val historyCutOff = mainViewModel.getCutOffDate()
                        val historyEmployerId = mainViewModel.getEmployer()?.employerId

                        // If the employer changed or there's no history for this employer, go to latest
                        if (historyEmployerId != selectedEmployer!!.employerId ||
                            historyCutOff == null ||
                            !cutOffDates.any { it.ppCutoffDate == historyCutOff }
                        ) {
                            selectedCutOffDate = cutOffDates.first().ppCutoffDate
                        } else if (selectedCutOffDate.isEmpty()) {
                            selectedCutOffDate = historyCutOff
                        }
                    }
                }

                // Recalculate when selection changes
                LaunchedEffect(selectedEmployer, selectedCutOffDate, trigger) {
                    if (selectedEmployer != null && selectedCutOffDate.isNotEmpty()) {
                        mainViewModel.setEmployer(selectedEmployer!!)
                        mainViewModel.setCutOffDate(selectedCutOffDate)

                        val payPeriod = payDayViewModel.getPayPeriodSync(
                            selectedCutOffDate,
                            selectedEmployer!!.employerId
                        )
                        if (payPeriod != null) {
                            mainViewModel.setPayPeriod(payPeriod)
                            val payCalculations =
                                PayCalculationsAsync(
                                    payCalculationsViewModel,
                                    payDetailViewModel,
                                    selectedEmployer!!,
                                    payPeriod
                                )
                            payCalculations.waitForCalculations()

                            credits = payCalculations.getCredits()
                            deductions = payCalculations.getDebits()
                            taxes = payCalculations.getTaxList()

                            val payDay = df.getDisplayDate(
                                LocalDate.parse(selectedCutOffDate)
                                    .plusDays(selectedEmployer!!.cutoffDaysBefore.toLong())
                                    .toString()
                            )

                            paySummary = PaySummaryData(
                                payDayMessage = getString(R.string.pay_day_is_) + payDay,
                                grossPay = nf.displayDollars(payCalculations.getPayGross()),
                                deductions = nf.displayDollars(-payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()),
                                netPay = getString(R.string.net_) + nf.displayDollars(
                                    payCalculations.getPayGross() - payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()
                                ),
                                totalCredits = nf.displayDollars(payCalculations.getCreditTotalAll()),
                                totalDeductions = nf.displayDollars(payCalculations.getDebitTotalsByPay() + payCalculations.getAllTaxDeductions())
                            )

                            val items = mutableListOf<HourlyItem>()
                            if (payCalculations.getPayReg() > 0.0) {
                                items.add(
                                    HourlyItem(
                                        getString(R.string.reg_hours),
                                        nf.getNumberFromDouble(payCalculations.getHoursReg()),
                                        nf.displayDollars(payCalculations.getPayRate()),
                                        nf.displayDollars(payCalculations.getPayReg())
                                    )
                                )
                            }
                            if (payCalculations.getPayOt() > 0.0) {
                                items.add(
                                    HourlyItem(
                                        getString(R.string.overtime),
                                        nf.getNumberFromDouble(payCalculations.getHoursOt()),
                                        nf.displayDollars(payCalculations.getPayRate() * 1.5),
                                        nf.displayDollars(payCalculations.getPayOt())
                                    )
                                )
                            }
                            if (payCalculations.getPayDblOt() > 0.0) {
                                items.add(
                                    HourlyItem(
                                        getString(R.string.double_overtime),
                                        nf.getNumberFromDouble(payCalculations.getHoursDblOt()),
                                        nf.displayDollars(payCalculations.getPayRate() * 2),
                                        nf.displayDollars(payCalculations.getPayDblOt())
                                    )
                                )
                            }
                            if (payCalculations.getPayStat() > 0.0) {
                                items.add(
                                    HourlyItem(
                                        getString(R.string.other_hours),
                                        nf.getNumberFromDouble(payCalculations.getHoursStat()),
                                        nf.displayDollars(payCalculations.getPayRate()),
                                        nf.displayDollars(payCalculations.getPayStat())
                                    )
                                )
                            }
                            hourlyBreakdown = HourlyBreakdownData(
                                items,
                                nf.displayDollars(payCalculations.getPayAllHourly())
                            )
                        }
                    }
                }

                PayDetailScreen(
                    employers = employers,
                    selectedEmployer = selectedEmployer,
                    onEmployerSelected = {
                        if (selectedEmployer?.employerId != it.employerId) {
                            selectedEmployer = it
                            selectedCutOffDate = "" // Reset cutoff when employer changes
                        }
                    },
                    onAddNewEmployer = {
                        mainViewModel.setCallingFragment(FRAG_PAY_DETAILS)
                        findNavController().navigate(PayDetailFragmentNewDirections.actionPayDetailFragmentNewToEmployerAddFragment())
                    },
                    cutOffDates = cutOffDates.map { it.ppCutoffDate },
                    selectedCutOffDate = selectedCutOffDate,
                    onCutOffDateSelected = { selectedCutOffDate = it },
                    paySummary = paySummary,
                    hourlyBreakdown = hourlyBreakdown,
                    credits = credits,
                    deductions = deductions,
                    taxes = taxes,
                    onAddCreditClick = {
                        if (selectedEmployer != null && selectedCutOffDate.isNotEmpty()) {
                            gotoExtraAddFragment(true, selectedEmployer!!, selectedCutOffDate)
                        }
                    },
                    onAddDeductionClick = {
                        if (selectedEmployer != null && selectedCutOffDate.isNotEmpty()) {
                            gotoExtraAddFragment(false, selectedEmployer!!, selectedCutOffDate)
                        }
                    },
                    onExtraClick = { extra ->
                        if (extra.payPeriodExtra != null) {
                            mainViewModel.setEmployer(selectedEmployer!!)
                            mainViewModel.setCutOffDate(selectedCutOffDate)
                            mainViewModel.setPayPeriodExtra(extra.payPeriodExtra!!)
                            gotoPeriodExtraUpdateFragment()
                        }
                    },
                    onExtraActiveChange = { extra: ExtraContainer, active: Boolean ->
                        val payPeriod = cutOffDates.find { it.ppCutoffDate == selectedCutOffDate }
                        if (payPeriod != null) {
                            viewLifecycleOwner.lifecycleScope.launch {
                                insertOrUpdateExtraOnChange(extra, !active, payPeriod.payPeriodId)
                                delay(WAIT_500)
                                triggerRecalculation()
                            }
                        }
                    },
                    onDeleteCutOffClick = {
                        if (selectedEmployer != null && selectedCutOffDate.isNotEmpty()) {
                            confirmDeletingCutoffDate(selectedEmployer!!, selectedCutOffDate)
                        }
                    },
                    onBackClick = { findNavController().popBackStack() }
                )
            }
        }
    }

    private var recalculationTrigger = mutableStateOf(0)

    private fun triggerRecalculation() {
        recalculationTrigger.value++
    }

    private fun insertOrUpdateExtraOnChange(
        extraContainer: ExtraContainer, delete: Boolean, payPeriodId: Long
    ) {
        if (extraContainer.payPeriodExtra != null) {
            val payPeriodExtra = extraContainer.payPeriodExtra!!
            val newExtra = WorkPayPeriodExtras(
                payPeriodExtra.workPayPeriodExtraId,
                payPeriodExtra.ppePayPeriodId,
                payPeriodExtra.ppeExtraTypeId,
                payPeriodExtra.ppeName,
                payPeriodExtra.ppeAppliesTo,
                3,
                payPeriodExtra.ppeValue,
                payPeriodExtra.ppeIsFixed,
                payPeriodExtra.ppeIsCredit,
                delete,
                df.getCurrentTimeAsString()
            )
            extraContainer.payPeriodExtra = newExtra
            payDayViewModel.updatePayPeriodExtra(newExtra)
        } else if (extraContainer.extraDefinitionAndType != null) {
            val extraDefinitionAndType = extraContainer.extraDefinitionAndType!!
            val newExtra = WorkPayPeriodExtras(
                nf.generateRandomIdAsLong(),
                payPeriodId,
                extraDefinitionAndType.extraType.workExtraTypeId,
                extraDefinitionAndType.extraType.wetName,
                extraDefinitionAndType.extraType.wetAppliesTo,
                extraDefinitionAndType.extraType.wetAttachTo,
                extraDefinitionAndType.definition.weValue,
                extraDefinitionAndType.definition.weIsFixed,
                extraDefinitionAndType.extraType.wetIsCredit,
                delete,
                df.getCurrentTimeAsString()
            )
            extraContainer.payPeriodExtra = newExtra
            payDayViewModel.insertPayPeriodExtra(newExtra)
        }
    }

    private fun gotoExtraAddFragment(isCredit: Boolean, employer: Employers, cutOff: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.warning_))
            .setMessage(getString(R.string.it_is_best_to_add_custom_extras_only_after_all_the_work_hours_have_been_entered))
            .setPositiveButton(getString(R.string.continue_)) { _, _ ->
                payDayViewModel.getPayPeriod(cutOff, employer.employerId)
                    .observe(viewLifecycleOwner) { payPeriod ->
                        if (payPeriod != null) {
                            mainViewModel.setPayPeriod(payPeriod)
                            mainViewModel.setEmployer(employer)
                            mainViewModel.setIsCredit(isCredit)
                            findNavController().navigate(PayDetailFragmentNewDirections.actionPayDetailFragmentNewToPayPeriodExtraAddFragment())
                        }
                    }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun confirmDeletingCutoffDate(employer: Employers, cutOff: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_delete_pay_period))
            .setMessage(
                getString(R.string.warning_) + "\n" + getString(R.string.this_action_cannot_be_undone) + getString(
                    R.string.all_the_work_dates_will_have_to_be_re_entered
                )
            )
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val payPeriod = payDayViewModel.getPayPeriodSync(cutOff, employer.employerId)
                    if (payPeriod != null) {
                        payDayViewModel.updatePayPeriod(
                            payPeriod.copy(
                                ppIsDeleted = true,
                                ppUpdateTime = df.getCurrentTimeAsString()
                            )
                        )
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun populatePayDetails() {
        // Not used in Compose version or triggered externally
    }

    override fun gotoPeriodExtraUpdateFragment() {
        findNavController().navigate(PayDetailFragmentNewDirections.actionPayDetailFragmentNewToPayPeriodExtraUpdateFragment())
    }
}