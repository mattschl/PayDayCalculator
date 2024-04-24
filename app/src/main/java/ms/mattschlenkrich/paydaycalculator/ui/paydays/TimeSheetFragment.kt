package ms.mattschlenkrich.paydaycalculator.ui.paydays

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.WorkDateAdapter
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_TIME_SHEET
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_100
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.common.WAIT_500
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTimeSheetBinding
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.payFunctions.PayCalculations
import ms.mattschlenkrich.paydaycalculator.payFunctions.PayDayProjections
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import java.time.LocalDate

private const val TAG = FRAG_TIME_SHEET

class TimeSheetFragment : Fragment(R.layout.fragment_time_sheet) {

    private var _binding: FragmentTimeSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private var curEmployer: Employers? = null
    private val cutOffs = ArrayList<String>()
    private var curPayPeriod: PayPeriods? = null
    private var curCutOff = ""
    private val projections = PayDayProjections()
    private var workDateAdapter: WorkDateAdapter? = null
    private var valuesFilled = false
    private val cf = NumberFunctions()
    private val df = DateFunctions()

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
        fillEmployers()
        setActions()
        selectEmployer()
        selectCutOffDate()
        fillFromHistory()
    }


    private fun fillFromHistory() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_500)
            binding.apply {
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
                                break
                            }
                        }
                    }
                }
            }
            valuesFilled = true
        }
    }

    private fun setActions() {
        binding.apply {
            fabAddDate.setOnClickListener {
                addWorkDate()
            }
            crdPayDetails.setOnClickListener {
                gotoPayDetails()
            }
        }
    }

    private fun gotoPayDetails() {
        mainActivity.mainViewModel.setCutOffDate(curCutOff)
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            TimeSheetFragmentDirections
                .actionTimeSheetFragmentToPayDetailsFragment()
        )
    }

    private fun addWorkDate() {
        mainActivity.mainViewModel.setPayPeriod(getPayPeriod())
        mainActivity.mainViewModel.setCutOffDate(curCutOff)
        mainActivity.mainViewModel.setEmployer(curEmployer)
        mView.findNavController().navigate(
            TimeSheetFragmentDirections
                .actionTimeSheetFragmentToWorkDateAddFragment()
        )
    }

    private fun getPayPeriod(): PayPeriods {
        binding.apply {
            return PayPeriods(
                cf.generateId(),
                spCutOff.selectedItem.toString(),
                curEmployer!!.employerId,
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun selectCutOffDate() {
        binding.apply {
            spCutOff.onItemSelectedListener =
                object : OnItemSelectedListener {
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
                                curCutOff = spCutOff.selectedItem.toString()
                                if (valuesFilled) mainActivity.mainViewModel.setCutOffDate(curCutOff)
                                fillPayDayDate()
                                fillWorkDates()
                                fillValues()
                            } else {
                                generateCutOff()
                            }
                        } else {
                            generateCutOff()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
//                        fillCutOffDates()*///-
                    }
                }
        }
    }

    fun fillValues() {
        mainActivity.payDayViewModel.getPayPeriod(
            binding.spCutOff.selectedItem.toString(),
            curEmployer!!.employerId
        ).observe(
            viewLifecycleOwner
        ) { payPeriod ->
            curPayPeriod = payPeriod
            mainActivity.mainViewModel.setPayPeriod(payPeriod)
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_250)
            val payCalculations = PayCalculations(
                mainActivity, curEmployer!!, mView, curPayPeriod!!
            )
//            delay(WAIT_1000)
            val grossPay = async { payCalculations.pay.getPayGross() }
//            val grossPay = deferGrossPay.await()
            val deductions = async { payCalculations.pay.getDebitTotalsByPay() }
//            val deductions = deferDeductions.await()
            val taxDeduction = async { payCalculations.tax.getAllTaxDeductions() }
//            val taxDeduction = deferTaxDeduction.await()
            val regHours = async { payCalculations.hours.getHoursReg() }
//            val regHours = deferRegHours.await()
            val otHours = async { payCalculations.hours.getHoursOt() }
//            val otHours = deferOtHours.await()
            val dblOtHours = async { payCalculations.hours.getHoursDblOt() }
//            val dblOtHours = deferDblOtHours.await()
            val statHours = async { payCalculations.hours.getHoursStat() }
//            val statHours = deferStatHours.await()
            binding.apply {
                var display = ""
                if (regHours.await() != 0.0) {
                    display = "Hours: ${regHours.await()}"
                }
                if (otHours.await() != 0.0) {
                    if (display.isNotBlank()) display += " | "
                    display += "Ot: ${otHours.await()}"
                }
                if (dblOtHours.await() != 0.0) {
                    if (display.isNotBlank()) display += " | "
                    display += "Dbl Ot: ${dblOtHours.await()}"
                }
                if (statHours.await() != 0.0) {
                    if (display.isNotBlank()) display += " | "
                    display += "Stat Hours: ${statHours.await()}"
                }
                tvHours.text = display
                display = "Gross ${cf.displayDollars(grossPay.await())}"
                tvGrossPay.text = display
                display = cf.displayDollars(-deductions.await() - taxDeduction.await())
                tvDeductions.text = display
                tvDeductions.setTextColor(Color.RED)
                display =
                    "NET: ${
                        cf.displayDollars(
                            grossPay.await() - deductions.await() - taxDeduction.await()
                        )
                    }"
                tvNetPay.text = display
            }
        }
    }

    private fun fillPayDayDate() {
        if (curCutOff != "" && curEmployer != null) {
            binding.apply {
                val display = df.getDisplayDate(
                    LocalDate.parse(curCutOff)
                        .plusDays(curEmployer!!.cutoffDaysBefore.toLong()).toString()
                ) + " - Pay Summary"
                tvPaySummary.text = display
            }
        }
    }

    private fun fillWorkDates() {
        binding.apply {
            workDateAdapter = null
            workDateAdapter = WorkDateAdapter(
                mainActivity, curCutOff, curEmployer!!, mView, this@TimeSheetFragment
            )
            rvDates.apply {
                layoutManager = LinearLayoutManager(mView.context)
                adapter = workDateAdapter
            }
            activity?.let {
                mainActivity.payDayViewModel.getWorkDateList(
                    curEmployer!!.employerId,
                    curCutOff
                ).observe(viewLifecycleOwner) { workDates ->
                    workDateAdapter!!.differ.submitList(workDates)
                    updateUI(workDates)
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

    private fun generateCutOff() {
        val nextCutOff = projections.generateNextCutOff(
            curEmployer!!,
            if (cutOffs.isEmpty()) "" else cutOffs[0]
        )
        mainActivity.payDayViewModel.insertPayPeriod(
            PayPeriods(
                cf.generateId(),
                nextCutOff,
                curEmployer!!.employerId,
                false,
                df.getCurrentTimeAsString()
            )
        )
        fillCutOffDates()
    }

    private fun selectEmployer() {
        binding.apply {
            spEmployers.onItemSelectedListener =
                object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (spEmployers.selectedItem.toString() !=
                            getString(R.string.add_new_employer)
                        ) {
                            CoroutineScope(Dispatchers.IO).launch {
                                curEmployer = mainActivity.employerViewModel.findEmployer(
                                    spEmployers.selectedItem.toString()
                                )
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(WAIT_100)
                                if (valuesFilled) mainActivity.mainViewModel.setEmployer(curEmployer)
                                mainActivity.title = getString(R.string.pay_details) +
                                        " for ${spEmployers.selectedItem}"
                                fillCutOffDates()
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

    private fun gotoEmployerAdd() {
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mainActivity.mainViewModel.setEmployer(null)
        mainActivity.mainViewModel.setCutOffDate(null)
        mView.findNavController().navigate(
            TimeSheetFragmentDirections
                .actionTimeSheetFragmentToEmployerAddFragment()
        )
    }

    private fun fillCutOffDates() {
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
//                    Log.d(TAG, "cutOffAdapter has ${dates.size}")
                    if (dates.isEmpty() ||
                        dates[0].ppCutoffDate < df.getCurrentDateAsString()
                    ) {
                        generateCutOff()
                    } else {
                        cutOffAdapter.add(getString(R.string.generate_a_new_cut_off))
                    }
                }
                spCutOff.adapter = cutOffAdapter
//                gotoCurrentCutoff()
            }
        }
    }

    private fun fillEmployers() {
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
                curEmployer = employers[0]
            }
//            updateUI(employers)
            employerAdapter.add(getString(R.string.add_new_employer))
//            fillCutOffDates()
        }
        binding.spEmployers.adapter = employerAdapter
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