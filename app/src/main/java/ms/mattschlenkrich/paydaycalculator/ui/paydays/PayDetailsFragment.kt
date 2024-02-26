package ms.mattschlenkrich.paydaycalculator.ui.paydays

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
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.R
import ms.mattschlenkrich.paydaycalculator.adapter.PayDetailExtraAdapter
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.common.WAIT_500
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentPayDetailsBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.payFunctions.PayCalculations
import java.time.LocalDate

private const val TAG = "PayDetails"

class PayDetailsFragment : Fragment(R.layout.fragment_pay_details) {

    private var _binding: FragmentPayDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private var curEmployer: Employers? = null
    private val cutOffs = ArrayList<String>()
    private var curCutOff = ""
    private val cf = CommonFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayDetailsBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainActivity.title = getString(R.string.pay_details)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillEmployers()
        selectEmployer()
        selectCutOffDate()
        fillValues()
        setActions()
    }

    private fun setActions() {
        binding.apply {
            fabAddExtra.setOnClickListener {
                //gotoExtraAddByPay
            }
        }
    }

    private fun selectCutOffDate() {
        binding.apply {
            spCutOff.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        curCutOff = spCutOff.selectedItem.toString()
                        fillPayDayDate()
                        fillPayDetails()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
//                        fillCutOffDates()*///-
                    }
                }
        }
    }

    private fun fillPayDetails() {
        val payCalculations = PayCalculations(
            mainActivity, curEmployer!!, curCutOff, mView
        )
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_500)
            binding.apply {
                if (payCalculations.pay.getPayReg() > 0.0) {
                    llRegPay.visibility = View.VISIBLE
                    tvRegHours.text = payCalculations.hours.getHoursReg().toString()
                    tvRegRate.text = cf.displayDollars(payCalculations.rate)
                    tvRegPay.text = cf.displayDollars(payCalculations.pay.getPayReg())
                } else {
                    llRegPay.visibility = View.GONE
                }
                if (payCalculations.pay.getPayOt() > 0.0) {
                    llOtPay.visibility = View.VISIBLE
                    tvOtHours.text = payCalculations.hours.getHoursOt().toString()
                    tvOtRate.text = cf.displayDollars(payCalculations.rate * 1.5)
                    tvOTPay.text = cf.displayDollars(payCalculations.pay.getPayOt())
                } else {
                    llOtPay.visibility = View.GONE
                }
                if (payCalculations.pay.getPayDblOt() > 0.0) {
                    llDblOtPay.visibility = View.VISIBLE
                    tvDblOtHours.text = payCalculations.hours.getHoursDblOt().toString()
                    tvDblOtRate.text = cf.displayDollars(payCalculations.rate * 2)
                    tvDblOtPay.text = cf.displayDollars(payCalculations.pay.getPayDblOt())
                } else {
                    llDblOtPay.visibility = View.GONE
                }
                if (payCalculations.pay.getPayStat() > 0.0) {
                    llStatPay.visibility = View.VISIBLE
                    tvStatHours.text = payCalculations.hours.getHoursStat().toString()
                    tvStatRate.text = cf.displayDollars(payCalculations.rate)
                    tvStatPay.text = cf.displayDollars(payCalculations.pay.getPayStat())
                } else {
                    llStatPay.visibility = View.GONE
                }
                tvHourlyTotal.text = cf.displayDollars(payCalculations.pay.getPayHourly())
                val creditList =
                    payCalculations.extras.getCreditExtraAndTotalsByDate()
                val creditByPay =
                    payCalculations.extras.getCreditExtrasAndTotalsByPay()
                for (credit in creditByPay) {
                    creditList.add(credit)
                }
//                Log.d(TAG, "creditList size is ${creditList.size}")
                val creditLstAdapter = PayDetailExtraAdapter(creditList)
                rvCredits.layoutManager = LinearLayoutManager(mView.context)
                rvCredits.adapter = creditLstAdapter
                tvCreditTotal.text = cf.displayDollars(
                    payCalculations.pay.getCreditTotalAll()
                )
                val debitList =
                    payCalculations.deductions.getDebitExtraAndTotalByPay()
                for (tax in payCalculations.tax.getTaxList()
                ) {
                    debitList.add(
                        ExtraAndTotal(
                            tax.taxType, tax.amount
                        )
                    )
                }
                val deductionListAdapter = PayDetailExtraAdapter(debitList)
                rvDebits.layoutManager = LinearLayoutManager(mView.context)
                rvDebits.adapter = deductionListAdapter
                var debitTotal = 0.0
                for (debit in debitList) {
                    debitTotal += debit.amount
                }
                tvDebitTotal.text = cf.displayDollars(debitTotal)
            }
        }
    }

    private fun fillValues() {
        binding.apply {
            if (mainActivity.mainViewModel.getEmployer() != null) {
                curEmployer = mainActivity.mainViewModel.getEmployer()!!
                for (i in 0 until spEmployers.adapter.count) {
                    if (spEmployers.getItemAtPosition(i) == curEmployer!!.employerName) {
                        spEmployers.setSelection(i)
                        break
                    }
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(WAIT_250)
                if (mainActivity.mainViewModel.getCutOffDate() != null) {
                    curCutOff = mainActivity.mainViewModel.getCutOffDate()!!
                    for (i in 0 until spCutOff.adapter.count) {
                        if (spCutOff.getItemAtPosition(i) == curCutOff) {
                            spCutOff.setSelection(i)
                            break
                        }
                    }
                }
            }
        }
    }

    private fun fillPayDayDate() {
        if (curCutOff != "" && curEmployer != null) {
            binding.apply {
                val display = "Pay Day is " +
                        df.getDisplayDate(
                            LocalDate.parse(curCutOff)
                                .plusDays(curEmployer!!.cutoffDaysBefore.toLong()).toString()
                        )
                tvPaySummary.text = display
            }
        }
    }


    private fun gotoEmployerAdd() {
        mainActivity.mainViewModel.setCallingFragment(TAG)
        mView.findNavController().navigate(
            TimeSheetFragmentDirections
                .actionTimeSheetFragmentToEmployerAddFragment()
        )
    }

    private fun selectEmployer() {
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
                            mainActivity.employerViewModel.findEmployer(
                                spEmployers.selectedItem.toString()
                            ).observe(viewLifecycleOwner) { employer ->
                                curEmployer = employer
                            }
                            mainActivity.title = getString(R.string.time_sheet) +
                                    " for ${spEmployers.selectedItem}"
                            fillCutOffDates()
                        } else {
                            gotoEmployerAdd()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        fillEmployers()
                    }
                }
        }
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
                    if (dates.isEmpty()
                    ) {
                        cutOffAdapter.add(getString(R.string.no_cutoff_dates))
                    }
                }
                spCutOff.adapter = cutOffAdapter
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}