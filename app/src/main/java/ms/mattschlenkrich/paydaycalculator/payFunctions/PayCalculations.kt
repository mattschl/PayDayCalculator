package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxTypes
import ms.mattschlenkrich.paydaycalculator.model.tax.WorkTaxRules
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

//private const val TAG = "PayCalculations"

class PayCalculations(
    private val mainActivity: MainActivity,
    private val employer: Employers,
    private val cutOff: String,
    private val mView: View,
) {
    private val workDates = ArrayList<WorkDates>()
    private lateinit var extraTypes: ArrayList<WorkExtraTypes>

    private val taxRules = ArrayList<WorkTaxRules>()
    private val taxTypes = ArrayList<TaxTypes>()
    private lateinit var hourlyCalculations: HourlyCalculations
    private lateinit var extraCalculations: ExtraCalculations
    private lateinit var extraCreditCalculations: ExtraCreditCalculations
    private lateinit var extraDebitCalculations: ExtraDebitCalculations
    private lateinit var hourlyPayCalculations: HourlyPayCalculations
    private lateinit var taxCalculations: TaxCalculations
    var payRate = 0.0
    val hours = Hours()
    val pay = Pay()
    val credits = Credits()
    val tax = Tax()
    val deductions = Deductions()

    init {
        CoroutineScope(Dispatchers.Main).launch {
            findWorkDates()
            findRate()
            findTaxRates()
            performHourlyCalculations()
            performHourlyPayCalculations()
            performExtraCalculations()
            performCreditCalculations()
            performDebitCalculations()
            performTaxCalculations()
        }
    }

    private fun performTaxCalculations() {
        taxCalculations = TaxCalculations(
            employer,
            taxRules,
            taxTypes,
            hourlyPayCalculations,
            extraCreditCalculations
        )
    }

    private fun performHourlyPayCalculations() {
        hourlyPayCalculations = HourlyPayCalculations(
            hourlyCalculations,
            payRate
        )
    }

    private fun performDebitCalculations() {
        extraDebitCalculations = ExtraDebitCalculations(
            extraCalculations.getWorkExtrasByPay(),
            hourlyPayCalculations
        )
    }

    private fun performCreditCalculations() {
        extraCreditCalculations = ExtraCreditCalculations(
            hourlyCalculations,
            payRate,
            workDates,
            extraCalculations.getWorkDateExtrasFull(),
            extraCalculations.getWorkExtrasByPay()
        )
    }

    private fun performExtraCalculations() {
        extraCalculations = ExtraCalculations(
            mView, mainActivity, employer, cutOff
        )
        extraTypes = extraCalculations.getExtraTypes()
    }

    private fun performHourlyCalculations() {
        hourlyCalculations = HourlyCalculations(workDates)
    }

    inner class Deductions {
        fun getDebitExtraAndTotalByPay() = extraDebitCalculations.getDebitList()
        fun getDebitTotalsByPay(): Double = extraDebitCalculations.getDebitTotalsByPay()
    }

    inner class Credits {
        fun getCreditExtraAndTotalsByDate() =
            extraCreditCalculations.getCreditExtraAndTotalsByDate()

        fun getCreditExtrasAndTotalsByPay() =
            extraCreditCalculations.getCreditExtrasAndTotalsByPay()

        fun getCreditTotalAll() = extraCreditCalculations.getCreditTotal()
    }

    inner class Hours {
        fun getHoursWorked() = hourlyCalculations.getHoursWorked()
        fun getHoursAll() = hourlyCalculations.getHoursAll()
        fun getHoursReg() = hourlyCalculations.getHoursReg()
        fun getHoursOt() = hourlyCalculations.getHoursOt()
        fun getHoursDblOt() = hourlyCalculations.getHoursDblOt()
        fun getHoursStat() = hourlyCalculations.getHoursStat()
        fun getDaysWorked() = hourlyCalculations.getDaysWorked()
    }

    inner class Pay {
        fun getPayReg() = hourlyPayCalculations.getPayReg()
        fun getPayOt() = hourlyPayCalculations.getPayOt()
        fun getPayDblOt() = hourlyPayCalculations.getPayDblOt()
        fun getPayHourly() = hourlyPayCalculations.getPayHourly()
        fun getPayStat() = hourlyPayCalculations.getPayStat()
        fun getPayGross(): Double {
            return if (getPayHourly() > 0.0) {
                getPayHourly() + credits.getCreditTotalAll()
            } else {
                0.0
            }
        }

        fun getPayTimeWorked() = hourlyPayCalculations.getPayTimeWorked()

    }

    inner class Tax {
        fun getAllTaxDeductions() = taxCalculations.getAllTaxDeductions()
        fun getTaxList() = taxCalculations.getTaxList()
    }

    private fun findRate() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.employerViewModel.getEmployerPayRates(
                employer.employerId
            ).observe(lifecycleOwner) { rates ->
                rates.listIterator().forEach {
                    if (it.eprEffectiveDate <= cutOff) {
                        payRate = it.eprPayRate
                    }
                }
            }
        }
    }

    private fun findWorkDates() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateList(
                employer.employerId, cutOff
            ).observe(lifecycleOwner) { list ->
                workDates.clear()
                list.listIterator().forEach {
                    workDates.add(it)
                }
            }
        }
    }


    private fun findTaxRates() {
        var effectiveDate = ""
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workTaxViewModel.getCurrentEffectiveDate(
                cutOff
            ).observe(lifecycleOwner) { date ->
                effectiveDate = date
            }
        }
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workTaxViewModel.getTaxTypesByEmployer(
                employer.employerId
            ).observe(lifecycleOwner) { types ->
                taxTypes.clear()
                types.listIterator().forEach {
                    taxTypes.add(it)
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_250)
            mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
                mainActivity.workTaxViewModel.getTaxDefByDate(
                    effectiveDate
                ).observe(lifecycleOwner) { list ->
                    taxRules.clear()
                    list.listIterator().forEach {
                        taxRules.add(it)
                    }
                }
            }
        }
    }
}