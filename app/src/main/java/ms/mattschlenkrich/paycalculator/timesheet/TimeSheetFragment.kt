package ms.mattschlenkrich.paycalculator.timesheet

import android.app.AlertDialog
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
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.PayPeriods
import ms.mattschlenkrich.paycalculator.data.WorkDates
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.logic.PayCalculationsAsync
import ms.mattschlenkrich.paycalculator.logic.PayDateProjections
import java.time.LocalDate

class TimeSheetFragment : Fragment(), ITimeSheetFragment {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var payDayViewModel: PayDayViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel

    private val projections = PayDateProjections()
    private val nf = NumberFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        employerViewModel = mainActivity.employerViewModel
        payDayViewModel = mainActivity.payDayViewModel
        workOrderViewModel = mainActivity.workOrderViewModel

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
                var paySummary by remember { mutableStateOf(TimeSheetPaySummary()) }
                val workDates by if (selectedEmployer != null && selectedCutOffDate.isNotEmpty()) {
                    payDayViewModel.getWorkDateList(
                        selectedEmployer!!.employerId,
                        selectedCutOffDate
                    )
                        .observeAsState(emptyList())
                } else {
                    remember { mutableStateOf(emptyList()) }
                }

                val extrasList by if (selectedEmployer != null && selectedCutOffDate.isNotEmpty()) {
                    payDayViewModel.getWorkDateExtrasPerPay(
                        selectedEmployer!!.employerId,
                        selectedCutOffDate
                    )
                        .observeAsState(emptyList())
                } else {
                    remember { mutableStateOf(emptyList()) }
                }

                val workDateExtras = remember(extrasList) {
                    extrasList.groupBy { it.extra.wdeWorkDateId }
                }

                // Initial selection from history
                LaunchedEffect(employers) {
                    if (selectedEmployer == null && employers.isNotEmpty()) {
                        val historyEmployer = mainViewModel.getEmployer()
                        selectedEmployer = historyEmployer ?: employers.first()
                    }
                }

                LaunchedEffect(cutOffDates, selectedEmployer) {
                    if (selectedEmployer != null) {
                        if (cutOffDates.isEmpty()) {
                            generateNewCutOff(selectedEmployer!!)
                        } else {
                            val historyCutOff = mainViewModel.getCutOffDate()
                            val historyEmployerId = mainViewModel.getEmployer()?.employerId

                            if (historyEmployerId != selectedEmployer!!.employerId ||
                                historyCutOff == null ||
                                !cutOffDates.any { it.ppCutoffDate == historyCutOff }
                            ) {
                                // Default to the first (latest) cutoff if no valid history
                                if (selectedCutOffDate.isEmpty() || !cutOffDates.any { it.ppCutoffDate == selectedCutOffDate }) {
                                    selectedCutOffDate = cutOffDates.first().ppCutoffDate
                                }
                            } else if (selectedCutOffDate.isEmpty()) {
                                selectedCutOffDate = historyCutOff
                            }
                        }
                    }
                }

                // Update summary and set global state
                LaunchedEffect(selectedEmployer, selectedCutOffDate, workDates) {
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
                                PayCalculationsAsync(mainActivity, selectedEmployer!!, payPeriod)
                            payCalculations.waitForCalculations()

                            val week1EndDate =
                                LocalDate.parse(selectedCutOffDate).minusDays(7).toString()
                            val wk1Summary =
                                getWeekSummary(workDates.filter { it.wdDate <= week1EndDate })
                            val wk2Summary =
                                getWeekSummary(workDates.filter { it.wdDate > week1EndDate })

                            var totalHoursDesc = ""
                            if (payCalculations.getHoursReg() > 0) totalHoursDesc += "${
                                nf.getNumberFromDouble(
                                    payCalculations.getHoursReg()
                                )
                            } hr "
                            if (payCalculations.getHoursOt() > 0) totalHoursDesc += "| ${
                                nf.getNumberFromDouble(
                                    payCalculations.getHoursOt()
                                )
                            } ot "
                            if (payCalculations.getHoursDblOt() > 0) totalHoursDesc += "| ${
                                nf.getNumberFromDouble(
                                    payCalculations.getHoursDblOt()
                                )
                            } dbl ot "
                            if (payCalculations.getHoursStat() > 0) totalHoursDesc += "| ${
                                nf.getNumberFromDouble(
                                    payCalculations.getHoursStat()
                                )
                            } other "

                            paySummary = TimeSheetPaySummary(
                                grossPay = nf.displayDollars(payCalculations.getPayGross()),
                                deductions = nf.displayDollars(-payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()),
                                netPay = nf.displayDollars(payCalculations.getPayGross() - payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()),
                                totalHoursDescription = getString(R.string.totals) + " " + totalHoursDesc.trim(),
                                week1Total = getString(R.string.week_1_) + (wk1Summary.ifBlank {
                                    getString(
                                        R.string._0_hr
                                    )
                                }),
                                week2Total = getString(R.string.week_2_) + (wk2Summary.ifBlank {
                                    getString(
                                        R.string._0_hr
                                    )
                                })
                            )
                        }
                    }
                }

                TimeSheetScreen(
                    employers = employers,
                    selectedEmployer = selectedEmployer,
                    onEmployerSelected = {
                        if (selectedEmployer?.employerId != it.employerId) {
                            selectedEmployer = it
                            selectedCutOffDate = ""
                        }
                    },
                    onAddNewEmployer = {
                        setCurrentVariables(selectedEmployer, selectedCutOffDate)
                        findNavController().navigate(TimeSheetFragmentDirections.actionTimeSheetFragmentToEmployerAddFragment())
                    },
                    cutOffDates = cutOffDates.map { it.ppCutoffDate },
                    selectedCutOffDate = selectedCutOffDate,
                    onCutOffDateSelected = { selectedCutOffDate = it },
                    paySummary = paySummary,
                    workDates = workDates,
                    workDateExtras = workDateExtras,
                    onWorkDateClick = { workDate ->
                        mainViewModel.setWorkDateObject(workDate)
                        setCurrentVariables(selectedEmployer, selectedCutOffDate)
                        findNavController().navigate(TimeSheetFragmentDirections.actionTimeSheetFragmentToWorkDateUpdateFragment())
                    },
                    onWorkDateLongClick = { workDate ->
                        showWorkDateOptions(workDate)
                    },
                    onAddWorkDateClick = {
                        setCurrentVariables(selectedEmployer, selectedCutOffDate)
                        findNavController().navigate(TimeSheetFragmentDirections.actionTimeSheetFragmentToWorkDateAddFragment())
                    },
                    onViewPayDetailsClick = {
                        setCurrentVariables(selectedEmployer, selectedCutOffDate)
                        lifecycleScope.launch {
                            delay(WAIT_250)
                            findNavController().navigate(TimeSheetFragmentDirections.actionTimeSheetFragmentToPayDetailFragmentNew())
                        }
                    },
                    onGenerateCutoffClick = {
                        if (selectedEmployer != null) {
                            generateNewCutOff(selectedEmployer!!)
                        }
                    },
                    displayDate = { df.getDisplayDate(it) },
                    formatHours = { formatWorkDateHours(it) }
                )
            }
        }
    }

    private fun getWeekSummary(workDates: List<WorkDates>): String {
        var reg = 0.0;
        var ot = 0.0;
        var dbl = 0.0;
        var stat = 0.0
        workDates.forEach {
            reg += it.wdRegHours; ot += it.wdOtHours; dbl += it.wdDblOtHours; stat += it.wdStatHours
        }
        val parts = mutableListOf<String>()
        if (reg > 0) parts.add("${nf.getNumberFromDouble(reg)} ${getString(R.string.hr)}")
        if (ot > 0) parts.add("${nf.getNumberFromDouble(ot)} ${getString(R.string.ot)}")
        if (dbl > 0) parts.add("${nf.getNumberFromDouble(dbl)} ${getString(R.string.dbl_ot)}")
        if (stat > 0) parts.add("${nf.getNumberFromDouble(stat)} ${getString(R.string.other_hours)}")
        return parts.joinToString(getString(R.string.pipe))
    }

    private fun formatWorkDateHours(workDate: WorkDates): String {
        val parts = mutableListOf<String>()
        if (workDate.wdRegHours > 0) parts.add(
            "${nf.getNumberFromDouble(workDate.wdRegHours)}${
                getString(
                    R.string.hrs
                )
            }"
        )
        if (workDate.wdOtHours > 0) parts.add(
            "${nf.getNumberFromDouble(workDate.wdOtHours)}${
                getString(
                    R.string.ot_hrs
                )
            }"
        )
        if (workDate.wdDblOtHours > 0) parts.add(
            "${nf.getNumberFromDouble(workDate.wdDblOtHours)}${
                getString(
                    R.string.dbl_ot_hrs
                )
            }"
        )
        if (workDate.wdStatHours > 0) parts.add(
            "${nf.getNumberFromDouble(workDate.wdStatHours)}${
                getString(
                    R.string.other_hrs
                )
            }"
        )

        var display = parts.joinToString(getString(R.string.pipe))
        if (!workDate.wdNote.isNullOrBlank()) {
            if (display.isNotBlank()) display += " - "
            display += workDate.wdNote
        }
        return display.ifBlank { getString(R.string.no_time_entered) }
    }

    private fun showWorkDateOptions(workDate: WorkDates) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_an_action_for) + df.getDisplayDate(workDate.wdDate))
            .setItems(
                arrayOf(
                    getString(R.string.open_this_date),
                    getString(R.string.delete_this_date)
                )
            ) { _, pos ->
                when (pos) {
                    0 -> {
                        mainViewModel.setWorkDateObject(workDate)
                        findNavController().navigate(TimeSheetFragmentDirections.actionTimeSheetFragmentToWorkDateUpdateFragment())
                    }

                    1 -> confirmDeleteWorkDate(workDate)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun confirmDeleteWorkDate(workDate: WorkDates) {
        AlertDialog.Builder(requireContext())
            .setTitle(
                getString(R.string.are_you_sure_you_want_to_delete) + df.getDisplayDate(
                    workDate.wdDate
                )
            )
            .setMessage(getString(R.string.this_cannot_be_undone))
            .setPositiveButton(getString(R.string.delete)) { _, _ -> deleteWorkDate(workDate) }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteWorkDate(workDate: WorkDates) {
        val deletedDate =
            workDate.copy(wdIsDeleted = true, wdUpdateTime = df.getCurrentTimeAsString())
        payDayViewModel.updateWorkDate(deletedDate)
        workOrderViewModel.deleteWorkOrderHistoryByWorkDateId(
            workDate.workDateId,
            df.getCurrentTimeAsString()
        )
        payDayViewModel.deleteWorkDateExtrasByDateId(
            workDate.workDateId,
            df.getCurrentTimeAsString()
        )
    }

    private fun generateNewCutOff(employer: Employers) {
        lifecycleScope.launch {
            val dates = payDayViewModel.getCutOffDatesSync(employer.employerId)
            val nextCutOff =
                projections.generateNextCutOff(employer, dates.firstOrNull()?.ppCutoffDate ?: "")
            if (nextCutOff.isNotEmpty()) {
                payDayViewModel.insertPayPeriod(
                    PayPeriods(
                        nf.generateRandomIdAsLong(),
                        nextCutOff,
                        employer.employerId,
                        false,
                        df.getCurrentTimeAsString()
                    )
                )
            }
        }
    }

    private fun setCurrentVariables(employer: Employers?, cutOffDate: String) {
        mainViewModel.apply {
            setEmployer(employer)
            setCutOffDate(cutOffDate)
            if (employer != null && cutOffDate.isNotEmpty()) {
                lifecycleScope.launch {
                    val payPeriod =
                        payDayViewModel.getPayPeriodSync(cutOffDate, employer.employerId)
                    setPayPeriod(payPeriod)
                }
            }
        }
    }

    override fun populatePayDetails() {
        // Kept for interface compatibility, logic handled in Compose
    }
}