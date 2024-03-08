package ms.mattschlenkrich.paydaycalculator.ui.paydays

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.WorkDateAdapter
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_TIME_SHEET
import ms.mattschlenkrich.paydaycalculator.common.WAIT_100
import ms.mattschlenkrich.paydaycalculator.common.WAIT_1000
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.common.WAIT_500
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentTimeSheetBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
import ms.mattschlenkrich.paydaycalculator.payFunctions.PayCalculations
import ms.mattschlenkrich.paydaycalculator.payFunctions.PayDayProjections
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
    private val cf = CommonFunctions()
    private val df = DateFunctions()
    private var workDateAdapter: WorkDateAdapter? = null
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
        fillEmployers()
        selectEmployer()
        selectCutOffDate()
        setActions()
        fillFromHistory()
    }

    private fun fillFromHistory() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_500)
            Log.d(
                TAG, "the employer is " +
                        "${mainActivity.mainViewModel.getEmployer()?.employerName}"
            )
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
                    Log.d(
                        TAG, "The cutoff is " +
                                "${mainActivity.mainViewModel.getCutOffDate()} if found"
                    )
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
                mainActivity, curEmployer!!, curCutOff, mView, curPayPeriod!!
            )
            delay(WAIT_1000)
            binding.apply {
                var display = cf.displayDollars(
                    -payCalculations.pay.getDebitTotalsByPay()
                            - payCalculations.tax.getAllTaxDeductions()
                )
                tvDeductions.text = display
                tvDeductions.setTextColor(Color.RED)
                display = "NET: ${
                    cf.displayDollars(
                        payCalculations.pay.getPayGross()
                                - payCalculations.pay.getDebitTotalsByPay()
                                - payCalculations.tax.getAllTaxDeductions()
                    )
                }"
                tvNetPay.text = display
                display = "Gross ${
                    cf.displayDollars(
                        payCalculations.pay.getPayGross()
                    )
                }"
                tvGrossPay.text = display
                display = ""
                if (payCalculations.hours.getHoursReg() != 0.0) {
                    display = "Hours: ${payCalculations.hours.getHoursReg()}"
                }
                if (payCalculations.pay.getPayOt() != 0.0) {
                    if (display.isNotBlank()) display += " | "
                    display += "Ot: ${payCalculations.hours.getHoursOt()}"
                }
                if (payCalculations.hours.getHoursDblOt() != 0.0) {
                    if (display.isNotBlank()) display += " | "
                    display += "Dbl Ot: ${payCalculations.hours.getHoursDblOt()}"
                }
                if (payCalculations.hours.getHoursStat() != 0.0) {
                    if (display.isNotBlank()) display += " | "
                    display += "Stat Hours: ${payCalculations.hours.getHoursStat()}"
                }
                tvHours.text = display
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

//    private fun gotoCurrentCutoff() {
//        CoroutineScope(Dispatchers.Main).launch {
//            delay(WAIT_500)
//            binding.apply {
//                val dateNow = LocalDate.parse(df.getCurrentDateAsString())
//                for (i in 0 until spCutOff.adapter.count - 1) {
//                    val chkDate = LocalDate.parse(spCutOff.getItemAtPosition(i).toString())
//                    if (chkDate <= dateNow) {
//                        spCutOff.setSelection(i)
//                        break
//                    }
//                }
//            }
//        }
//    }

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