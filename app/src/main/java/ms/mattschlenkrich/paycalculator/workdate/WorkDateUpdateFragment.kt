package ms.mattschlenkrich.paycalculator.workdate

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_TIME_SHEET
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_TIME
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_UPDATE
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_1000
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.data.HolidayPayCalculator
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.WorkDates
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.data.WorkOrderViewModel
import kotlin.math.round

private const val TAG = FRAG_WORK_DATE_UPDATE

class WorkDateUpdateFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var payDayViewModel: PayDayViewModel
    private lateinit var workExtraViewModel: WorkExtraViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel

    private var curDateString by mutableStateOf("")
    private var displayDate by mutableStateOf("")
    private lateinit var currentWorkDateObject: WorkDates
    private val workDateExtras = mutableStateListOf<WorkDateExtras>()
    private val usedWorkDatesList = ArrayList<String>()

    private var regHours by mutableStateOf("")
    private var otHours by mutableStateOf("")
    private var dblOtHours by mutableStateOf("")
    private var statHours by mutableStateOf("")
    private var note by mutableStateOf("")

    private var historyRegHours = 0.0
    private var historyOtHours = 0.0
    private var historyDblOtHours = 0.0
    private var workOrderSummary by mutableStateOf("")
    private val histories = mutableStateListOf<WorkOrderHistoryWithDates>()

    private val df = DateFunctions()
    private val nf = NumberFunctions()
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        payDayViewModel = mainActivity.payDayViewModel
        workExtraViewModel = mainActivity.workExtraViewModel
        workOrderViewModel = mainActivity.workOrderViewModel

        populateValues()

        return ComposeView(requireContext()).apply {
            setContent {
                WorkDateUpdateScreen(
                    dateText = displayDate,
                    onDateClick = { changeDate() },
                    regHours = regHours,
                    onRegHoursChange = { regHours = it },
                    otHours = otHours,
                    onOtHoursChange = { otHours = it },
                    dblOtHours = dblOtHours,
                    onDblOtHoursChange = { dblOtHours = it },
                    statHours = statHours,
                    onStatHoursChange = { statHours = it },
                    onStatHoursLongClick = { setStatHoursEstimate() },
                    note = note,
                    onNoteChange = { note = it },
                    onUpdateTimeClick = { updateWorkDate(FRAG_WORK_DATE_TIME) },
                    onAddHistoryClick = { validateWorkDateToSave(FRAG_WORK_ORDER_HISTORY_ADD) },
                    onTransferClick = { transferWorkOrderTotals() },
                    onDoneClick = { validateWorkDateToSave(FRAG_TIME_SHEET) },
                    histories = histories,
                    onHistoryClick = { gotoWorkOrderHistoryUpdate(it) },
                    onHistoryLongClick = { chooseOptions(it) },
                    workOrderSummary = workOrderSummary,
                    extras = workDateExtras,
                    onExtraClick = { toggleExtra(it) },
                    onExtraEditClick = { gotoUpdateWorkDateExtra(it) },
                    onAddExtraClick = { gotoWorkDateExtraAdd() }
                )
            }
        }
    }

    private fun populateValues() {
        mainViewModel.getWorkDateObject()?.let { workDate ->
            currentWorkDateObject = workDate
            curDateString = workDate.wdDate
            displayDate = df.getDisplayDate(curDateString)
            mainViewModel.setWorkDateString(curDateString)

            regHours = nf.getNumberFromDouble(workDate.wdRegHours)
            otHours = nf.getNumberFromDouble(workDate.wdOtHours)
            dblOtHours = nf.getNumberFromDouble(workDate.wdDblOtHours)
            statHours = nf.getNumberFromDouble(workDate.wdStatHours)
            note = workDate.wdNote ?: ""

            populateUsedWorkDateList()
            observeWorkOrderHistory()
            observeExtras()

            mainScope.launch {
                delay(WAIT_250)
                updateWorkDateTotals()
            }
        }
    }

    private fun populateUsedWorkDateList() {
        payDayViewModel.getWorkDateList(
            currentWorkDateObject.wdEmployerId, currentWorkDateObject.wdCutoffDate
        ).observe(viewLifecycleOwner) { list ->
            usedWorkDatesList.clear()
            list.forEach { usedWorkDatesList.add(it.wdDate) }
        }
    }

    private fun observeWorkOrderHistory() {
        workOrderViewModel.getWorkOrderHistoriesByDate(
            currentWorkDateObject.workDateId
        ).observe(viewLifecycleOwner) { list ->
            histories.clear()
            histories.addAll(list)

            historyRegHours = 0.0
            historyOtHours = 0.0
            historyDblOtHours = 0.0

            list.forEach {
                historyRegHours += it.history.woHistoryRegHours
                historyOtHours += it.history.woHistoryOtHours
                historyDblOtHours += it.history.woHistoryDblOtHours
            }

            updateWorkOrderSummaryText()
        }
    }

    private fun updateWorkOrderSummaryText() {
        val display = buildString {
            if (historyRegHours != 0.0) {
                append(getString(R.string.reg_))
                append(nf.getNumberFromDouble(historyRegHours))
            }
            if (historyOtHours != 0.0) {
                if (isNotEmpty()) append(getString(R.string.pipe))
                append(getString(R.string.ot_))
                append(nf.getNumberFromDouble(historyOtHours))
            }
            if (historyDblOtHours != 0.0) {
                if (isNotEmpty()) append(getString(R.string.pipe))
                append(getString(R.string.dbl_ot_))
                append(nf.getNumberFromDouble(historyDblOtHours))
            }
        }
        workOrderSummary = display
    }

    private fun observeExtras() {
        val currentExtras = mutableListOf<WorkDateExtras>()
        payDayViewModel.getWorkDateExtras(currentWorkDateObject.workDateId)
            .observe(viewLifecycleOwner) { extras ->
                currentExtras.clear()
                currentExtras.addAll(extras)
                refreshExtrasList(currentExtras)
            }

        mainScope.launch {
            delay(WAIT_250)
            workExtraViewModel.getExtraTypesAndDefByDaily(
                currentWorkDateObject.wdEmployerId, currentWorkDateObject.wdCutoffDate
            ).observe(viewLifecycleOwner) { extras ->
                extras.forEach { typeDef ->
                    val found = currentExtras.any { it.wdeName == typeDef.extraType.wetName }
                    if (!found) {
                        val tempExtra = WorkDateExtras(
                            0,
                            currentWorkDateObject.workDateId,
                            null,
                            typeDef.extraType.wetName,
                            typeDef.extraType.wetAppliesTo,
                            typeDef.extraType.wetAttachTo,
                            typeDef.definition.weValue,
                            typeDef.definition.weIsFixed,
                            typeDef.extraType.wetIsCredit,
                            true,
                            df.getCurrentTimeAsString()
                        )
                        currentExtras.add(tempExtra)
                    }
                }
                refreshExtrasList(currentExtras)
            }
        }
    }

    private fun refreshExtrasList(list: List<WorkDateExtras>) {
        workDateExtras.clear()
        workDateExtras.addAll(list.sortedBy { it.wdeName })
    }

    private fun toggleExtra(extra: WorkDateExtras) {
        if (!extra.wdeIsDeleted) {
            payDayViewModel.deleteWorkDateExtra(
                extra.wdeName, extra.wdeWorkDateId, extra.wdeUpdateTime
            )
        } else {
            if (extra.workDateExtraId != 0L) {
                payDayViewModel.updateWorkDateExtra(
                    extra.copy(wdeIsDeleted = false, wdeUpdateTime = df.getCurrentTimeAsString())
                )
            } else {
                payDayViewModel.insertWorkDateExtra(
                    extra.copy(
                        workDateExtraId = nf.generateRandomIdAsLong(),
                        wdeIsDeleted = false,
                        wdeUpdateTime = df.getCurrentTimeAsString()
                    )
                )
            }
        }
    }

    private fun setStatHoursEstimate() {
        mainScope.launch {
            val holidayPayCalculator = HolidayPayCalculator(
                payDayViewModel, currentWorkDateObject.wdEmployerId, curDateString
            )
            delay(WAIT_1000)
            val stat = round(holidayPayCalculator.getStatHours() * 4) / 4
            statHours = nf.getNumberFromDouble(stat)
        }
    }

    private fun updateWorkDateTotals() {
        val curReg = regHours.toDoubleOrNull() ?: 0.0
        val curOt = otHours.toDoubleOrNull() ?: 0.0
        val curDbl = dblOtHours.toDoubleOrNull() ?: 0.0

        if (historyRegHours > curReg || historyOtHours > curOt || historyDblOtHours > curDbl) {
            transferWorkOrderTotals()
        }
    }

    private fun transferWorkOrderTotals() {
        regHours = nf.getNumberFromDouble(historyRegHours)
        otHours = nf.getNumberFromDouble(historyOtHours)
        dblOtHours = nf.getNumberFromDouble(historyDblOtHours)
    }

    private fun changeDate() {
        val curDateAll = curDateString.split("-")
        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                curDateString = "$year-${
                    month.toString().padStart(2, '0')
                }-${
                    dayOfMonth.toString().padStart(2, '0')
                }"
                displayDate = df.getDisplayDate(curDateString)
            }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
        )
        datePickerDialog.setTitle(getString(R.string.choose_a_work_date))
        datePickerDialog.show()
    }

    private fun validateWorkDateToSave(fragment: String) {
        var found = false
        if (curDateString != currentWorkDateObject.wdDate) {
            if (usedWorkDatesList.contains(curDateString)) {
                found = true
                confirmOverwriteUsedDate(fragment)
            }
        }
        if (!found) {
            updateWorkDate(fragment)
        }
    }

    private fun confirmOverwriteUsedDate(fragment: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.this_date_is_already_used))
            .setMessage(getString(R.string.would_you_like_to_replace_the_old_information_for_this_work_date))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                updateWorkDate(fragment)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun updateWorkDate(fragment: String) {
        payDayViewModel.updateWorkDate(getCurrentWorkDate())
        gotoFragment(fragment)
    }

    private fun getCurrentWorkDate(): WorkDates {
        return currentWorkDateObject.copy(
            wdDate = curDateString,
            wdRegHours = regHours.toDoubleOrNull() ?: 0.0,
            wdOtHours = otHours.toDoubleOrNull() ?: 0.0,
            wdDblOtHours = dblOtHours.toDoubleOrNull() ?: 0.0,
            wdStatHours = statHours.toDoubleOrNull() ?: 0.0,
            wdNote = note.ifBlank { null },
            wdIsDeleted = false,
            wdUpdateTime = df.getCurrentTimeAsString()
        )
    }

    private fun gotoFragment(fragment: String) {
        val navController = view?.findNavController() ?: return
        when (fragment) {
            FRAG_WORK_ORDER_HISTORY_ADD -> {
                mainViewModel.setWorkDateObject(getCurrentWorkDate())
                mainViewModel.setCallingFragment(TAG)
                navController.navigate(
                    WorkDateUpdateFragmentDirections.actionWorkDateUpdateFragmentToWorkOrderHistoryAddFragment()
                )
            }

            FRAG_TIME_SHEET -> {
                navController.navigate(
                    WorkDateUpdateFragmentDirections.actionGlobalTimeSheetFragment()
                )
            }

            FRAG_WORK_DATE_TIME -> {
                mainViewModel.setWorkDateObject(getCurrentWorkDate())
                mainViewModel.addCallingFragment(FRAG_WORK_DATE_UPDATE)
                navController.navigate(
                    WorkDateUpdateFragmentDirections.actionWorkDateUpdateFragmentToWorkDateTimes()
                )
            }
        }
    }

    fun gotoWorkOrderHistoryUpdate(history: WorkOrderHistoryWithDates) {
        mainViewModel.setWorkOrderHistory(history.history)
        view?.findNavController()?.navigate(
            WorkDateUpdateFragmentDirections.actionWorkDateUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    private fun chooseOptions(history: WorkOrderHistoryWithDates) {
        AlertDialog.Builder(requireContext())
            .setTitle(
                getString(R.string.choose_option_for_wo) + history.workOrder.woNumber +
                        getString(R.string._on_) + df.getDisplayDate(history.workDate.wdDate)
            )
            .setPositiveButton(getString(R.string.open_caps)) { _, _ ->
                gotoWorkOrderHistoryUpdate(history)
            }
            .setNegativeButton(getString(R.string.delete)) { _, _ ->
                confirmDeleteWorkOrderHistory(history)
            }
            .setNeutralButton(getString(R.string.cancel), null)
            .show()
    }

    private fun confirmDeleteWorkOrderHistory(history: WorkOrderHistoryWithDates) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.are_you_sure_you_want_to_delete_wo) + history.workOrder.woNumber)
            .setMessage(getString(R.string.this_cannot_be_undone))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteWorkOrderHistory(history.history.woHistoryId)
            }
            .setNeutralButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteWorkOrderHistory(historyId: Long) {
        mainScope.launch {
            workOrderViewModel.removeAllWorkPerformedFromWorkOderHistory(historyId)
            workOrderViewModel.removeAllMaterialsFromWorkOrderHistory(historyId)
            delay(WAIT_500)
            workOrderViewModel.deleteWorkOrderHistory(historyId)
        }
    }

    fun gotoUpdateWorkDateExtra(extra: WorkDateExtras) {
        mainViewModel.setWorkDateExtra(extra)
        mainViewModel.setWorkDateExtraList(workDateExtras.toCollection(ArrayList()))
        view?.findNavController()?.navigate(
            WorkDateUpdateFragmentDirections.actionWorkDateUpdateFragmentToWorkDateExtraUpdateFragment()
        )
    }

    private fun gotoWorkDateExtraAdd() {
        mainViewModel.setWorkDateObject(getCurrentWorkDate())
        mainViewModel.setCallingFragment(TAG)
        view?.findNavController()?.navigate(
            WorkDateUpdateFragmentDirections.actionWorkDateUpdateFragmentToWorkDateExtraAddFragment()
        )
    }

    override fun onDestroy() {
        mainScope.cancel()
        super.onDestroy()
    }
}