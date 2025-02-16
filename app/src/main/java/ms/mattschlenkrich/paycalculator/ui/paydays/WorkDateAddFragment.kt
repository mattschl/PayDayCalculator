package ms.mattschlenkrich.paycalculator.ui.paydays

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_TIME_SHEET
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_ADD
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_EXTRA_ADD
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_DATE_UPDATE
import ms.mattschlenkrich.paycalculator.common.FRAG_WORK_ORDER_HISTORY_ADD
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.databinding.FragmentWorkDateAddBinding
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.paydays.adapter.WorkDateDefaultExtraAdapter
import java.time.LocalDate

private const val TAG = FRAG_WORK_DATE_ADD

class WorkDateAddFragment : Fragment(R.layout.fragment_work_date_add), IWorkDateAddFragment {

    private var _binding: FragmentWorkDateAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curDateString: String
    private val workExtrasDefaultList = ArrayList<WorkExtraTypes>()
    private var payPeriod: PayPeriods? = null
    private val workDatesList = ArrayList<WorkDates>()
    private val usedWorkDatesList = ArrayList<WorkDates>()
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkDateAddBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.add_a_new_work_date)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateValues()
        setClickActions()
    }

    private fun populateValues() {
        if (mainActivity.mainViewModel.getPayPeriod() != null) {
            payPeriod = mainActivity.mainViewModel.getPayPeriod()
            populateWorkDateLists()
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                populateDate()
                populateExtras()
            }
        }
    }

    private fun populateWorkDateLists() {
        mainActivity.payDayViewModel.getWorkDateListUsed(
            payPeriod!!.ppEmployerId,
            payPeriod!!.ppCutoffDate
        ).observe(viewLifecycleOwner) { list ->
            usedWorkDatesList.clear()
            workDatesList.clear()
            list.listIterator().forEach {
                if (!it.wdIsDeleted) {
                    workDatesList.add(it)
                }
                usedWorkDatesList.add(it)
            }
        }
    }

    private fun populateDate() {
        curDateString = LocalDate.now().toString()
        for (date in workDatesList) {
            if (curDateString == date.wdDate) {
                curDateString = LocalDate.parse(curDateString).plusDays(1L).toString()
            }
        }
        binding.tvWorkDate.text = df.getDisplayDate(curDateString)
    }

    private fun populateExtras() {
        binding.apply {
            val extraAdapter = WorkDateDefaultExtraAdapter(
                mainActivity, mView, this@WorkDateAddFragment
            )
            rvExtras.apply {
                layoutManager = LinearLayoutManager(mView.context)
                adapter = extraAdapter
            }
            activity?.let {
                mainActivity.workExtraViewModel.getExtraTypesByDaily(
                    payPeriod!!.ppEmployerId
                ).observe(viewLifecycleOwner) { extras ->
                    extraAdapter.differ.submitList(extras)
                    extras.listIterator().forEach { extra ->
                        if (extra.wetIsDefault) {
                            workExtrasDefaultList.add(extra)
                        }
                    }
                    updateExtraUI(extras)
                }
            }
        }
    }

    private fun updateExtraUI(extras: List<Any>) {
        binding.apply {
            if (extras.isEmpty()) {
                rvExtras.visibility = View.GONE
            } else {
                rvExtras.visibility = View.VISIBLE
            }
        }
    }

    override fun addToExtraList(include: Boolean, extraType: WorkExtraTypes) {
        if (include) {
            workExtrasDefaultList.add(extraType)
        } else {
            workExtrasDefaultList.remove(extraType)
        }
    }

    private fun changeDate() {
        binding.apply {
            val curDateAll = curDateString.split("-")
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val month = monthOfYear + 1
                    val display = "$year-${
                        month.toString()
                            .padStart(2, '0')
                    }-${
                        dayOfMonth.toString().padStart(2, '0')
                    }"
                    curDateString = display
                    tvWorkDate.text = df.getDisplayDate(curDateString)

                },
                curDateAll[0].toInt(),
                curDateAll[1].toInt() - 1,
                curDateAll[2].toInt()
            )
            datePickerDialog.setTitle(getString(R.string.choose_a_work_date))
            datePickerDialog.show()
        }
    }

    private fun setClickActions() {
        setMenuActions()
        binding.apply {
            fabAddExtra.setOnClickListener {
                validateWorkDateToSave(FRAG_WORK_DATE_EXTRA_ADD)
            }
            tvWorkDate.setOnClickListener {
                changeDate()
            }
            fabAddWorkOrder.setOnClickListener {
                validateWorkDateToSave(FRAG_WORK_ORDER_HISTORY_ADD)
            }
        }
    }

    private fun setMenuActions() {
        mainActivity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        validateWorkDateToSave(FRAG_TIME_SHEET)
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun confirmSaveNow(fragment: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.finish_adding_work_date))
            .setMessage(getString(R.string.would_you_like_to_save_this_date_now_and_continue))
            .setPositiveButton(getString(R.string.save_now)) { _, _ ->
                saveWorkDate(fragment)
            }
            .setNegativeButton(getString(R.string.go_back), null)
            .show()
    }

    override fun validateWorkDateToSave(fragment: String) {
        var found = false
        if (usedWorkDatesList.isEmpty()) {
            confirmSaveNow(fragment)
        } else {
            for (date in usedWorkDatesList) {
                if (date.wdDate == curDateString) {
                    found = true
                    confirmOverwriteUsedDate(date, fragment)
                }
            }
            if (!found) {
                confirmSaveNow(fragment)
            }
        }
    }

    private fun getCurWorkDate(): WorkDates {
        binding.apply {
            return WorkDates(
                nf.generateRandomIdAsLong(),
                payPeriod!!.payPeriodId,
                payPeriod!!.ppEmployerId,
                payPeriod!!.ppCutoffDate,
                curDateString,
                if (etHours.text.isNullOrBlank()) 0.0 else etHours.text.toString().trim()
                    .toDouble(),
                if (etOt.text.isNullOrBlank()) 0.0 else etOt.text.toString().trim()
                    .toDouble(),
                if (etDblOt.text.isNullOrBlank()) 0.0 else etDblOt.text.toString().trim()
                    .toDouble(),
                if (etStat.text.isNullOrBlank()) 0.0 else etStat.text.toString().trim()
                    .toDouble(),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun saveWorkDate(goBackTo: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val workDate = getCurWorkDate()
            mainActivity.payDayViewModel.insertWorkDate(workDate)
            mainActivity.mainViewModel.setWorkDateObject(workDate)
            delay(WAIT_250)
            saveExtras(workDate)
            goBackToFragment(goBackTo, workDate)
        }
    }

    private fun confirmOverwriteUsedDate(date: WorkDates, fragment: String) {
        AlertDialog.Builder(mView.context)
            .setTitle(getString(R.string.this_date_is_already_used))
            .setMessage(
                getString(R.string.would_you_like_to_replace_the_old_information_for_this_work_date)
            )
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                overWriteWorkDate(date, fragment)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun overWriteWorkDate(date: WorkDates, goBackTo: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val workDate = getUpdatedWorkDate(date)
            mainActivity.payDayViewModel.updateWorkDate(workDate)
            mainActivity.mainViewModel.setWorkDateObject(workDate)
            delay(WAIT_250)
            saveExtras(workDate)
            goBackToFragment(goBackTo, workDate)
        }
    }

    private fun goBackToFragment(
        goBackTo: String,
        workDate: WorkDates
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_500)
            if (goBackTo == FRAG_TIME_SHEET) {
                gotoTimeSheetFragment()
            }
            if (goBackTo == FRAG_WORK_DATE_UPDATE) {
                gotoWorkDateUpdate(workDate)
            }
            if (goBackTo == FRAG_WORK_ORDER_HISTORY_ADD) {
                gotoTimeSheetAddWorkOrder(workDate)
            }
        }
    }

    private fun getUpdatedWorkDate(date: WorkDates): WorkDates {
        val tempWorkDate = getCurWorkDate()
        return WorkDates(
            date.workDateId,
            tempWorkDate.wdPayPeriodId,
            tempWorkDate.wdEmployerId,
            tempWorkDate.wdCutoffDate,
            tempWorkDate.wdDate,
            tempWorkDate.wdRegHours,
            tempWorkDate.wdOtHours,
            tempWorkDate.wdDblOtHours,
            tempWorkDate.wdStatHours,
            false,
            df.getCurrentTimeAsString()
        )
    }

    private fun saveExtras(workDate: WorkDates) {
        for (extraType in workExtrasDefaultList) {
            mainActivity.workExtraViewModel.getExtraTypeAndDefByTypeId(
                extraType.workExtraTypeId, workDate.wdCutoffDate
            ).observe(viewLifecycleOwner) { extra ->
                mainActivity.payDayViewModel.insertWorkDateExtra(
                    WorkDateExtras(
                        nf.generateRandomIdAsLong(),
                        workDate.workDateId,
                        extra.extraType.workExtraTypeId,
                        extra.extraType.wetName,
                        extra.extraType.wetAppliesTo,
                        extra.extraType.wetAttachTo,
                        extra.definition.weValue,
                        extra.definition.weIsFixed,
                        extra.extraType.wetIsCredit,
                        false,
                        df.getCurrentTimeAsString()
                    )
                )
            }
        }
    }

    private fun gotoTimeSheetAddWorkOrder(workDate: WorkDates) {
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mainActivity.mainViewModel.setWorkDateObject(workDate)
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_100)
            gotoWorkOrderHistoryAddFragment()
        }
    }

    private fun gotoWorkOrderHistoryAddFragment() {
        mView.findNavController().navigate(
            WorkDateAddFragmentDirections
                .actionWorkDateAddFragmentToWorkOrderHistoryAddFragment()
        )
    }

    private fun gotoWorkDateUpdate(workDate: WorkDates) {
        mainActivity.mainViewModel.setWorkDateObject(workDate)
        gotoWorkDateUpdateFragment()
    }

    private fun gotoWorkDateUpdateFragment() {
        mView.findNavController().navigate(
            WorkDateAddFragmentDirections
                .actionWorkDateAddFragmentToWorkDateUpdateFragment()
        )
    }

    private fun gotoTimeSheetFragment() {
        mView.findNavController().navigate(
            WorkDateAddFragmentDirections
                .actionGlobalTimeSheetFragment()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}