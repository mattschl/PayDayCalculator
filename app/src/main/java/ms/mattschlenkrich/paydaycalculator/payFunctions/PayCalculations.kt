package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import ms.mattschlenkrich.paydaycalculator.database.model.extras.ExtraAndTotal
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
    private var daysWorked = 0
    private var hoursReg = 0.0
    private var hoursOt = 0.0
    private var hoursDblOt = 0.0
    private var hoursStat = 0.0
    private var payRate = 0.0

    init {
        val workDateCalculations = WorkDateCalculations(
            mainActivity, mView, currentPayPeriod
        )
        workDateList = workDateCalculations.processWorkDates()
        daysWorked = workDateCalculations.getDaysWorked()
        hoursReg = workDateCalculations.getHoursReg()
        hoursOt = workDateCalculations.getHoursOt()
        hoursDblOt = workDateCalculations.getHoursDblOt()
        hoursStat = workDateCalculations.getHoursStat()

        val employerPayRate = EmployerPayRate(
            mainActivity,
            mView,
            currentPayPeriod
        )
        payRate = employerPayRate.getPayRate()
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

    override fun getCreditTotalAll(): Double {
        TODO("Not yet implemented")
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
        return payRate * hoursReg
    }

    override fun getPayOt(): Double {
        return round(payRate * 150) / 100 * hoursOt
    }

    override fun getPayDblOt(): Double {
        return round(payRate * 200) / 100 * hoursDblOt
    }

    override fun getPayStat(): Double {
        return payRate * hoursReg
    }

    override fun getPayHourly(): Double {
        return getPayHourly() + getPayStat()
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