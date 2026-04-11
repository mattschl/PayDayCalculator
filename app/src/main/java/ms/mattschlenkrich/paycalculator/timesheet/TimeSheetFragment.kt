package ms.mattschlenkrich.paycalculator.timesheet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.common.WAIT_1000
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.PayPeriods
import ms.mattschlenkrich.paycalculator.data.WorkDates
import ms.mattschlenkrich.paycalculator.databinding.FragmentTimeSheetBinding
import ms.mattschlenkrich.paycalculator.logic.PayCalculationsAsync
import ms.mattschlenkrich.paycalculator.logic.PayDateProjections
import java.time.LocalDate

//private const val TAG = FRAG_TIME_SHEET

class TimeSheetFragment : Fragment(R.layout.fragment_time_sheet), ITimeSheetFragment {

    private var _binding: FragmentTimeSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var payDayViewModel: PayDayViewModel
    private var curEmployer: Employers? = null
    private val cutOffs = ArrayList<String>()
    private var curPayPeriod: PayPeriods? = null
    private var curCutOff = ""
    private val projections = PayDateProjections()
    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private var workDateAdapter: WorkDateAdapter? = null
    private lateinit var payCalculations: PayCalculationsAsync
    private val defaultScope = CoroutineScope(Dispatchers.Default)
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private var valuesFilled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeSheetBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        employerViewModel = mainActivity.employerViewModel
        payDayViewModel = mainActivity.payDayViewModel
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (populateEmployers()) {
            setClickActions()
            populateFromHistory()
        }
    }

    private fun populateEmployers(): Boolean {
        val employerAdapter = ArrayAdapter<String>(
            mView.context, R.layout.spinner_item_bold
        )
        val employerList = employerViewModel.employerLogicViewModel.getEmployerList()
        if (employerList.isEmpty()) {
            mView.findNavController().navigate(
                TimeSheetFragmentDirections.actionTimeSheetFragmentToEmployerAddFragment()
            )
            return false
        } else {

            employerList.listIterator().forEach {
                employerAdapter.add(it.employerName)
            }
            curEmployer = employerList.first()
            employerAdapter.add(getString(R.string.add_new_employer))
            binding.spEmployers.adapter = employerAdapter
            return true
        }
    }

    private fun setClickActions() {
        binding.apply {
            onSelectEmployer()
            onSelectCutOffDate()
            fabAddDate.setOnClickListener {
                gotoWorkDateAdd()
            }
            crdPayDetails.setOnClickListener {
                gotoPayDetails()
            }
        }
    }


    private fun onSelectEmployer() {
        binding.apply {
            spEmployers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (spEmployers.selectedItem.toString() != getString(R.string.add_new_employer)) {
                        defaultScope.launch {
                            curEmployer = employerViewModel.findEmployer(
                                spEmployers.selectedItem.toString()
                            )
                        }
                        mainScope.launch {
                            delay(WAIT_100)
//                            mainViewModel.setEmployer(curEmployer)
                            mainActivity.topMenuBar.title =
                                getString(R.string.time_sheet) + " for " + spEmployers.selectedItem.toString()
                            populateCutOffDates()
                        }
                    } else {
                        gotoEmployerAdd()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
//                        fillEmployers()
                }
            }
        }
    }

    private fun generateNewCutOff() {
        val employer = curEmployer ?: return
        val nextCutOff = projections.generateNextCutOff(
            employer,
            if (cutOffs.isEmpty()) "" else cutOffs[0]
        )
        if (nextCutOff.isEmpty()) return
        payDayViewModel.insertPayPeriod(
            PayPeriods(
                nf.generateRandomIdAsLong(),
                nextCutOff,
                employer.employerId,
                false,
                df.getCurrentTimeAsString()
            )
        )
        populateCutOffDates()
    }

    private fun populateCutOffDates() {
        if (curEmployer != null) {
            binding.apply {
                fabAddDate.visibility = View.INVISIBLE
                val cutOffAdapter = ArrayAdapter<Any>(
                    mView.context, R.layout.spinner_item_bold
                )
                payDayViewModel.getCutOffDates(curEmployer!!.employerId).observe(
                    viewLifecycleOwner
                ) { dates ->
                    cutOffAdapter.clear()
                    cutOffs.clear()
                    cutOffAdapter.notifyDataSetChanged()
                    dates.listIterator().forEach {
                        cutOffAdapter.add(it.ppCutoffDate)
                        cutOffs.add(it.ppCutoffDate)
                    }
                    if (dates.isEmpty() || dates[0].ppCutoffDate < df.getCurrentDateAsString()) {
                        generateNewCutOff()
                    } else {
                        cutOffAdapter.add(getString(R.string.generate_a_new_cut_off))
                    }
                }
                spCutOff.adapter = cutOffAdapter
            }
        }
    }

    private fun onSelectCutOffDate() {
        binding.apply {
            spCutOff.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (spCutOff.adapter.count > 0) {
                        if (spCutOff.selectedItem.toString() != getString(R.string.generate_a_new_cut_off)) {
                            defaultScope.launch {
                                curCutOff = spCutOff.selectedItem.toString()
//                                if (valuesFilled) mainViewModel.setCutOffDate(curCutOff)
                                populatePayDayDate()
                                populatePayDetails()
                            }
                            mainScope.launch {
                                delay(WAIT_1000)
                                populateExistingWorkDates()
                            }
                        } else {
                            generateNewCutOff()
                        }
                    } else {
                        generateNewCutOff()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
//                        fillCutOffDates()*///-
                }
            }
        }
    }

    private fun populatePayDayDate() {
        if (curCutOff.isNotBlank() && curEmployer != null) {
            binding.apply {
                val display = getString(R.string.pay_day_is_) + df.getDisplayDate(
                    LocalDate.parse(curCutOff).plusDays(curEmployer!!.cutoffDaysBefore.toLong())
                        .toString()
                )
                tvPaySummary.text = display
            }
        }
    }

    private fun populateExistingWorkDates() {
        binding.apply {
            workDateAdapter = null
            workDateAdapter = WorkDateAdapter(
                mainActivity,
                curCutOff,
                curEmployer!!,
                payCalculations.getPayRate(),
                mView,
                this@TimeSheetFragment
            )
            rvDates.apply {
                layoutManager = LinearLayoutManager(mView.context)
                adapter = workDateAdapter
            }
            payDayViewModel.getWorkDateList(curEmployer!!.employerId, curCutOff)
                .observe(viewLifecycleOwner) { workDates ->
                    populateWeekTotals(workDates)
                    workDateAdapter!!.differ.submitList(workDates)
                    updateUI(workDates)
                }
        }
    }

    private fun populateWeekTotals(workDates: List<WorkDates>) {
        binding.apply {
            val week1EndDate = LocalDate.parse(curCutOff).minusDays(7).toString()
            var wk1RegHours = 0.0
            var wk1OtHours = 0.0
            var wk1DblOtHours = 0.0
            var wk1StatHours = 0.0
            var wk2RegHours = 0.0
            var wk2OtHours = 0.0
            var wk2DblOtHours = 0.0
            var wk2StatHours = 0.0
            for (wDate in workDates) {
                if (wDate.wdDate <= week1EndDate) {
                    wk1RegHours += wDate.wdRegHours
                    wk1OtHours += wDate.wdOtHours
                    wk1DblOtHours += wDate.wdDblOtHours
                    wk1StatHours += wDate.wdStatHours
                } else {
                    wk2RegHours += wDate.wdRegHours
                    wk2OtHours += wDate.wdOtHours
                    wk2DblOtHours += wDate.wdDblOtHours
                    wk2StatHours += wDate.wdStatHours
                }
            }
            var display = ""
            if (wk1RegHours != 0.0) {
                display += nf.getNumberFromDouble(wk1RegHours) + " " + getString(R.string.hr)
            }
            if (wk1OtHours != 0.0) {
                if (display != "") display += getString(R.string.pipe)
                display += nf.getNumberFromDouble(wk1OtHours) + " " + getString(R.string.ot)
            }
            if (wk1DblOtHours != 0.0) {
                if (display != "") display += getString(R.string.pipe)
                display += nf.getNumberFromDouble(wk1DblOtHours) + " " + getString(R.string.dbl_ot)
            }
            if (wk1StatHours != 0.0) {
                if (display != "") display += getString(R.string.pipe)
                display += nf.getNumberFromDouble(wk1StatHours) + " " + getString(R.string.other_hours)
            }
            display = getString(R.string.week_1_) +
                    if (display == "") getString(R.string._0_hr) else display
            tvWeek1.text = display
            display = ""
            if (wk2RegHours != 0.0) {
                display += nf.getNumberFromDouble(wk2RegHours) + " " + getString(R.string.hr)
            }
            if (wk2OtHours != 0.0) {
                if (display != "") display += getString(R.string.pipe)
                display += nf.getNumberFromDouble(wk2OtHours) + " " + getString(R.string.ot)
            }
            if (wk2DblOtHours != 0.0) {
                if (display != "") display += getString(R.string.pipe)
                display += nf.getNumberFromDouble(wk2DblOtHours) + " " + getString(R.string.dbl_ot)
            }
            if (wk2StatHours != 0.0) {
                if (display != "") display += getString(R.string.pipe)
                display += nf.getNumberFromDouble(wk2StatHours) + " " + getString(R.string.other_hours)
            }
            display = getString(R.string.week_2_) +
                    if (display == "") getString(R.string._0_hr) else display
            tvWeek2.text = display
        }
    }

    override fun populatePayDetails() {
        mainScope.launch {
            getSelectedPayPeriodObject()
            delay(WAIT_250)
            payCalculations = PayCalculationsAsync(
                mainActivity, curEmployer!!, curPayPeriod!!
            )
            delay(WAIT_1000)
            binding.apply {
                var display = nf.displayDollars(
                    -payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()
                )
                tvDeductions.text = display
                tvDeductions.setTextColor(Color.RED)
                display = getString(R.string.net_) + nf.displayDollars(
                    payCalculations.getPayGross() - payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()
                )
                tvNetPay.text = display
                display = nf.displayDollars(payCalculations.getPayGross())
                tvGrossPay.text = display
                display = ""
                if (payCalculations.getHoursReg() != 0.0) {
                    display =
                        getString(R.string.hours_) + nf.getNumberFromDouble(payCalculations.getHoursReg())
                }
                if (payCalculations.getPayOt() != 0.0) {
                    if (display.isNotBlank()) display += getString(R.string.pipe)
                    display += getString(R.string.ot_) + nf.getNumberFromDouble(payCalculations.getHoursOt())
                }
                if (payCalculations.getHoursDblOt() != 0.0) {
                    if (display.isNotBlank()) display += getString(R.string.pipe)
                    display += getString(R.string.dbl_ot_) + nf.getNumberFromDouble((payCalculations.getHoursDblOt()))
                }
                if (payCalculations.getHoursStat() != 0.0) {
                    if (display.isNotBlank()) display += getString(R.string.pipe)
                    display += getString(R.string.other_hours_) + nf.getNumberFromDouble(
                        payCalculations.getHoursStat()
                    )
                }
                display = getString(R.string.totals) + " " + display
                tvHours.text = display
                fabAddDate.visibility = View.VISIBLE
            }
        }
    }

    private fun populateFromHistory() {
//        if (!valuesFilled) {
        binding.apply {
            mainScope.launch {
                delay(WAIT_500)
                if (mainViewModel.getEmployer() != null) {
                    curEmployer = mainViewModel.getEmployer()!!
                    for (i in 0 until spEmployers.adapter.count) {
//                        Log.d(TAG, "populateFromHistory: " + curEmployer!!.employerName)
                        if (spEmployers.getItemAtPosition(i) == curEmployer!!.employerName) {
                            spEmployers.setSelection(i)
                            break
                        }
                    }
                    delay(WAIT_500)
                    if (mainViewModel.getCutOffDate() != null) {
                        curCutOff = mainViewModel.getCutOffDate()!!
                        for (i in 0 until spCutOff.adapter.count) {
                            if (spCutOff.getItemAtPosition(i).toString() == curCutOff) {
                                spCutOff.setSelection(i)
                                populatePayDetails()
                                break
                            }
                        }
                    }
                    valuesFilled = true
                }
            }
//            }
        }
    }

    private fun updateUI(workDates: List<Any>) {
        binding.apply {
            if (workDates.isEmpty()) {
                rvDates.visibility = View.GONE
            } else {
                rvDates.visibility = View.VISIBLE
            }
        }
    }

    private fun getSelectedPayPeriod(): PayPeriods? {
        val employer = curEmployer ?: return null
        binding.apply {
            if (curPayPeriod == null) {
                generateNewCutOff()
            }
            val selectedItem = spCutOff.selectedItem?.toString() ?: return null
            if (selectedItem == getString(R.string.generate_a_new_cut_off)) {
                return null
            }
            return PayPeriods(
                nf.generateRandomIdAsLong(),
                selectedItem,
                employer.employerId,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun getSelectedPayPeriodObject() {
        payDayViewModel.getPayPeriod(
            binding.spCutOff.selectedItem.toString(), curEmployer!!.employerId
        ).observe(viewLifecycleOwner) { payPeriod ->
            curPayPeriod = payPeriod
        }
        mainViewModel.setPayPeriod(curPayPeriod)

    }

    private fun gotoWorkDateAdd() {
        setCurrentVariables()
        gotoWorkDateAddFragment()
    }

    private fun gotoWorkDateAddFragment() {
        mView.findNavController().navigate(
            TimeSheetFragmentDirections.actionTimeSheetFragmentToWorkDateAddFragment()
        )
    }

    private fun gotoEmployerAdd() {
        setCurrentVariables()
        gotoEmployerAddFragment()
    }

    private fun gotoEmployerAddFragment() {
        mView.findNavController().navigate(
            TimeSheetFragmentDirections.actionTimeSheetFragmentToEmployerAddFragment()
        )
    }

    private fun gotoPayDetails() {
        mainScope.launch {
            setCurrentVariables()
            delay(WAIT_250)
            gotoPayDetailFragment()
        }
    }

    private fun gotoPayDetailFragment() {
        mView.findNavController().navigate(
            TimeSheetFragmentDirections.actionTimeSheetFragmentToPayDetailFragmentNew()
        )
    }

    override fun onStop() {
        setCurrentVariables()
        super.onStop()
    }

    private fun setCurrentVariables() {
        mainViewModel.apply {
            setPayPeriod(getSelectedPayPeriod())
            setEmployer(curEmployer)
            setCutOffDate(curCutOff)
//            Log.d(TAG, "setCurrentVariables: " + curEmployer?.employerName)
        }
    }

    override fun onDestroy() {
        mainScope.cancel()
        defaultScope.cancel()
        super.onDestroy()
        _binding = null
    }
}