package ms.mattschlenkrich.paycalculator.ui.workdate

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_TIME_SHEET
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_UPDATE
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paycalculator.common.HolidayPayCalculator
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_1000
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkOrderViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkDateUpdateBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.timesheet.timesheetadapter.WorkDateUpdateCustomExtraAdapter
import ms.mattschlenkrich.paycalculator.ui.workdate.adapter.WorkDateWorkOrderHistoryAdapter
import kotlin.math.round

private const val TAG = FRAG_WORK_DATE_UPDATE

class WorkDateUpdateFragment : Fragment(
    R.layout.fragment_work_date_update
) {

    private var _binding: FragmentWorkDateUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var payDayViewModel: PayDayViewModel
    private lateinit var workExtraViewModel: WorkExtraViewModel
    private lateinit var workOrderViewModel: WorkOrderViewModel
    private lateinit var curDateString: String
    private lateinit var currentWorkDateObject: WorkDates
    private val workDateExtras = ArrayList<WorkDateExtras>()
    private val customWorkDateExtras = ArrayList<WorkDateExtras>()
    private val usedWorkDatesList = ArrayList<String>()
    private var regHours = 0.0
    private var otHours = 0.0
    private var dblOtHours = 0.0
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkDateUpdateBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        payDayViewModel = mainActivity.payDayViewModel
        workExtraViewModel = mainActivity.workExtraViewModel
        workOrderViewModel = mainActivity.workOrderViewModel
        mainActivity.title = getString(R.string.update_this_work_date)
        return mView
    }

    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
        mainScope.launch {
            delay(WAIT_250)
            updateWorkDateTotals()
        }
    }

    private fun populateValues() {
        if (mainViewModel.getWorkDateObject() != null) {
            currentWorkDateObject = mainViewModel.getWorkDateObject()!!
            curDateString = currentWorkDateObject.wdDate
            mainViewModel.setWorkDateString(curDateString)
            populateUsedWorkDateList()
            binding.apply {
                tvWorkDate.text = df.getDisplayDate(currentWorkDateObject.wdDate)
                etHours.setText(
                    nf.getNumberFromDouble(currentWorkDateObject.wdRegHours)
                )
                etOt.setText(
                    nf.getNumberFromDouble(currentWorkDateObject.wdOtHours)
                )
                etDblOt.setText(
                    nf.getNumberFromDouble(currentWorkDateObject.wdDblOtHours)
                )
                etStat.setText(
                    nf.getNumberFromDouble(currentWorkDateObject.wdStatHours)
                )
                etNote.setText(currentWorkDateObject.wdNote)
            }
            populateExtras()
            populateWorkOrderHistory()
        }
    }

    private fun populateUsedWorkDateList() {
        payDayViewModel.getWorkDateList(
            currentWorkDateObject.wdEmployerId, currentWorkDateObject.wdCutoffDate
        ).observe(viewLifecycleOwner) { list ->
            usedWorkDatesList.clear()
            list.listIterator().forEach {
                usedWorkDatesList.add(it.wdDate)
            }
        }
    }

    private fun populateWorkOrderHistory() {
        binding.apply {
            workOrderViewModel.getWorkOrderHistoriesByDate(
                currentWorkDateObject.workDateId
            ).observe(viewLifecycleOwner) { list ->
                if (list.isNotEmpty()) {
                    btnTransfer.visibility = View.VISIBLE
                    tvWorkOrderSummary.visibility = View.VISIBLE
                } else {
                    btnTransfer.visibility = View.GONE
                    tvWorkOrderSummary.visibility = View.GONE
                }
                val workOrderAdapter = WorkDateWorkOrderHistoryAdapter(
                    list as ArrayList, mainActivity, mView,
                    this@WorkDateUpdateFragment,
                )
                rvHistory.apply {
                    layoutManager = LinearLayoutManager(mView.context)
                    adapter = workOrderAdapter
                }
                regHours = 0.0
                otHours = 0.0
                dblOtHours = 0.0

                list.listIterator().forEach {
                    regHours += it.history.woHistoryRegHours
                    otHours += it.history.woHistoryOtHours
                    dblOtHours += it.history.woHistoryDblOtHours
                }
                var display = ""
                if (regHours != 0.0) {
                    display += getString(R.string.reg_) + nf.getNumberFromDouble(regHours)
                }
                if (otHours != 0.0) {
                    if (display != "") {
                        display += getString(R.string.pipe)
                    }
                    display += getString(R.string.ot_) + nf.getNumberFromDouble(otHours)
                }
                if (dblOtHours != 0.0) {
                    if (display != "") {
                        display += getString(R.string.pipe)
                    }
                    display += getString(R.string.dbl_ot_) + nf.getNumberFromDouble(dblOtHours)
                }
                tvWorkOrderSummary.text = display
            }
        }
    }

    fun populateExtras() {
        payDayViewModel.getWorkDateExtras(currentWorkDateObject.workDateId)
            .observe(viewLifecycleOwner) { extras ->
                workDateExtras.clear()
                customWorkDateExtras.clear()
                extras.listIterator().forEach {
                    workDateExtras.add(it)
                    customWorkDateExtras.add(it)
                }
            }
        mainScope.launch {
            delay(WAIT_250)
            binding.apply {
                activity?.let {
                    workExtraViewModel.getExtraTypesAndDefByDaily(
                        currentWorkDateObject.wdEmployerId, currentWorkDateObject.wdCutoffDate
                    ).observe(viewLifecycleOwner) { extras ->
                        extras.listIterator().forEach {
                            val tempExtra = WorkDateExtras(
                                0,
                                currentWorkDateObject.workDateId,
                                null,
                                it.extraType.wetName,
                                it.extraType.wetAppliesTo,
                                it.extraType.wetAttachTo,
                                it.definition.weValue,
                                it.definition.weIsFixed,
                                it.extraType.wetIsCredit,
                                true,
                                df.getCurrentTimeAsString()
                            )
                            var found = false
                            for (oldExtra in workDateExtras) {
                                if (oldExtra.wdeName == it.extraType.wetName) {
                                    found = true
                                    break
                                }
                            }
                            if (!found) {
                                workDateExtras.add(tempExtra)
                            }

                        }
                        workDateExtras.sortBy { extra ->
                            extra.wdeName
                        }
                        val extraAdapter = WorkDateUpdateCustomExtraAdapter(
                            workDateExtras, mainActivity, mView,
                            this@WorkDateUpdateFragment,
                        )
                        rvExtras.apply {
                            layoutManager = LinearLayoutManager(mView.context)
                            adapter = extraAdapter
                        }
                    }

                }
            }
        }
    }

    private fun setClickActions() {
        binding.apply {
            fabDone.setOnClickListener {
                validateWorkDateToSave(FRAG_TIME_SHEET)
            }
            fabAddExtra.setOnClickListener {
                gotoWorkDateExtraAdd()
            }
            tvWorkDate.setOnClickListener {
                changeDate()
            }
            fabAddWorkOrder.setOnClickListener {
                validateWorkDateToSave(FRAG_WORK_ORDER_HISTORY_ADD)
            }
            btnTransfer.setOnClickListener {
                transferWorkOrderTotals()
            }
            lblStat.setOnClickListener {
                setStatHoursEstimate()
            }
        }
    }

    private fun setStatHoursEstimate() {
        binding.apply {
            etStat.setText("")
            mainScope.launch {
                val holidayPayCalculator = HolidayPayCalculator(
                    mainActivity, currentWorkDateObject.wdEmployerId, curDateString
                )
                delay(WAIT_1000)
                val statHours = round(holidayPayCalculator.getStatHours() * 4) / 4
                etStat.setText(nf.getNumberFromDouble(statHours))

            }
        }
    }

    private fun updateWorkDateTotals() {
        if (regHours > currentWorkDateObject.wdRegHours || otHours > currentWorkDateObject.wdOtHours || dblOtHours > currentWorkDateObject.wdDblOtHours) {
            transferWorkOrderTotals()
        }
    }

    private fun transferWorkOrderTotals() {
        binding.apply {
            etHours.setText(
                nf.getNumberFromDouble(regHours)
            )
            etOt.setText(
                nf.getNumberFromDouble(otHours)
            )
            etDblOt.setText(
                nf.getNumberFromDouble(dblOtHours)
            )
        }
    }

    private fun changeDate() {
        binding.apply {
            val curDateAll = curDateString.split("-")
            val datePickerDialog = DatePickerDialog(
                requireContext(), { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${
                        month.toString().padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    curDateString = display
                    tvWorkDate.text = df.getDisplayDate(display)

                }, curDateAll[0].toInt(), curDateAll[1].toInt() - 1, curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(getString(R.string.choose_a_work_date))
            datePickerDialog.show()
        }
    }

    private fun updateWorkDate(fragment: String) {
        payDayViewModel.updateWorkDate(getCurrentWorkDate())
        gotoFragment(fragment)
    }

    private fun gotoTimeSheetFragment() {
        mView.findNavController().navigate(
            WorkDateUpdateFragmentDirections.actionGlobalTimeSheetFragment()
        )
    }

    private fun validateWorkDateToSave(fragment: String) {
        var found = false
        if (curDateString != currentWorkDateObject.wdDate) {
            for (date in usedWorkDatesList) {
                if (date == curDateString) {
                    found = true
                    confirmOverwriteUsedDate(fragment)
                }
            }
        }
        if (!found) {
            updateWorkDate(fragment)
        }
    }

    private fun confirmOverwriteUsedDate(fragment: String) {
        AlertDialog.Builder(mView.context).setTitle(getString(R.string.this_date_is_already_used))
            .setMessage(
                getString(R.string.would_you_like_to_replace_the_old_information_for_this_work_date)
            ).setPositiveButton(getString(R.string.yes)) { _, _ ->
                updateWorkDate(fragment)
            }.setNegativeButton(getString(R.string.no), null).show()
    }

    private fun getCurrentWorkDate(): WorkDates {
        binding.apply {
            currentWorkDateObject.apply {
                return WorkDates(
                    workDateId,
                    wdPayPeriodId,
                    wdEmployerId,
                    wdCutoffDate,
                    curDateString,
                    if (etHours.text.isNullOrBlank()) 0.0 else etHours.text.toString().trim()
                        .toDouble(),
                    if (etOt.text.isNullOrBlank()) 0.0 else etOt.text.toString().trim().toDouble(),
                    if (etDblOt.text.isNullOrBlank()) 0.0 else etDblOt.text.toString().trim()
                        .toDouble(),
                    if (etStat.text.isNullOrBlank()) 0.0 else etStat.text.toString().trim()
                        .toDouble(),
                    if (etNote.text.isNullOrBlank()) null else etNote.text.toString().trim(),
                    false,
                    df.getCurrentTimeAsString()
                )
            }
        }
    }

    private fun gotoFragment(fragment: String) {
        if (fragment == FRAG_WORK_ORDER_HISTORY_ADD) {
            gotoWorkOrderHistoryAdd()
        } else if (fragment == FRAG_TIME_SHEET) {
            gotoTimeSheetFragment()
        }
    }

    private fun gotoWorkOrderHistoryAdd() {
        mainViewModel.apply {
            setWorkDateObject(getCurrentWorkDate())
            setCallingFragment(TAG)
        }
        gotoWorkOrderHistoryAddFragment()
    }

    private fun gotoWorkOrderHistoryAddFragment() {
        mView.findNavController().navigate(
            WorkDateUpdateFragmentDirections.actionWorkDateUpdateFragmentToWorkOrderHistoryAddFragment()
        )
    }

    private fun gotoWorkDateExtraAdd() {
        mainViewModel.apply {
            setWorkDateObject(getCurrentWorkDate())
            setCallingFragment(TAG)
        }
        gotoWorkDateExtraAddFragment()
    }

    private fun gotoWorkDateExtraAddFragment() {
        mView.findNavController().navigate(
            WorkDateUpdateFragmentDirections.actionWorkDateUpdateFragmentToWorkDateExtraAddFragment()
        )
    }

    fun gotoWorkDateExtraUpdateFragment() {
        mView.findNavController().navigate(
            WorkDateUpdateFragmentDirections.actionWorkDateUpdateFragmentToWorkDateExtraUpdateFragment()

        )
    }

    fun gotoWorkOrderHistoryUpdateFragment() {
        mView.findNavController().navigate(
            WorkDateUpdateFragmentDirections.actionWorkDateUpdateFragmentToWorkOrderHistoryUpdateFragment()
        )
    }

    override fun onStop() {
        mainScope.cancel()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}