package ms.mattschlenkrich.paydaycalculator.newPayCalculations

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxAndAmount
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxTypes
import ms.mattschlenkrich.paydaycalculator.model.tax.WorkTaxRules
import ms.mattschlenkrich.paydaycalculator.payFunctions.IPayCalculations
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class NewPayCalculationsRepository(
    private val mainActivity: MainActivity,
    private val employer: Employers,
    private val cutOff: String,
    private val mView: View,
) : IPayCalculations {

    private var hoursReg = 0.0
    private var hoursOt = 0.0
    private var hoursDblOt = 0.0
    private var hoursStat = 0.0
    private var daysWorked = 0
    private var payRate = 0.0
    private var creditTotal = 0.0
    private var debitTotal = 0.0
    private var allTaxDeductions = 0.0
    private var debitExtrasAndTotalsByPay: List<ExtraAndTotal>
    private var creditExtrasAndTotalsByDate: List<ExtraAndTotal>
    private var creditExtrasAndTotalsByPay: List<ExtraAndTotal>
    private var taxList: List<TaxAndAmount>

    init {
        payRate = findRate()
        val workDateList = getWorkDatesList()
        val hourlyCalculations = HourlyCalculations(
            workDateList
        )
        hoursReg = hourlyCalculations.getHoursReg()
        hoursOt = hourlyCalculations.getHoursOt()
        hoursDblOt = hourlyCalculations.getHoursDblOt()
        hoursStat = hourlyCalculations.getHoursStat()
        val extraCalculations = ExtraCalculations(
            mView, mainActivity, employer, cutOff
        )
        val extraCreditsCalculations = ExtraCreditCalculations(
            hourlyCalculations,
            payRate,
            workDateList,
            extraCalculations.getWorkDateExtrasFull(),
            extraCalculations.getWorkExtrasByPay()
        )
        creditExtrasAndTotalsByPay = extraCreditsCalculations
            .getCreditExtrasAndTotalsByPay()
        creditExtrasAndTotalsByDate = extraCreditsCalculations
            .getCreditExtraAndTotalsByDate()
        creditTotal = extraCreditsCalculations.getCreditTotal()
        val hourlyPayCalculations = HourlyPayCalculations(
            hourlyCalculations, payRate
        )
        val extraDebitCalculations = ExtraDebitCalculations(
            extraCalculations.getWorkExtrasByPay(),
            hourlyPayCalculations
        )
        debitExtrasAndTotalsByPay = extraDebitCalculations
            .getDebitList()
        debitTotal = extraDebitCalculations.getDebitTotalsByPay()
        val taxCalculations = TaxCalculations(
            employer,
            getTaxRules(),
            getTaxTypes(),
            hourlyPayCalculations,
            extraCreditsCalculations
        )
        allTaxDeductions = taxCalculations.getAllTaxDeductions()
        taxList = taxCalculations.getTaxList()
    }

    private fun getTaxRules(): List<WorkTaxRules> {
        val taxRules = ArrayList<WorkTaxRules>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workTaxViewModel.getTaxDefByDate(
                getEffectiveDateForTax()
            ).observe(lifecycleOwner) { list ->
                list.listIterator().forEach {
                    taxRules.add(it)
                }
            }
        }
        return taxRules
    }


    private fun getTaxTypes(): List<TaxTypes> {
        val taxTypeList = ArrayList<TaxTypes>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workTaxViewModel.getTaxTypesByEmployer(
                employer.employerId
            ).observe(lifecycleOwner) { types ->
                types.listIterator().forEach {
                    taxTypeList.add(it)
                }
            }
        }
        return taxTypeList.toList()
    }

    private fun getEffectiveDateForTax(): String {
        var effectiveDate = ""
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workTaxViewModel.getCurrentEffectiveDate(
                cutOff
            ).observe(lifecycleOwner) { date ->
                effectiveDate = date
            }
        }
        return effectiveDate
    }

    private fun getWorkDatesList(): List<WorkDates> {
        val workDates = ArrayList<WorkDates>()
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
        return workDates.toList()
    }

    private fun findRate(): Double {
        var rate = 0.0
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.employerViewModel.getEmployerPayRates(
                employer.employerId
            ).observe(lifecycleOwner) { rates ->
                rates.listIterator().forEach {
                    if (it.eprEffectiveDate <= cutOff) {
                        rate = it.eprPayRate
                    }
                }
            }
        }
        return rate
    }

    override fun getDebitExtraAndTotalByPay(): List<ExtraAndTotal> {
        return debitExtrasAndTotalsByPay
    }

    override fun getDebitTotalsByPay(): Double {
        return debitTotal
    }

    override fun getCreditExtraAndTotalsByDate(): List<ExtraAndTotal> {
        return creditExtrasAndTotalsByDate
    }

    override fun getCreditExtrasAndTotalsByPay(): List<ExtraAndTotal> {
        return creditExtrasAndTotalsByPay
    }

    override fun getCreditTotalAll(): Double {
        return creditTotal
    }

    override fun getHoursWorked(): Double {
        return hoursReg + hoursOt + hoursDblOt
    }

    override fun getHoursAll(): Double {
        return getHoursWorked() + hoursStat
    }

    override fun getHoursReg(): Double {
        return hoursReg
    }

    override fun getHoursOt(): Double {
        return hoursOt
    }

    override fun getHoursDblOt(): Double {
        return hoursDblOt
    }

    override fun getHoursStat(): Double {
        return hoursStat
    }

    override fun getDaysWorked(): Int {
        return daysWorked
    }

    override fun getPayRate(): Double {
        return payRate
    }

    override fun getPayReg(): Double {
        return hoursReg * payRate
    }

    override fun getPayOt(): Double {
        return hoursOt * payRate * 1.5
    }

    override fun getPayDblOt(): Double {
        return hoursDblOt * payRate * 2
    }

    override fun getPayHourly(): Double {
        return getPayTimeWorked() + getPayStat()
    }

    override fun getPayStat(): Double {
        return hoursStat * payRate
    }

    override fun getPayGross(): Double {
        return getPayHourly() + getCreditTotalAll()
    }

    override fun getPayTimeWorked(): Double {
        return getPayReg() + getPayOt() + getPayDblOt()
    }

    override fun getAllTaxDeductions(): Double {
        return allTaxDeductions
    }

    override fun getTaxList(): List<TaxAndAmount> {
        return taxList
    }
}