package ms.mattschlenkrich.paydaycalculator.ui.paydays
//
//import android.graphics.Color
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.Menu
//import android.view.MenuInflater
//import android.view.MenuItem
//import android.view.View
//import android.view.ViewGroup
//import android.widget.AdapterView
//import android.widget.ArrayAdapter
//import androidx.appcompat.app.AlertDialog
//import androidx.core.view.MenuProvider
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.Lifecycle
//import androidx.navigation.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import ms.mattschlenkrich.paydaycalculator.R
//import ms.mattschlenkrich.paydaycalculator.adapter.PayDetailExtraAdapter
//import ms.mattschlenkrich.paydaycalculator.adapter.PayDetailTaxAdapter
//import ms.mattschlenkrich.paydaycalculator.common.DateFunctions
//import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
//import ms.mattschlenkrich.paydaycalculator.common.WAIT_100
//import ms.mattschlenkrich.paydaycalculator.common.WAIT_1000
//import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
//import ms.mattschlenkrich.paydaycalculator.common.WAIT_500
//import ms.mattschlenkrich.paydaycalculator.databinding.FragmentPayDetailsBinding
//import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
//import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraAndTotal
//import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraDefinitionAndType
//import ms.mattschlenkrich.paydaycalculator.model.extras.WorkDateExtrasAndDates
//import ms.mattschlenkrich.paydaycalculator.model.payperiod.PayPeriods
//import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkPayPeriodExtras
//import ms.mattschlenkrich.paydaycalculator.payFunctions.PayCalculations
//import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
//import java.time.LocalDate
//
//class PayDetailsFragment2 : Fragment(R.layout.fragment_pay_details) {
//
//    private var _binding: FragmentPayDetailsBinding? = null
//    private val binding get() = _binding!!
//    private lateinit var mView: View
//    private lateinit var mainActivity: MainActivity
//    private var curEmployer: Employers? = null
//    private var curPayPeriod: PayPeriods? = null
//    private val cutOffs = ArrayList<String>()
//    private var curCutOff = ""
//    private val cf = NumberFunctions()
//    private val df = DateFunctions()
//    private var valuesFilled = false
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentPayDetailsBinding.inflate(
//            inflater, container, false
//        )
//        mView = binding.root
//        mainActivity = (activity as MainActivity)
//        mainActivity.title = getString(R.string.pay_details)
//        return mView
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        fillEmployers()
//        setActions()
//        selectEmployer()
//        selectCutOffDate()
//        fillMenu()
//        fillValues()
//    }
//
//    private fun fillMenu() {
//        mainActivity.addMenuProvider(object : MenuProvider {
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menuInflater.inflate(R.menu.menu_delete, menu)
//            }
//
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//                return when (menuItem.itemId) {
//                    R.id.menu_delete -> {
//                        chooseDeletePayDay()
//                        true
//                    }
//
//                    else -> {
//                        false
//                    }
//                }
//            }
//        }, viewLifecycleOwner, Lifecycle.State.CREATED)
//    }
//
//    private fun chooseDeletePayDay() {
//        android.app.AlertDialog.Builder(mView.context)
//            .setTitle("Confirm Delete Pay Period")
//            .setMessage(
//                "**Warning!! \n" +
//                        "This action cannot be undone! " +
//                        "All the work dates will have to b re-entered."
//            )
//            .setPositiveButton("DELETE") { _, _ ->
//                deletePayDay()
//            }
//            .setNegativeButton("CANCEL", null)
//            .create().show()
//    }
//
//    private fun deletePayDay() {
//        mainActivity.payDayViewModel.updatePayPeriod(
//            PayPeriods(
//                curPayPeriod!!.payPeriodId,
//                curPayPeriod!!.ppCutoffDate,
//                curPayPeriod!!.ppEmployerId,
//                true,
//                df.getCurrentTimeAsString()
//            )
//        )
//        fillCutOffDates(curEmployer)
//    }
//
//
//    private fun setActions() {
//        binding.apply {
//            fabAddExtra.setOnClickListener {
//                gotoExtraAdd(true)
//            }
//            fabAddDeduction.setOnClickListener {
//                gotoExtraAdd(false)
//            }
//        }
//    }
//
//    private fun gotoExtraAdd(isCredit: Boolean) {
////        Log.d(TAG, "IN THE FUNCTION the button actions")
//        AlertDialog.Builder(mView.context)
//            .setTitle("Continue adding?")
//            .setMessage(
//                "It is best to add custom extras only after all the " +
//                        "work hours have been entered. " +
//                        "If it is based on the number of hours, days or a percentage, " +
//                        "the results could be improperly calculated. "
//            )
//            .setPositiveButton("Continue") { _, _ ->
//                mainActivity.payDayViewModel.getPayPeriod(
//                    curCutOff, curEmployer!!.employerId
//                ).observe(viewLifecycleOwner) { payPeriod ->
//                    mainActivity.mainViewModel.setPayPeriod(payPeriod)
//                }
//                mainActivity.mainViewModel.setEmployer(curEmployer!!)
//                mainActivity.mainViewModel.setIsCredit(isCredit)
//                mView.findNavController().navigate(
//                    PayDetailsFragmentDirections
//                        .actionPayDetailsFragmentToPayPeriodExtraAddFragment()
//                )
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
//
//    private fun selectCutOffDate() {
//        binding.apply {
//            spCutOff.onItemSelectedListener =
//                object : AdapterView.OnItemSelectedListener {
//                    override fun onItemSelected(
//                        parent: AdapterView<*>?,
//                        view: View?,
//                        position: Int,
//                        id: Long
//                    ) {
//                        if (curCutOff != spCutOff.selectedItem.toString()) {
//                            curCutOff = spCutOff.selectedItem.toString()
//                            if (valuesFilled) mainActivity.mainViewModel.setCutOffDate(curCutOff)
//                            fillPayDayDate()
//                            fillPayDetails()
//                        }
//                    }
//
//                    override fun onNothingSelected(parent: AdapterView<*>?) {
////                        fillCutOffDates()*///-
//                    }
//                }
//        }
//    }
//
//    fun fillPayDetails() {
//        mainActivity.payDayViewModel.getPayPeriod(
//            curCutOff, curEmployer!!.employerId
//        ).observe(
//            viewLifecycleOwner
//        ) { payPeriod ->
//            curPayPeriod = payPeriod
//            mainActivity.mainViewModel.setPayPeriod(payPeriod)
//        }
//        CoroutineScope(Dispatchers.Main).launch {
//            delay(WAIT_250)
//            val payCalculations = PayCalculations(
//                mainActivity, curEmployer!!, curCutOff, mView, curPayPeriod!!
//            )
//            delay(WAIT_500)
//            fillExtras(payCalculations)
//            delay(WAIT_500)
//            binding.apply {
//                var display = "Gross ${
//                    cf.displayDollars(
//                        payCalculations.pay.getPayGross()
//                    )
//                }"
//                tvGrossPay.text = display
//                display = cf.displayDollars(
//                    -payCalculations.pay.getDebitTotalsByPay()
//                            - payCalculations.tax.getAllTaxDeductions()
//                )
//                tvDeductions.text = display
//                tvDeductions.setTextColor(Color.RED)
//                display = "NET: ${
//                    cf.displayDollars(
//                        payCalculations.pay.getPayGross()
//                                - payCalculations.pay.getDebitTotalsByPay()
//                                - payCalculations.tax.getAllTaxDeductions()
//                    )
//                }"
//                tvNetPay.text = display
//                if (payCalculations.pay.getPayReg() > 0.0) {
//                    llRegPay.visibility = View.VISIBLE
//                    tvRegHours.text = payCalculations.hours.getHoursReg().toString()
//                    tvRegRate.text = cf.displayDollars(payCalculations.rate)
//                    tvRegPay.text = cf.displayDollars(payCalculations.pay.getPayReg())
//                } else {
//                    llRegPay.visibility = View.GONE
//                }
//                if (payCalculations.pay.getPayOt() > 0.0) {
//                    llOtPay.visibility = View.VISIBLE
//                    tvOtHours.text = payCalculations.hours.getHoursOt().toString()
//                    tvOtRate.text = cf.displayDollars(payCalculations.rate * 1.5)
//                    tvOTPay.text = cf.displayDollars(payCalculations.pay.getPayOt())
//                } else {
//                    llOtPay.visibility = View.GONE
//                }
//                if (payCalculations.pay.getPayDblOt() > 0.0) {
//                    llDblOtPay.visibility = View.VISIBLE
//                    tvDblOtHours.text = payCalculations.hours.getHoursDblOt().toString()
//                    tvDblOtRate.text = cf.displayDollars(payCalculations.rate * 2)
//                    tvDblOtPay.text = cf.displayDollars(payCalculations.pay.getPayDblOt())
//                } else {
//                    llDblOtPay.visibility = View.GONE
//                }
//                if (payCalculations.pay.getPayStat() > 0.0) {
//                    llStatPay.visibility = View.VISIBLE
//                    tvStatHours.text = payCalculations.hours.getHoursStat().toString()
//                    tvStatRate.text = cf.displayDollars(payCalculations.rate)
//                    tvStatPay.text = cf.displayDollars(payCalculations.pay.getPayStat())
//                } else {
//                    llStatPay.visibility = View.GONE
//                }
//                tvHourlyTotal.text = cf.displayDollars(payCalculations.pay.getPayHourly())
//            }
//        }
//    }
//
//    private fun findExtras(payCalculations: PayCalculations): ArrayList<WorkPayPeriodExtras> {
//        val extraList = mutableListOf<WorkPayPeriodExtras>()
//        CoroutineScope(Dispatchers.Main).launch {
//            delay(WAIT_250)
//            mainActivity.payDayViewModel.getPayPeriodExtras(
//                curPayPeriod!!.payPeriodId
//            ).observe(viewLifecycleOwner) { credit ->
//                credit.listIterator().forEach {
//                    processExtraByManuallyAdded(it, payCalculations, extraList)
//                }
//            }
//            val workDateExtrasAndDates = ArrayList<WorkDateExtrasAndDates>()
//            mainActivity.payDayViewModel.getWorkDateExtrasAndDates(
//                curCutOff
//            ).observe(viewLifecycleOwner) { extraPlusDate ->
//                extraPlusDate.listIterator().forEach {
//                    workDateExtrasAndDates.add(it)
//                }
//            }
//            delay(WAIT_250)
//            processExtrasByDay(workDateExtrasAndDates, extraList)
//            delay(WAIT_250)
//            mainActivity.workExtraViewModel.getExtraTypesAndDef(
//                curEmployer!!.employerId, curCutOff, 3
//            ).observe(viewLifecycleOwner) { extras ->
//                extras.listIterator().forEach {
//                    processExtraByPayPeriod(extraList, it, payCalculations)
//                }
//            }
//
//        }
//        return extraList as ArrayList<WorkPayPeriodExtras>
//    }
//
//    private fun processExtrasByDay(
//        workDateExtrasAndDates: ArrayList<WorkDateExtrasAndDates>,
//        extraList: MutableList<WorkPayPeriodExtras>
//    ) {
//        var subTotal = 0.0
//        for (i in 0 until workDateExtrasAndDates.size) {
//            workDateExtrasAndDates[i].apply {
//                subTotal +=
//                    when (workDateExtra.wdeAppliesTo) {
//                        0 -> {
//                            (workDate.wdRegHours + workDate.wdOtHours + workDate.wdDblOtHours) *
//                                    workDateExtra.wdeValue /
//                                    if (workDateExtra.wdeIsFixed) 1 else 100
//                        }
//
//                        1 -> {
//                            workDateExtra.wdeValue /
//                                    if (workDateExtra.wdeIsFixed) 1 else 100
//                        }
//
//                        else -> {
//                            0.0
//                        }
//                    }
//                if (i < workDateExtrasAndDates.size - 1) {
//                    if (workDateExtrasAndDates[i].workDateExtra.wdeName
//                        != workDateExtrasAndDates[i + 1].workDateExtra.wdeName
//                    ) {
//                        extraList.add(
//                            createExtraByManuallyAdded(subTotal)
//                        )
//                        subTotal = 0.0
//                    }
//                } else {
//                    extraList.add(
//                        createExtraByManuallyAdded(subTotal)
//                    )
//                    subTotal = 0.0
//                }
//            }
//        }
//    }
//
//    private fun WorkDateExtrasAndDates.createExtraByManuallyAdded(
//        subTotal: Double
//    ) = WorkPayPeriodExtras(
//        cf.generateId(),
//        curPayPeriod!!.payPeriodId,
//        null,
//        workDateExtra.wdeName,
//        workDateExtra.wdeAppliesTo,
//        1,
//        subTotal,
//        true,
//        workDateExtra.wdeIsCredit,
//        false,
//        df.getCurrentTimeAsString()
//    )
//
//    private fun processExtraByPayPeriod(
//        extraList: MutableList<WorkPayPeriodExtras>,
//        it: ExtraDefinitionAndType,
//        payCalculations: PayCalculations
//    ) {
//        var notFound = true
//        for (extra in extraList) {
//            if (extra.ppeName == it.extraType.wetName) {
//                notFound = false
//            }
//        }
//        if (notFound) {
//            when (it.extraType.wetAppliesTo) {
//                0 -> {
//                    val sum = if (it.definition.weIsFixed) {
//                        payCalculations.hours.getHoursWorked() * it.definition.weValue
//                    } else {
//                        payCalculations.pay.getPayTimeWorked() * it.definition.weValue / 100
//                    }
//                    extraList.add(
//                        createWorkPayPeriodExtra(it, sum)
//                    )
//                }
//
//                1 -> {
//                    val sum =
//                        if (it.definition.weIsFixed) {
//                            payCalculations.hours.getDaysWorked() * it.definition.weValue
//                        } else {
//                            payCalculations.pay.getPayTimeWorked() * it.definition.weValue / 100
//                        }
//                    extraList.add(
//                        createWorkPayPeriodExtra(it, sum)
//                    )
//                }
//
//                3 -> {
//                    if (it.definition.weIsFixed) {
//                        extraList.add(
//                            createWorkPayPeriodExtra(it, it.definition.weValue)
//                        )
//                    } else {
//                        val sum =
//                            payCalculations.pay.getPayHourly() * it.definition.weValue / 100
//                        extraList.add(
//                            createWorkPayPeriodExtra(it, sum)
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    private fun createWorkPayPeriodExtra(
//        it: ExtraDefinitionAndType,
//        sum: Double
//    ) = WorkPayPeriodExtras(
//        cf.generateId(),
//        curPayPeriod!!.payPeriodId,
//        it.extraType.workExtraTypeId,
//        it.extraType.wetName,
//        it.extraType.wetAppliesTo,
//        it.extraType.wetAttachTo,
//        sum,
//        it.definition.weIsFixed,
//        it.extraType.wetIsCredit,
//        !it.extraType.wetIsDefault,
//        df.getCurrentTimeAsString()
//    )
//
//    private fun processExtraByManuallyAdded(
//        it: WorkPayPeriodExtras,
//        payCalculations: PayCalculations,
//        extraList: MutableList<WorkPayPeriodExtras>
//    ) {
//        var sum = 0.0
//        when (it.ppeAppliesTo) {
//            0 -> {
//                sum = if (it.ppeIsFixed) {
//                    payCalculations.hours.getHoursWorked() * it.ppeValue
//                } else {
//                    payCalculations.pay.getPayTimeWorked() * it.ppeValue / 100
//                }
//            }
//
//            1 -> {
//                sum = if (it.ppeIsFixed) {
//                    payCalculations.hours.getDaysWorked() * it.ppeValue
//                } else {
//                    payCalculations.pay.getPayTimeWorked() * it.ppeValue / 100
//                }
//            }
//
//            3 -> {
//                sum = it.ppeValue
//            }
//        }
//        extraList.add(
//            WorkPayPeriodExtras(
//                it.workPayPeriodExtraId,
//                it.ppePayPeriodId,
//                it.ppeExtraTypeId,
//                it.ppeName,
//                it.ppeAppliesTo,
//                it.ppeAttachTo,
//                sum,
//                it.ppeIsFixed,
//                it.ppeIsCredit,
//                it.ppeIsDeleted,
//                it.ppeUpdateTime
//            )
//        )
//    }
//
//    private fun fillExtras(payCalculations: PayCalculations) {
//        CoroutineScope(Dispatchers.Main).launch {
//            val extrasList =
//                findExtras(payCalculations)
////            Log.d(TAG, "Before delay size is ${extrasList.size}")
//            delay(WAIT_1000)
////            Log.d(TAG, "AFTER delay size is ${extrasList.size}")
//            fillCredits(extrasList)
//            fillDeductions(payCalculations, extrasList)
//            mainActivity.mainViewModel.setPayPeriodExtraList(extrasList)
//        }
//    }
//
//    private fun fillCredits(
//        extraList: ArrayList<WorkPayPeriodExtras>,
//    ) {
//        val creditList = ArrayList<WorkPayPeriodExtras>()
//        var creditTotal = 0.0
//        for (extra in extraList) {
//            if (extra.ppeIsCredit) {
//                creditList.add(extra)
//                if (!extra.ppeIsDeleted) {
//                    creditTotal += extra.ppeValue
//                }
//            }
//        }
//        CoroutineScope(Dispatchers.Main).launch {
//            delay(WAIT_250)
//            val creditListAdapter = PayDetailExtraAdapter(
//                mainActivity, creditList, mView, this@PayDetailsFragment2
//            )
//            binding.apply {
//                rvCredits.layoutManager = LinearLayoutManager(mView.context)
//                rvCredits.adapter = creditListAdapter
//                tvCreditTotal.text = cf.displayDollars(creditTotal)
//            }
//        }
//    }
//
//    private fun fillDeductions(
//        payCalculations: PayCalculations,
//        extraList: ArrayList<WorkPayPeriodExtras>,
//    ) {
//        val debitList = ArrayList<WorkPayPeriodExtras>()
//        var debitTotal = 0.0
//        for (extra in extraList) {
//            if (!extra.ppeIsCredit) {
//                debitList.add(extra)
//                if (!extra.ppeIsDeleted) {
//                    debitTotal += extra.ppeValue
//                }
//            }
//        }
//        val taxList = ArrayList<ExtraAndTotal>()
//        for (tax in payCalculations.tax.getTaxList()
//        ) {
//            taxList.add(
//                ExtraAndTotal(
//                    tax.taxType, tax.amount
//                )
//            )
//            debitTotal += tax.amount
//        }
//        CoroutineScope(Dispatchers.Main).launch {
//            binding.apply {
//                delay(WAIT_250)
//                val deductionListAdapter = PayDetailExtraAdapter(
//                    mainActivity, debitList, mView, this@PayDetailsFragment2
//                )
//                rvDebits.layoutManager = LinearLayoutManager(mView.context)
//                rvDebits.adapter = deductionListAdapter
//                delay(WAIT_250)
//                val taxListAdapter = PayDetailTaxAdapter(taxList)
//                rvTax.layoutManager = LinearLayoutManager(mView.context)
//                rvTax.adapter = taxListAdapter
//                delay(WAIT_250)
//                tvDebitTotal.text = cf.displayDollars(debitTotal)
//            }
//        }
//    }
//
//    private fun fillValues() {
//        binding.apply {
//            CoroutineScope(Dispatchers.Main).launch {
//                delay(WAIT_500)
//                if (mainActivity.mainViewModel.getEmployer() != null) {
//                    curEmployer = mainActivity.mainViewModel.getEmployer()!!
//                    for (i in 0 until spEmployers.adapter.count) {
//                        if (spEmployers.getItemAtPosition(i) == curEmployer!!.employerName) {
//                            spEmployers.setSelection(i)
//                            break
//                        }
//                    }
//                }
//                delay(WAIT_500)
//                if (mainActivity.mainViewModel.getCutOffDate() != null) {
//                    curCutOff = mainActivity.mainViewModel.getCutOffDate()!!
//                    for (i in 0 until spCutOff.adapter.count) {
//                        if (spCutOff.getItemAtPosition(i) == curCutOff) {
//                            spCutOff.setSelection(i)
//                            break
//                        }
//                    }
//                }
//                valuesFilled = true
//            }
//        }
//    }
//
//    private fun fillPayDayDate() {
//        if (curCutOff != "" && curEmployer != null) {
//            binding.apply {
//                val display = "Pay Day is " +
//                        df.getDisplayDate(
//                            LocalDate.parse(curCutOff)
//                                .plusDays(curEmployer!!.cutoffDaysBefore.toLong()).toString()
//                        )
//                tvPaySummary.text = display
//            }
//        }
//    }
//
//
//    private fun gotoEmployerAdd() {
//        mainActivity.mainViewModel.setCallingFragment(TAG)
//        mView.findNavController().navigate(
//            PayDetailsFragmentDirections
//                .actionPayDetailsFragmentToEmployerAddFragment()
//        )
//    }
//
//    private fun selectEmployer() {
//        binding.apply {
//            spEmployers.onItemSelectedListener =
//                object : AdapterView.OnItemSelectedListener {
//                    override fun onItemSelected(
//                        parent: AdapterView<*>?,
//                        view: View?,
//                        position: Int,
//                        id: Long
//                    ) {
//                        if (spEmployers.selectedItem.toString() !=
//                            getString(R.string.add_new_employer)
//                        ) {
//                            CoroutineScope(Dispatchers.IO).launch {
//                                curEmployer = mainActivity.employerViewModel.findEmployer(
//                                    spEmployers.selectedItem.toString()
//                                )
//                            }
//                            CoroutineScope(Dispatchers.Main).launch {
//                                delay(WAIT_100)
//                                if (valuesFilled) mainActivity.mainViewModel.setEmployer(curEmployer)
//                                mainActivity.title = getString(R.string.pay_details) +
//                                        " for ${spEmployers.selectedItem}"
//                                fillCutOffDates(curEmployer)
//                            }
//                        } else {
//                            gotoEmployerAdd()
//                        }
//                    }
//
//                    override fun onNothingSelected(parent: AdapterView<*>?) {
////                        fillEmployers()
//                    }
//                }
//        }
//    }
//
//
//    private fun fillCutOffDates(employer: Employers?) {
//        if (employer != null) {
//            binding.apply {
//                val cutOffAdapter = ArrayAdapter<Any>(
//                    mView.context,
//                    R.layout.spinner_item_bold
//                )
//                mainActivity.payDayViewModel.getCutOffDates(employer.employerId).observe(
//                    viewLifecycleOwner
//                ) { dates ->
//                    cutOffAdapter.clear()
//                    cutOffs.clear()
//                    cutOffAdapter.notifyDataSetChanged()
//                    dates.listIterator().forEach {
//                        cutOffAdapter.add(it.ppCutoffDate)
//                    }
//                    if (dates.isEmpty()
//                    ) {
//                        AlertDialog.Builder(mView.context)
//                            .setTitle("No pay days to view")
//                            .setMessage(
//                                "Since there are no pay periods set, you will be sent " +
//                                        "to the time sheet to create a new one."
//                            )
//                            .setPositiveButton("Ok") { _, _ ->
//                                mView.findNavController().navigate(
//                                    PayDetailsFragmentDirections
//                                        .actionPayDetailsFragmentToTimeSheetFragment()
//                                )
//                            }
//                    }
//                }
//                spCutOff.adapter = cutOffAdapter
//            }
//        }
//    }
//
//    private fun fillEmployers() {
//        val employerAdapter = ArrayAdapter<String>(
//            mView.context,
//            R.layout.spinner_item_bold
//        )
//        mainActivity.employerViewModel.getEmployers().observe(
//            viewLifecycleOwner
//        ) { employers ->
//            employerAdapter.clear()
//            employerAdapter.notifyDataSetChanged()
//            employers.listIterator().forEach {
//                employerAdapter.add(it.employerName)
//                curEmployer = employers[0]
//            }
//            employerAdapter.add(getString(R.string.add_new_employer))
//        }
//        binding.spEmployers.adapter = employerAdapter
//    }
//
//    override fun onStop() {
//        mainActivity.mainViewModel.setEmployer(curEmployer)
//        mainActivity.mainViewModel.setCutOffDate(curCutOff)
//        super.onStop()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        _binding = null
//    }
//}