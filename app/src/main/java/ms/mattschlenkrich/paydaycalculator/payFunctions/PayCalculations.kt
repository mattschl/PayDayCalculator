package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import ms.mattschlenkrich.paydaycalculator.database.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.PayPeriodHourlySummary
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.database.model.tax.TaxAndAmount
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import kotlin.math.round

class PayCalculations(
    mainActivity: MainActivity,
    mView: View,
    currentPayPeriod: PayPeriods,
) : IPayCalculations {
    private var workDateList = ArrayList<WorkDates>()
    private val hourly = PayPeriodHourlySummary(
        0, 0.0, 0.0,
        0.0, 0.0, 0.0,
    )
    private var creditList: ArrayList<ExtraAndTotal>
    private var creditTotal = 0.0

    init {
        val workDateCalculations = WorkDateCalculations(
            mainActivity, mView, currentPayPeriod
        )
        workDateList = workDateCalculations.processWorkDates()
        hourly.daysWorked = workDateCalculations.getDaysWorked()
        hourly.hoursReg = workDateCalculations.getHoursReg()
        hourly.hoursOt = workDateCalculations.getHoursOt()
        hourly.hoursDblOt = workDateCalculations.getHoursDblOt()
        hourly.hoursStat = workDateCalculations.getHoursStat()

        val employerPayRate = EmployerPayRate(
            mainActivity,
            mView,
            currentPayPeriod
        )
        hourly.payRate = employerPayRate.getPayRate()

        val creditCalculations = CreditCalculations(
            mainActivity,
            mView,
            currentPayPeriod,
            workDateList,
            hourly,
        )
        creditList = creditCalculations.getCreditList() as ArrayList
        creditTotal = creditCalculations.getCreditTotal()

        val percentCreditCalculations =
            PercentCreditCalculations(
                mainActivity,
                mView,
                currentPayPeriod,
                hourly,
                creditCalculations
            )
        creditList.add(percentCreditCalculations.getExtraList()[0])
    }

    override fun getDebitExtrasListByPay(): List<ExtraAndTotal>? {
        TODO("Not yet implemented")
    }

    override fun getDebitTotalsByPay(): Double {
        TODO("Not yet implemented")
    }

    override fun getCreditExtrasListByPercentageOfAll(): List<ExtraAndTotal>? {
        TODO("Not yet implemented")
    }

    override fun getCreditExtrasListByDate(): List<ExtraAndTotal>? {
        TODO("Not yet implemented")
    }

    override fun getCreditExtrasListByPay(): List<ExtraAndTotal>? {
        TODO("Not yet implemented")
    }

    override fun getCredits(): List<ExtraAndTotal> {
        return creditList
    }

    override fun getCreditTotalAll(): Double {
        var total = 0.0
        for (credit in creditList) {
            total += credit.amount
        }
        return total
    }

    override fun getHoursWorked(): Double {
        return hourly.hoursReg + hourly.hoursOt + hourly.hoursDblOt
    }

    override fun getHoursAll(): Double {
        return getHoursWorked() + hourly.hoursStat
    }

    override fun getHoursReg(): Double {
        return hourly.hoursReg
    }

    override fun getHoursOt(): Double {
        return hourly.hoursOt
    }

    override fun getHoursDblOt(): Double {
        return hourly.hoursDblOt
    }

    override fun getHoursStat(): Double {
        return hourly.hoursStat
    }

    override fun getDaysWorked(): Int {
        return hourly.daysWorked
    }

    override fun getPayRate(): Double {
        return hourly.payRate
    }

    override fun getPayReg(): Double {
        return hourly.payRate * hourly.hoursReg
    }

    override fun getPayOt(): Double {
        return round(hourly.payRate * 150) / 100 * hourly.hoursOt
    }

    override fun getPayDblOt(): Double {
        return round(hourly.payRate * 200) / 100 * hourly.hoursDblOt
    }

    override fun getPayStat(): Double {
        return hourly.payRate * hourly.hoursReg
    }

    override fun getPayAllHourly(): Double {
        return getPayAllHourly() + getPayStat()
    }

    override fun getPayTimeWorked(): Double {
        return getPayReg() + getPayOt() + getPayDblOt()
    }

    override fun getPayGross(): Double {
        TODO("Not yet implemented")
    }

    override fun getTaxList(): List<TaxAndAmount>? {
        TODO("Not yet implemented")
    }

    override fun getAllTaxDeductions(): Double {
        TODO("Not yet implemented")
    }
}