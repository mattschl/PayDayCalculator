package ms.mattschlenkrich.paydaycalculator.ui.paydays

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
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.WorkDateExtraAdapter
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.common.WAIT_500
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentWorkDateAddBinding
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import java.time.LocalDate

//private const val TAG = "WorkDateAdd"

class WorkDateAddFragment : Fragment(R.layout.fragment_work_date_add) {

    private var _binding: FragmentWorkDateAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var curDateString: String
    private val workExtrasDefaultList = ArrayList<WorkExtraTypes>()
    private var payPeriod: PayPeriods? = null
    private val df = DateFunctions()
    private val cf = NumberFunctions()

    private val usedWorkDatesList = ArrayList<String>()

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
        setMenuActions()
        populateExtras()
        setClickActions()
    }

    private fun populateValues() {
        if (mainActivity.mainViewModel.getPayPeriod() != null) {
            payPeriod = mainActivity.mainViewModel.getPayPeriod()
            mainActivity.payDayViewModel.getWorkDateList(
                payPeriod!!.ppEmployerId,
                payPeriod!!.ppCutoffDate
            ).observe(viewLifecycleOwner) { list ->
                usedWorkDatesList.clear()
                list.listIterator().forEach {
                    usedWorkDatesList.add(it.wdDate)
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                populateDate()
            }
        }
    }

    private fun populateDate() {
        curDateString = LocalDate.now().toString()
        for (date in usedWorkDatesList) {
            if (curDateString == date) {
                curDateString = LocalDate.parse(curDateString).plusDays(1L).toString()
            }
        }
        binding.tvWorkDate.text = df.getDisplayDate(curDateString)
    }

    private fun setMenuActions() {
        mainActivity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        validateWorkDateToSave()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun populateExtras() {
        binding.apply {
            val extraAdapter = WorkDateExtraAdapter(
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

    fun addToExtraList(include: Boolean, extraType: WorkExtraTypes) {
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
        binding.apply {
            fabAddExtra.setOnClickListener {
                gotoAddExtra()
            }
            tvWorkDate.setOnClickListener {

            }
        }
    }

    private fun gotoAddExtra() {
        askUserToSaveNowOrAddExtras()
    }

    private fun gotoTimeSheet() {
        mView.findNavController().navigate(
            WorkDateAddFragmentDirections
                .actionGlobalTimeSheetFragment()
        )
    }

    private fun validateWorkDateToSave() {
        var found = false
        if (usedWorkDatesList.isEmpty()) {
            askUserToSaveNowOrAddExtras()
        } else {
            for (date in usedWorkDatesList) {
                if (date == curDateString) {
                    found = true
                    askUserToOverwriteUsedDate()
                }
            }
            if (!found) {
                askUserToSaveNowOrAddExtras()
            }
        }
    }

    private fun askUserToOverwriteUsedDate() {
        AlertDialog.Builder(mView.context)
            .setTitle("This date is already used")
            .setMessage(
                "Would you like to REPLACE the old information for " +
                        "this work date?"
            )
            .setPositiveButton("Yes") { _, _ ->
                saveWorkDate(true)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun askUserToSaveNowOrAddExtras() {
        AlertDialog.Builder(requireContext())
            .setTitle("Finish adding work date")
            .setMessage("Would you like to save this date or add extras?")
            .setPositiveButton("Save Now") { _, _ ->
                saveWorkDate(true)
            }
            .setNeutralButton("Save and add extras") { _, _ ->
                saveWorkDate(false)
            }
            .setNegativeButton("Abort", null)
            .show()
    }

    fun saveWorkDate(goBack: Boolean) {
        val workDate = getCurWorkDate()
        mainActivity.payDayViewModel.insertWorkDate(workDate)
        saveExtras(workDate)
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_500)
            if (goBack) {
                gotoTimeSheet()
            } else {
                gotoWorkDateUpdate(workDate)
            }
        }
    }

    private fun saveExtras(workDate: WorkDates) {
        for (extraType in workExtrasDefaultList) {
            mainActivity.workExtraViewModel.getExtraTypeAndDefByTypeId(
                extraType.workExtraTypeId, workDate.wdCutoffDate
            ).observe(viewLifecycleOwner) { extra ->
                mainActivity.payDayViewModel.insertWorkDateExtra(
                    WorkDateExtras(
                        cf.generateRandomIdAsLong(),
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

    private fun gotoWorkDateUpdate(workDate: WorkDates) {
        mainActivity.mainViewModel.setWorkDateObject(workDate)
        mView.findNavController().navigate(
            WorkDateAddFragmentDirections
                .actionWorkDateAddFragmentToWorkDateUpdateFragment()
        )
    }

    private fun getCurWorkDate(): WorkDates {
        binding.apply {
            return WorkDates(
                cf.generateRandomIdAsLong(),
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}