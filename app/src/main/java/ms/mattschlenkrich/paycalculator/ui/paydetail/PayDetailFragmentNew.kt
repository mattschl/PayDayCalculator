package ms.mattschlenkrich.paycalculator.ui.paydetail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.AppliesToFrequencies
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.FRAG_PAY_DETAILS
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.WAIT_100
import ms.mattschlenkrich.paycalculator.common.WAIT_1000
import ms.mattschlenkrich.paycalculator.common.WAIT_1500
import ms.mattschlenkrich.paycalculator.common.WAIT_250
import ms.mattschlenkrich.paycalculator.common.WAIT_500
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraContainer
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkDateExtrasAndDates
import ms.mattschlenkrich.paycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.database.viewModel.EmployerViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.MainViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.database.viewModel.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.databinding.FragmentPayDetailsBinding
import ms.mattschlenkrich.paycalculator.logic.payfunctions.IPayCalculations
import ms.mattschlenkrich.paycalculator.logic.payfunctions.PayCalculationsAsync
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import ms.mattschlenkrich.paycalculator.ui.paydetail.adapter.PayDetailExtraContainerAdapter
import ms.mattschlenkrich.paycalculator.ui.paydetail.adapter.PayDetailTaxAdapter
import java.time.LocalDate

private const val TAG = FRAG_PAY_DETAILS

class PayDetailFragmentNew : Fragment(R.layout.fragment_pay_details), IPayDetailsFragment {

    private var _binding: FragmentPayDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mView: View
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var employerViewModel: EmployerViewModel
    private lateinit var payDayViewModel: PayDayViewModel
    private lateinit var workExtraViewModel: WorkExtraViewModel
    private var curEmployer: Employers? = null
    private var curPayPeriod: PayPeriods? = null
    private val cutOffs = ArrayList<String>()
    private var creditTotal = 0.0
    private var curCutOff = ""
    private var valuesFilled = false
    private val nf = NumberFunctions()
    private val df = DateFunctions()
    private val defaultScope = CoroutineScope(Dispatchers.Default)
    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayDetailsBinding.inflate(
            inflater, container, false
        )
        mView = binding.root
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        employerViewModel = mainActivity.employerViewModel
        payDayViewModel = mainActivity.payDayViewModel
        workExtraViewModel = mainActivity.workExtraViewModel
        mainActivity.title = getString(R.string.pay_details)
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
            mView.context, R.layout.spinner_item_bold
        )
        employerViewModel.getEmployers().observe(viewLifecycleOwner) { employers ->
            employerAdapter.clear()
            employerAdapter.notifyDataSetChanged()
            employers.listIterator().forEach {
                employerAdapter.add(it.employerName)
            }
            curEmployer = employers.first()
            employerAdapter.add(getString(R.string.add_new_employer))
        }
        binding.spEmployers.adapter = employerAdapter
    }

    private fun setClickActions() {
        setMenuAction()
        binding.apply {
            fabAddExtra.setOnClickListener { chooseToGotoExtraAdd(true) }
            fabAddDeduction.setOnClickListener { chooseToGotoExtraAdd(false) }
        }
        onSelectEmployer()
        onSelectCutOffDate()
    }

    private fun setMenuAction() {
        mainActivity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_delete, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        confirmDeletingCutoffDate()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.CREATED)
    }

    private fun confirmDeletingCutoffDate() {
        android.app.AlertDialog.Builder(mView.context)
            .setTitle(getString(R.string.confirm_delete_pay_period)).setMessage(
                getString(R.string.warning_) + "\n" + getString(R.string.this_action_cannot_be_undone) + getString(
                    R.string.all_the_work_dates_will_have_to_be_re_entered
                )
            ).setPositiveButton(getString(R.string.delete)) { _, _ ->
                deletePayDay()
            }.setNegativeButton(getString(R.string.cancel), null).create().show()
    }

    private fun deletePayDay() {
        payDayViewModel.updatePayPeriod(
            PayPeriods(
                curPayPeriod!!.payPeriodId,
                curPayPeriod!!.ppCutoffDate,
                curPayPeriod!!.ppEmployerId,
                true,
                df.getCurrentTimeAsString()
            )
        )
        populateCutOffDates(curEmployer)
    }

    private fun chooseToGotoExtraAdd(isCredit: Boolean) {
        AlertDialog.Builder(mView.context).setTitle(getString(R.string.warning_)).setMessage(
            getString(
                R.string.it_is_best_to_add_custom_extras_only_after_all_the_work_hours_have_been_entered
            )
        ).setPositiveButton(getString(R.string.continue_)) { _, _ ->
            gotoExtraAddFragment(isCredit)
        }.setNegativeButton(getString(R.string.cancel), null).show()
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
//                            Log.d(TAG, "onSelectEmployer: ${curEmployer?.employerName}")
//                            mainViewModel.setEmployer(curEmployer)
                            mainActivity.title =
                                getString(R.string.pay_details) + getString(R.string._for_) + spEmployers.selectedItem
                            populateCutOffDates(curEmployer)
                        }
                    } else {
                        gotoEmployerAdd()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
    }

    private fun populateCutOffDates(employer: Employers?) {
        if (employer != null) {
            mainScope.launch {
                binding.apply {
                    val cutOffAdapter = ArrayAdapter<Any>(
                        mView.context, R.layout.spinner_item_bold
                    )
                    payDayViewModel.getCutOffDates(employer.employerId).observe(
                        viewLifecycleOwner
                    ) { dates ->
                        cutOffs.clear()
                        cutOffAdapter.clear()
                        dates.listIterator().forEach {
                            cutOffAdapter.add(it.ppCutoffDate)
                        }
                    }
                    delay(WAIT_250)
                    if (cutOffAdapter.isEmpty) {
                        nextStepsIfDateListIsEmpty()
                    }
                    spCutOff.adapter = cutOffAdapter
                }
            }
        }
    }

    private fun onSelectCutOffDate() {
        binding.apply {
            spCutOff.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (curCutOff != spCutOff.selectedItem.toString()) {
                        curCutOff = spCutOff.selectedItem.toString()
//                        if (valuesFilled) mainActivity.mainViewModel.setCutOffDate(curCutOff)
                        getCurrentPayPeriodObject()
                        populatePayDayDate()
                        populatePayDetails()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
    }

    private fun getCurrentPayPeriodObject() {
        payDayViewModel.getPayPeriod(curCutOff, curEmployer!!.employerId).observe(
            viewLifecycleOwner
        ) { payPeriod ->
            curPayPeriod = payPeriod
            mainActivity.mainViewModel.setPayPeriod(payPeriod)
        }
    }

    private fun populatePayDayDate() {
        if (curCutOff != "" && curEmployer != null) {
            binding.apply {
                val display = getString(R.string.pay_day_is_) + df.getDisplayDate(
                    LocalDate.parse(curCutOff).plusDays(curEmployer!!.cutoffDaysBefore.toLong())
                        .toString()
                )
                tvPaySummary.text = display
            }
        }
    }

    override fun populatePayDetails() {
        mainScope.launch {
            getCurrentPayPeriodObject()
            delay(WAIT_250)
            val payCalculations = PayCalculationsAsync(
                mainActivity, curEmployer!!, curPayPeriod!!
            )
            delay(WAIT_1000)
            populateExtras(payCalculations)
            delay(WAIT_1500)
            binding.apply {
                if (payCalculations.getPayReg() > 0.0) {
                    llRegPay.visibility = View.VISIBLE
                    tvRegHours.text = nf.getNumberFromDouble(payCalculations.getHoursReg())
                    tvRegRate.text = nf.displayDollars(payCalculations.getPayRate())
                    tvRegPay.text = nf.displayDollars(payCalculations.getPayReg())
                } else {
                    llRegPay.visibility = View.GONE
                }
                if (payCalculations.getPayOt() > 0.0) {
                    llOtPay.visibility = View.VISIBLE
                    tvOtHours.text = nf.getNumberFromDouble(payCalculations.getHoursOt())
                    tvOtRate.text = nf.displayDollars(payCalculations.getPayRate() * 1.5)
                    tvOTPay.text = nf.displayDollars(payCalculations.getPayOt())
                } else {
                    llOtPay.visibility = View.GONE
                }
                if (payCalculations.getPayDblOt() > 0.0) {
                    llDblOtPay.visibility = View.VISIBLE
                    tvDblOtHours.text = nf.getNumberFromDouble(payCalculations.getHoursDblOt())
                    tvDblOtRate.text = nf.displayDollars(payCalculations.getPayRate() * 2)
                    tvDblOtPay.text = nf.displayDollars(payCalculations.getPayDblOt())
                } else {
                    llDblOtPay.visibility = View.GONE
                }
                if (payCalculations.getPayStat() > 0.0) {
                    llStatPay.visibility = View.VISIBLE
                    tvStatHours.text = nf.getNumberFromDouble(payCalculations.getHoursStat())
                    tvStatRate.text = nf.displayDollars(payCalculations.getPayRate())
                    tvStatPay.text = nf.displayDollars(payCalculations.getPayStat())
                } else {
                    llStatPay.visibility = View.GONE
                }
                tvHourlyTotal.text = nf.displayDollars(payCalculations.getPayAllHourly())
                var display =
                    getString(R.string.gross_) + nf.displayDollars(payCalculations.getPayGross())
                tvGrossPay.text = display
                display = nf.displayDollars(
                    -payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()
                )
                tvCreditTotal.text = nf.displayDollars(
                    payCalculations.getCreditTotalAll()
                )
                tvDeductions.text = display
                tvDeductions.setTextColor(Color.RED)
                display = getString(R.string.net_) + nf.displayDollars(
                    payCalculations.getPayGross() - payCalculations.getDebitTotalsByPay() - payCalculations.getAllTaxDeductions()
                )
                tvNetPay.text = display
            }
        }
    }

    private fun nextStepsIfDateListIsEmpty() {
        AlertDialog.Builder(mView.context).setTitle(getString(R.string.no_pay_days_to_view))
            .setMessage(
                getString(R.string.since_there_are_no_pay_periods_set_you_will_be_sent_to_the_time_sheet_to_create_a_new_one)
            ).setPositiveButton(getString(R.string.ok)) { _, _ ->
                gotoTimeSheetFragment()
            }.show()
    }

    private fun populateExtras(payCalculations: IPayCalculations) {
        mainScope.launch {
            val extrasList = processExtras(payCalculations)
            val creditList = payCalculations.getCredits()
            delay(WAIT_250)
            populateCredits(creditList)
            populateDeductions(payCalculations)
            mainViewModel.setPayPeriodExtraList(extrasList)
        }
    }

    private fun processExtras(payCalculations: IPayCalculations): ArrayList<WorkPayPeriodExtras> {
        val extraList = mutableListOf<WorkPayPeriodExtras>()
        mainScope.launch {
            delay(WAIT_250)
            payDayViewModel.getPayPeriodExtras(curPayPeriod!!.payPeriodId).observe(
                viewLifecycleOwner
            ) { credit ->
                credit.listIterator().forEach {
                    processExtrasByManuallyAdded(it, payCalculations, extraList)
                }
            }
            val workDateExtrasAndDates = ArrayList<WorkDateExtrasAndDates>()
            payDayViewModel.getWorkDateExtrasAndDates(curCutOff).observe(
                viewLifecycleOwner
            ) { extraPlusDate ->
                extraPlusDate.listIterator().forEach {
                    workDateExtrasAndDates.add(it)
                }
            }
            delay(WAIT_250)
            processExtrasByDay(workDateExtrasAndDates, extraList)
            delay(WAIT_250)
            workExtraViewModel.getExtraTypesAndDef(
                curEmployer!!.employerId, curCutOff, 3
            ).observe(viewLifecycleOwner) { extras ->
                extras.listIterator().forEach {
                    processExtraByPayPeriod(extraList, it, payCalculations)
                }
            }
            delay(WAIT_100)
        }
        return extraList as ArrayList<WorkPayPeriodExtras>
    }

    private fun populateCredits(creditList: List<ExtraContainer>) {
        mainScope.launch {
            delay(WAIT_250)
            val creditListAdapter = PayDetailExtraContainerAdapter(
                mainActivity, mView, curPayPeriod!!, creditList, this@PayDetailFragmentNew
            )
            binding.apply {
                rvCredits.layoutManager = LinearLayoutManager(mView.context)
                rvCredits.adapter = creditListAdapter
                tvCreditTotal.text = nf.displayDollars(creditTotal)
            }
        }
    }

    private fun populateDeductions(
        payCalculations: IPayCalculations,
    ) {
//        val debitList = getDeductions(
//            payCalculations, extraList
//        )
        val debitList = payCalculations.getDebits()
        mainScope.launch {
            binding.apply {
                delay(WAIT_500)
                val deductionListAdapter = PayDetailExtraContainerAdapter(
                    mainActivity, mView, curPayPeriod!!, debitList, this@PayDetailFragmentNew
                )
                rvDebits.layoutManager = LinearLayoutManager(mView.context)
                rvDebits.adapter = deductionListAdapter
                delay(WAIT_250)
                val taxListAdapter = PayDetailTaxAdapter(payCalculations.getTaxList()!!)
                rvTax.layoutManager = LinearLayoutManager(mView.context)
                rvTax.adapter = taxListAdapter
                delay(WAIT_250)
                tvDebitTotal.text = nf.displayDollars(
                    payCalculations.getDebitTotalsByPay() + payCalculations.getAllTaxDeductions()
                )
            }
        }
    }

    private fun processExtrasByManuallyAdded(
        workPayPeriodExtras: WorkPayPeriodExtras,
        payCalculations: IPayCalculations,
        extraList: MutableList<WorkPayPeriodExtras>
    ) {
        var sum = 0.0
        payCalculations.apply {
            when (workPayPeriodExtras.ppeAppliesTo) {
                AppliesToFrequencies.HOURLY.value -> {
                    sum = if (workPayPeriodExtras.ppeIsFixed) {
                        getHoursWorked() * workPayPeriodExtras.ppeValue
                    } else {
                        getPayTimeWorked() * workPayPeriodExtras.ppeValue / 100
                    }
                }

                AppliesToFrequencies.DAILY.value -> {
                    sum = if (workPayPeriodExtras.ppeIsFixed) {
                        getDaysWorked() * workPayPeriodExtras.ppeValue
                    } else {
                        getPayTimeWorked() * workPayPeriodExtras.ppeValue / 100
                    }
                }

                AppliesToFrequencies.PER_PAY_FOR_HOURLY_WAGES.value -> {
                    sum = if (workPayPeriodExtras.ppeIsFixed) {
                        payCalculations.getDaysWorked() * workPayPeriodExtras.ppeValue
                    } else {
                        payCalculations.getPayAllHourly() * workPayPeriodExtras.ppeValue / 100
                    }
                }
            }
        }
        workPayPeriodExtras.apply {
            extraList.add(
                WorkPayPeriodExtras(
                    workPayPeriodExtraId,
                    ppePayPeriodId,
                    ppeExtraTypeId,
                    ppeName,
                    ppeAppliesTo,
                    ppeAttachTo,
                    sum,
                    ppeIsFixed,
                    ppeIsCredit,
                    ppeIsDeleted,
                    ppeUpdateTime,
                )
            )
        }
    }

    private fun processExtrasByDay(
        workDateExtrasAndDates: ArrayList<WorkDateExtrasAndDates>,
        extraList: MutableList<WorkPayPeriodExtras>
    ) {
        var subTotal = 0.0
        for (i in 0 until workDateExtrasAndDates.size) {
            workDateExtrasAndDates[i].apply {
                subTotal += when (workDateExtra.wdeAppliesTo) {
                    0 -> {
                        (workDate.wdRegHours + workDate.wdOtHours + workDate.wdDblOtHours) * workDateExtra.wdeValue / if (workDateExtra.wdeIsFixed) 1 else 100
                    }

                    1 -> {
                        workDateExtra.wdeValue / if (workDateExtra.wdeIsFixed) 1 else 100
                    }

                    else -> {
                        0.0
                    }
                }
                if (i < workDateExtrasAndDates.size - 1) {
                    if (workDateExtra.wdeName != workDateExtrasAndDates[i + 1].workDateExtra.wdeName) {
                        extraList.add(
                            addExtraManually(subTotal)
                        )
                        subTotal = 0.0
                    }
                } else {
                    extraList.add(
                        addExtraManually(subTotal)
                    )
                    subTotal = 0.0
                }
            }
        }
    }

    private fun processExtraByPayPeriod(
        extraList: MutableList<WorkPayPeriodExtras>,
        extraDefinitionAndType: ExtraDefinitionAndType,
        payCalculations: IPayCalculations
    ) {
        var notFound = true
        for (extra in extraList) {
            if (extra.ppeName == extraDefinitionAndType.extraType.wetName) {
                notFound = false
            }
        }
        if (notFound) {
            payCalculations.apply {
                when (extraDefinitionAndType.extraType.wetAppliesTo) {
                    0 -> {
                        val sum = if (extraDefinitionAndType.definition.weIsFixed) {
                            getHoursWorked() * extraDefinitionAndType.definition.weValue
                        } else {
                            getPayTimeWorked() * extraDefinitionAndType.definition.weValue / 100
                        }
                        extraList.add(
                            addWorkPayPeriodExtra(extraDefinitionAndType, sum)
                        )
                    }

                    1 -> {
                        val sum = if (extraDefinitionAndType.definition.weIsFixed) {
                            getDaysWorked() * extraDefinitionAndType.definition.weValue
                        } else {
                            getPayTimeWorked() * extraDefinitionAndType.definition.weValue / 100
                        }
                        extraList.add(
                            addWorkPayPeriodExtra(extraDefinitionAndType, sum)
                        )
                    }

                    3 -> {
                        if (extraDefinitionAndType.definition.weIsFixed) {
                            extraList.add(
                                addWorkPayPeriodExtra(
                                    extraDefinitionAndType,
                                    extraDefinitionAndType.definition.weValue
                                )
                            )
                        } else {
                            val sum =
                                getPayAllHourly() * extraDefinitionAndType.definition.weValue / 100
                            extraList.add(
                                addWorkPayPeriodExtra(extraDefinitionAndType, sum)
                            )
                        }
                    }

                    4 -> {
                        val sum = getPayGross() * extraDefinitionAndType.definition.weValue / 100
                        extraList.add(
                            addWorkPayPeriodExtra(extraDefinitionAndType, sum)
                        )
                    }
                }
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
//                    Log.d("TAG", "populateFromHistory: ${curEmployer!!.employerName}")
//                    Log.d("TAG", "populateFromHistory: number in loop ${spEmployers.adapter.count}")
                    for (i in 0 until spEmployers.adapter.count) {
//                        Log.d("TAG", "populateFromHistory: ${spEmployers.getItemAtPosition(i)}")
                        if (spEmployers.getItemAtPosition(i) == curEmployer!!.employerName) {
//                            Log.d(
//                                "TAG",
//                                "populateFromHistory: in loop ${spEmployers.getItemAtPosition(i)}"
//                            )
                            spEmployers.setSelection(i)
                            populatePayDetails()
                            break
                        }
                    }
                    delay(WAIT_250)
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
                }
                valuesFilled = true
            }
//            }
        }
    }


    private fun WorkDateExtrasAndDates.addExtraManually(
        subTotal: Double
    ) = WorkPayPeriodExtras(
        nf.generateRandomIdAsLong(),
        curPayPeriod!!.payPeriodId,
        null,
        workDateExtra.wdeName,
        workDateExtra.wdeAppliesTo,
        1,
        subTotal,
        true,
        workDateExtra.wdeIsCredit,
        false,
        df.getCurrentTimeAsString()
    )

    private fun addWorkPayPeriodExtra(
        it: ExtraDefinitionAndType, sum: Double
    ) = WorkPayPeriodExtras(
        nf.generateRandomIdAsLong(),
        curPayPeriod!!.payPeriodId,
        it.extraType.workExtraTypeId,
        it.extraType.wetName,
        it.extraType.wetAppliesTo,
        it.extraType.wetAttachTo,
        sum,
        it.definition.weIsFixed,
        it.extraType.wetIsCredit,
        !it.extraType.wetIsDefault,
        df.getCurrentTimeAsString()
    )

    private fun gotoEmployerAdd() {
        mainViewModel.setCallingFragment(TAG)
        gotoEmployerAddFragment()
    }

    private fun gotoExtraAddFragment(isCredit: Boolean) {
        mainViewModel.apply {
            payDayViewModel.getPayPeriod(
                curCutOff, curEmployer!!.employerId
            ).observe(viewLifecycleOwner) { payPeriod ->
                setPayPeriod(payPeriod)
            }
            setEmployer(curEmployer!!)
            setIsCredit(isCredit)
            gotoPayPeriodExtraAddFragment()
        }
    }

    private fun gotoEmployerAddFragment() {
        mView.findNavController().navigate(
            PayDetailFragmentNewDirections.actionPayDetailFragmentNewToEmployerAddFragment()
        )
    }

    private fun gotoTimeSheetFragment() {
        mView.findNavController().navigate(
            PayDetailFragmentNewDirections.actionPayDetailFragmentNewToTimeSheetFragment()

        )
    }

    private fun gotoPayPeriodExtraAddFragment() {
        mView.findNavController().navigate(
            PayDetailFragmentNewDirections.actionPayDetailFragmentNewToPayPeriodExtraAddFragment()
        )
    }

    override fun gotoPeriodExtraUpdateFragment() {
        mView.findNavController().navigate(
            PayDetailFragmentNewDirections.actionPayDetailFragmentNewToPayPeriodExtraUpdateFragment()
        )
    }

    override fun onStop() {
        mainViewModel.apply {
            setEmployer(curEmployer)
            setCutOffDate(curCutOff)
        }
        defaultScope.cancel()
        mainScope.cancel()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}