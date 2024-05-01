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
) : IPayCalculations {
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
    private var payRate = 0.0

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

    override fun getDebitExtraAndTotalByPay() = extraDebitCalculations.getDebitList()
    override fun getDebitTotalsByPay(): Double = extraDebitCalculations.getDebitTotalsByPay()
    override fun getCreditExtraAndTotalsByDate() =
        extraCreditCalculations.getCreditExtraAndTotalsByDate()

    override fun getCreditExtrasAndTotalsByPay() =
        extraCreditCalculations.getCreditExtrasAndTotalsByPay()

    override fun getCreditTotalAll() = extraCreditCalculations.getCreditTotal()
    override fun getHoursWorked() = hourlyCalculations.getHoursWorked()
    override fun getHoursAll() = hourlyCalculations.getHoursAll()
    override fun getHoursReg() = hourlyCalculations.getHoursReg()
    override fun getHoursOt() = hourlyCalculations.getHoursOt()
    override fun getHoursDblOt() = hourlyCalculations.getHoursDblOt()
    override fun getHoursStat() = hourlyCalculations.getHoursStat()
    override fun getDaysWorked() = hourlyCalculations.getDaysWorked()
    override fun getPayRate(): Double {
        return payRate
    }

    override fun getPayReg() = hourlyPayCalculations.getPayReg()
    override fun getPayOt() = hourlyPayCalculations.getPayOt()
    override fun getPayDblOt() = hourlyPayCalculations.getPayDblOt()
    override fun getPayHourly() = hourlyPayCalculations.getPayHourly()
    override fun getPayStat() = hourlyPayCalculations.getPayStat()
    override fun getPayGross(): Double {
        return if (getPayHourly() > 0.0) {
            getPayHourly() + getCreditTotalAll()
        } else {
            0.0
        }
    }

    override fun getPayTimeWorked() = hourlyPayCalculations.getPayTimeWorked()
    override fun getAllTaxDeductions() = taxCalculations.getAllTaxDeductions()
    override fun getTaxList() = taxCalculations.getTaxList()

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