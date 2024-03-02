package ms.mattschlenkrich.paydaycalculator.ui.paydays

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import ms.mattschlenkrich.paydaycalculator.adapter.PayDetailTaxAdapter
import ms.mattschlenkrich.paydaycalculator.common.CommonFunctions
import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
import ms.mattschlenkrich.paydaycalculator.common.FRAG_PAY_DETAILS
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.common.WAIT_500
import ms.mattschlenkrich.paydaycalculator.databinding.FragmentPayDetailsBinding
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.model.PayPeriodExtraAndTypeFull
import ms.mattschlenkrich.paydaycalculator.model.PayPeriods
import ms.mattschlenkrich.paydaycalculator.payFunctions.PayCalculations
import java.time.LocalDate

private const val TAG = FRAG_PAY_DETAILS

class PayDetailsFragment : Fragment(R.layout.fragment_pay_details) {

    private var _binding: FragmentPayDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private var curEmployer: Employers? = null
    private var curPayPeriod: PayPeriods? = null
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
                gotoExtraAdd(true)
            }
            fabAddDeduction.setOnClickListener {
                gotoExtraAdd(false)
            }
        }
    }

    private fun gotoExtraAdd(isCredit: Boolean) {
        mainActivity.payDayViewModel.getPayPeriod(
            curCutOff, curEmployer!!.employerId
        ).observe(viewLifecycleOwner) { payPeriod ->
            mainActivity.mainViewModel.setPayPeriod(payPeriod)
        }
        mainActivity.mainViewModel.setEmployer(curEmployer!!)
        mainActivity.mainViewModel.setIsCredit(isCredit)
        mView.findNavController().navigate(
            PayDetailsFragmentDirections
                .actionPayDetailsFragmentToPayPeriodExtraAddFragment()
        )
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

    fun fillPayDetails() {
        val payCalculations = PayCalculations(
            mainActivity, curEmployer!!, curCutOff, mView
        )
        mainActivity.payDayViewModel.getPayPeriod(
            curCutOff, curEmployer!!.employerId
        ).observe(
            viewLifecycleOwner
        ) { payPeriod ->
            curPayPeriod = payPeriod
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_500)
            binding.apply {
                var display = "Gross ${
                    cf.displayDollars(
                        payCalculations.pay.getPayGross()
                    )
                }"
                tvGrossPay.text = display
                display = cf.displayDollars(
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
                fillExtras(payCalculations)
            }
        }
    }

    private fun findExtras(): ArrayList<PayPeriodExtraAndTypeFull> {
        val debitList = ArrayList<PayPeriodExtraAndTypeFull>()
        //TODO: go through te list of workDateExtras -
        // combine into one PayPeriodExtra that cannot be edited
//        mainActivity.payDayViewModel.getWorkDateList(
//            curEmployer!!.employerId, curCutOff
//        ).observe(viewLifecycleOwner) { dates ->
//            val wdExtraList = ArrayList<WorkDateExtras>()
//            dates.listIterator().forEach {
//                mainActivity.payDayViewModel.getWorkDateExtrasActive(
//                    it.workDateId
//                ).observe(viewLifecycleOwner) { wdExtras ->
//                    wdExtras.listIterator().forEach {
//                        wdExtraList.add(it)
//                    }
//                }
//            }
//            wdExtraList.sortBy { extra ->
//                extra.wdeName
//            }
//            var workDateExtraTotal = 0.0
//            for (i in 0 until  wdExtraList.size) {
//                if (i == 0) {
//                    if (wdExtraList[1].wdeIsFixed) {
//                        workDateExtraTotal += wdExtraList[i].wdeValue
//                    } else {
//                        if (wdExtraList[i].wdeAppliesTo == 1) {
//                            workDateExtraTotal += wdExtraList[i].wdeValue
//                        }
//                    }
//                }
//            }
//        }

        mainActivity.workExtraViewModel.getExtraTypesAndDef(
            curEmployer!!.employerId, curCutOff, 3
        ).observe(viewLifecycleOwner) { extras ->
            extras.listIterator().forEach {
                Log.d(TAG, "ExtraType  added is ${it.extraType.wetName}")
                debitList.add(
                    PayPeriodExtraAndTypeFull(
                        null,
                        it.extraType,
                        it.definition
                    )
                )
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_250)
            mainActivity.payDayViewModel.getPayPeriodExtras(
                curPayPeriod!!.payPeriodId
            ).observe(viewLifecycleOwner) { credit ->
                credit.listIterator().forEach {
                    Log.d(TAG, "PayPeriodExtra  added is ${it.ppeName}")
                    debitList.add(
                        PayPeriodExtraAndTypeFull(
                            it,
                            null,
                            null
                        )
                    )
                }
            }
        }
        return debitList
    }

    private fun fillExtras(payCalculations: PayCalculations) {
        val extrasList = findExtras()
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_500)
            fillCredits(payCalculations, extrasList)
            fillDeductions(payCalculations, extrasList)
        }
    }

    private fun fillCredits(
        payCalculations: PayCalculations,
        extraList: ArrayList<PayPeriodExtraAndTypeFull>,
    ) {
        val creditList = ArrayList<PayPeriodExtraAndTypeFull>()
        var creditTotal = 0.0
        for (extra in extraList) {
            if (extra.payPeriodExtra != null) {
                if (extra.payPeriodExtra!!.ppeIsCredit) {
                    creditTotal += extra.payPeriodExtra!!.ppeValue
                    creditList.add(extra)
                }
            } else {
                if (extra.extraType!!.wetIsCredit) {
                    creditList.add(extra)
                    when (extra.extraType!!.wetAppliesTo) {
                        0 -> {
                            creditTotal += if (extra.extraDef!!.weIsFixed) {
                                payCalculations.hours.getHoursWorked() *
                                        extra.extraDef!!.weValue
                            } else {
                                payCalculations.hours.getHoursWorked() *
                                        extra.extraDef!!.weValue / 100
                            }
                        }

                        1 -> {
                            creditTotal += if (extra.extraDef!!.weIsFixed) {
                                payCalculations.hours.getDaysWorked() *
                                        extra.extraDef!!.weValue
                            } else {
                                payCalculations.pay.getPayTimeWorked() *
                                        extra.extraDef!!.weValue / 100
                            }
                        }

                        3 -> {
                            creditTotal += if (extra.extraDef!!.weIsFixed) {
                                extra.extraDef!!.weValue
                            } else {
                                payCalculations.pay.getPayHourly() *
                                        extra.extraDef!!.weValue / 100
                            }
                        }
                    }
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_250)
            val creditListAdapter = PayDetailExtraAdapter(
                creditList, mView, this@PayDetailsFragment
            )
            binding.apply {
                rvCredits.layoutManager = LinearLayoutManager(mView.context)
                rvCredits.adapter = creditListAdapter
                tvCreditTotal.text = cf.displayDollars(creditTotal)
            }
        }
    }

    private fun fillDeductions(
        payCalculations: PayCalculations,
        extraList: ArrayList<PayPeriodExtraAndTypeFull>,
    ) {
        val debitList = ArrayList<PayPeriodExtraAndTypeFull>()
        var debitTotal = 0.0
        for (extra in extraList) {
            if (extra.payPeriodExtra != null) {
                if (!extra.payPeriodExtra!!.ppeIsCredit) {
                    debitTotal += extra.payPeriodExtra!!.ppeValue
                    debitList.add(extra)
                }
            } else {
                if (!extra.extraType!!.wetIsCredit) {
                    debitList.add(extra)
                    when (extra.extraType!!.wetAppliesTo) {
                        0 -> {
                            debitTotal += if (extra.extraDef!!.weIsFixed) {
                                payCalculations.hours.getHoursWorked() *
                                        extra.extraDef!!.weValue
                            } else {
                                payCalculations.hours.getHoursWorked() *
                                        extra.extraDef!!.weValue / 100
                            }
                        }

                        1 -> {
                            debitTotal += if (extra.extraDef!!.weIsFixed) {
                                payCalculations.hours.getDaysWorked() *
                                        extra.extraDef!!.weValue
                            } else {
                                payCalculations.pay.getPayTimeWorked() *
                                        extra.extraDef!!.weValue / 100
                            }
                        }

                        3 -> {
                            debitTotal += if (extra.extraDef!!.weIsFixed) {
                                extra.extraDef!!.weValue
                            } else {
                                payCalculations.pay.getPayHourly() *
                                        extra.extraDef!!.weValue / 100
                            }
                        }
                    }
                }
            }
        }
        val taxList = ArrayList<ExtraAndTotal>()
        for (tax in payCalculations.tax.getTaxList()
        ) {
            taxList.add(
                ExtraAndTotal(
                    tax.taxType, tax.amount
                )
            )
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_250)
            val deductionListAdapter = PayDetailExtraAdapter(
                debitList, mView, this@PayDetailsFragment
            )
            val taxListAdapter = PayDetailTaxAdapter(taxList)
            for (tax in taxList) {
                debitTotal += tax.amount
            }
            binding.apply {
                rvDebits.layoutManager = LinearLayoutManager(mView.context)
                rvDebits.adapter = deductionListAdapter
                rvTax.layoutManager = LinearLayoutManager(mView.context)
                rvTax.adapter = taxListAdapter
                delay(WAIT_250)
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
                    }
                    if (dates.isEmpty()
                    ) {
                        AlertDialog.Builder(mView.context)
                            .setTitle("No pay days to view")
                            .setMessage(
                                "Since there are no pay periods set, you will be sent " +
                                        "to the time sheet to create a new one."
                            )
                            .setPositiveButton("Ok") { _, _ ->
                                mView.findNavController().navigate(
                                    PayDetailsFragmentDirections
                                        .actionPayDetailsFragmentToTimeSheetFragment()
                                )
                            }
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
            employerAdapter.add(getString(R.string.add_new_employer))
        }
        binding.spEmployers.adapter = employerAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}