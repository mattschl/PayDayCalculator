package ms.mattschlenkrich.paycalculator.ui.timesheet

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_TIME_SHEET
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.common.WAIT_1000
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paycalculator.databinding.FragmentTimeSheetBinding
import ms.mattschlenkrich.paycalculator.payfunctions.PayCalculationsAsync
import ms.mattschlenkrich.paycalculator.payfunctions.PayDateProjections
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.timesheet.adapter.WorkDateAdapter
import java.time.LocalDate

private const val TAG = FRAG_TIME_SHEET

class TimeSheetFragment :
    Fragment(R.layout.fragment_time_sheet),
    ITimeSheetFragment {

    private var _binding: FragmentTimeSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private var curEmployer: Employers? = null
    private val cutOffs = ArrayList<String>()
    private var curPayPeriod: PayPeriods? = null
    private var curCutOff = ""
    private val projections = PayDateProjections()
    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private var workDateAdapter: WorkDateAdapter? = null
    private lateinit var payCalculations: PayCalculationsAsync
    private var valuesFilled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeSheetBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateEmployers()
        setClickActions()
        populateFromHistory()
    }

    private fun populateEmployers() {
        val employerAdapter = ArrayAdapter<String>(
            mView.context,
            R.layout.spinner_item_bold
        )
        mainActivity.employerViewModel.getEmployers().observe(
            viewLifecycleOwner
        ) { employers ->
            employerAdapter.clear()
            employerAdapter.notifyDataSetChanged()
            employers.listIterator().forEach {
                employerAdapter.add(it.employerName)
            }
            employerAdapter.add(getString(R.string.add_new_employer))
            if (employers.isNotEmpty()) {
                curEmployer = employers.first()
            } else {
                gotoEmployerAdd()
            }
        }
        binding.spEmployers.adapter = employerAdapter
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
            spEmployers.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (spEmployers.selectedItem.toString() !=
                            getString(R.string.add_new_employer)
                        ) {
                            CoroutineScope(Dispatchers.Default).launch {
                                curEmployer = mainActivity.employerViewModel.findEmployer(
                                    spEmployers.selectedItem.toString()
                                )
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(WAIT_100)
                                mainActivity.mainViewModel.setEmployer(curEmployer)
                                mainActivity.title = getString(R.string.time_sheet) +
                                        " for " + spEmployers.selectedItem.toString()
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
        val nextCutOff = projections.generateNextCutOff(
            curEmployer!!,
            if (cutOffs.isEmpty()) "" else cutOffs[0]
        )
        mainActivity.payDayViewModel.insertPayPeriod(
            PayPeriods(
                nf.generateRandomIdAsLong(),
                nextCutOff,
                curEmployer!!.employerId,
                false,
                df.getCurrentTimeAsString()
            )
        )
        populateCutOffDates()
    }

    private fun populateCutOffDates() {
        if (curEmployer != null) {
            binding.apply {
                val cutOffAdapter = ArrayAdapter<Any>(
                    mView.context,
                    R.layout.spinner_item_bold
                )
                mainActivity.payDayViewModel.getCutOffDates(curEmployer!!.employerId).observe(
                    viewLifecycleOwner
                ) { dates ->
                    cutOffAdapter.clear()
                    cutOffs.clear()
                    cutOffAdapter.notifyDataSetChanged()
                    dates.listIterator().forEach {
                        cutOffAdapter.add(it.ppCutoffDate)
                        cutOffs.add(it.ppCutoffDate)
                    }
                    if (dates.isEmpty() ||
                        dates[0].ppCutoffDate < df.getCurrentDateAsString()
                    ) {
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
            spCutOff.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (spCutOff.adapter.count > 0) {
                            if (spCutOff.selectedItem.toString() !=
                                getString(R.string.generate_a_new_cut_off)
                            ) {
                                CoroutineScope(Dispatchers.Default).launch {
                                    curCutOff = spCutOff.selectedItem.toString()
                                    if (valuesFilled) mainActivity.mainViewModel.setCutOffDate(
                                        curCutOff
                                    )
                                    populatePayDayDate()
                                    populatePayDetails()
                                }
                                CoroutineScope(Dispatchers.Main).launch {
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
        if (curCutOff != "" && curEmployer != null) {
            binding.apply {
                val display = getString(R.string.pay_day_is_) +
                        df.getDisplayDate(
                            LocalDate.parse(curCutOff)
                                .plusDays(curEmployer!!.cutoffDaysBefore.toLong()).toString()
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
            mainActivity.payDayViewModel.getWorkDateList(
                curEmployer!!.employerId,
                curCutOff
            ).observe(viewLifecycleOwner) { workDates ->
                workDateAdapter!!.differ.submitList(workDates)
                updateUI(workDates)
            }
        }
    }

    override fun populatePayDetails() {
        CoroutineScope(Dispatchers.Main).launch {
            getSelectedPayPeriodObject()
            delay(WAIT_250)
            payCalculations = PayCalculationsAsync(
                mainActivity, curEmployer!!, curPayPeriod!!
            )
            delay(WAIT_1000)
            binding.apply {
                var display = nf.displayDollars(
                    -payCalculations.getDebitTotalsByPay()
                            - payCalculations.getAllTaxDeductions()
                )
                tvDeductions.text = display
                tvDeductions.setTextColor(Color.RED)
                display = getString(R.string.net_) +
                        nf.displayDollars(
                            payCalculations.getPayGross()
                                    - payCalculations.getDebitTotalsByPay()
                                    - payCalculations.getAllTaxDeductions()
                        )
                tvNetPay.text = display
                display = getString(R.string.gross_) +
                        nf.displayDollars(
                            payCalculations.getPayGross()
                        )
                tvGrossPay.text = display
                display = ""
                if (payCalculations.getHoursReg() != 0.0) {
                    display = getString(R.string.hours_) +
                            nf.getNumberFromDouble(payCalculations.getHoursReg())
                }
                if (payCalculations.getPayOt() != 0.0) {
                    if (display.isNotBlank()) display += getString(R.string.pipe)
                    display += getString(R.string.ot_) +
                            nf.getNumberFromDouble(payCalculations.getHoursOt())
                }
                if (payCalculations.getHoursDblOt() != 0.0) {
                    if (display.isNotBlank()) display += getString(R.string.pipe)
                    display += getString(R.string.dbl_ot_) +
                            nf.getNumberFromDouble((payCalculations.getHoursDblOt()))
                }
                if (payCalculations.getHoursStat() != 0.0) {
                    if (display.isNotBlank()) display += getString(R.string.pipe)
                    display += getString(R.string.stat_hours_) +
                            nf.getNumberFromDouble(payCalculations.getHoursStat())
                }
                tvHours.text = display
            }
        }
    }

    private fun populateFromHistory() {
        if (!valuesFilled) {
            binding.apply {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(WAIT_500)
                    if (mainActivity.mainViewModel.getEmployer() != null) {
                        curEmployer = mainActivity.mainViewModel.getEmployer()!!
                        for (i in 0 until spEmployers.adapter.count) {
                            if (spEmployers.getItemAtPosition(i) == curEmployer!!.employerName) {
                                spEmployers.setSelection(i)
                                break
                            }
                        }
                        delay(WAIT_500)
                        if (mainActivity.mainViewModel.getCutOffDate() != null) {
                            curCutOff = mainActivity.mainViewModel.getCutOffDate()!!
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
            }
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

    private fun getSelectedPayPeriod(): PayPeriods {
        binding.apply {
            return PayPeriods(
                nf.generateRandomIdAsLong(),
                spCutOff.selectedItem.toString(),
                curEmployer!!.employerId,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun getSelectedPayPeriodObject() {
        mainActivity.payDayViewModel.getPayPeriod(
            binding.spCutOff.selectedItem.toString(),
            curEmployer!!.employerId
        ).observe(viewLifecycleOwner) { payPeriod ->
            curPayPeriod = payPeriod
        }
        mainActivity.mainViewModel.setPayPeriod(curPayPeriod)

    }

    private fun gotoWorkDateAdd() {
        mainActivity.mainViewModel.setPayPeriod(getSelectedPayPeriod())
        mainActivity.mainViewModel.setCutOffDate(curCutOff)
        mainActivity.mainViewModel.setEmployer(curEmployer)
        gotoWorkDateAddFragment()
    }

    private fun gotoWorkDateAddFragment() {
        mView.findNavController().navigate(
            TimeSheetFragmentDirections
                .actionTimeSheetFragmentToWorkDateAddFragment()
        )
    }

    private fun gotoEmployerAdd() {
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mainActivity.mainViewModel.setEmployer(null)
        mainActivity.mainViewModel.setCutOffDate(null)
        gotoEmployerAddFragment()
    }

    private fun gotoEmployerAddFragment() {
        mView.findNavController().navigate(
            TimeSheetFragmentDirections
                .actionTimeSheetFragmentToEmployerAddFragment()
        )
    }

    private fun gotoPayDetails() {
        mainActivity.mainViewModel.setPayPeriod(getSelectedPayPeriod())
        mainActivity.mainViewModel.setCutOffDate(curCutOff)
        mainActivity.mainViewModel.setEmployer(curEmployer)
        gotoPayDetailFragment()
    }

    private fun gotoPayDetailFragment() {
        mView.findNavController().navigate(
            TimeSheetFragmentDirections
                .actionTimeSheetFragmentToPayDetailFragmentNew()
        )
    }

    override fun onStop() {
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mainActivity.mainViewModel.setCutOffDate(curCutOff)
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}