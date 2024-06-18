package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxAndAmount
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class NewPayCalculations(
    private val mainActivity: MainActivity,
    private val employer: Employers,
    private val mView: View,
    private val currentPayPeriod: PayPeriods,
) : IPayCalculations {
    override fun getDebitExtraAndTotalByPay(): List<ExtraAndTotal> {
        TODO("Not yet implemented")
    }

    override fun getDebitTotalsByPay(): Double {
        TODO("Not yet implemented")
    }

    override fun getCreditExtraAndTotalsByDate(): List<ExtraAndTotal> {
        TODO("Not yet implemented")
    }

    override fun getCreditExtrasAndTotalsByPay(): List<ExtraAndTotal> {
        TODO("Not yet implemented")
    }

    override fun getCreditTotalAll(): Double {
        TODO("Not yet implemented")
    }

    override fun getHoursWorked(): Double {
        TODO("Not yet implemented")
    }

    override fun getHoursAll(): Double {
        TODO("Not yet implemented")
    }

    override fun getHoursReg(): Double {
        TODO("Not yet implemented")
    }

    override fun getHoursOt(): Double {
        TODO("Not yet implemented")
    }

    override fun getHoursDblOt(): Double {
        TODO("Not yet implemented")
    }

    override fun getHoursStat(): Double {
        TODO("Not yet implemented")
    }

    override fun getDaysWorked(): Int {
        TODO("Not yet implemented")
    }

    override fun getPayRate(): Double {
        TODO("Not yet implemented")
    }

    override fun getPayReg(): Double {
        TODO("Not yet implemented")
    }

    override fun getPayOt(): Double {
        TODO("Not yet implemented")
    }

    override fun getPayDblOt(): Double {
        TODO("Not yet implemented")
    }

    override fun getPayHourly(): Double {
        TODO("Not yet implemented")
    }

    override fun getPayStat(): Double {
        TODO("Not yet implemented")
    }

    override fun getPayGross(): Double {
        TODO("Not yet implemented")
    }

    override fun getPayTimeWorked(): Double {
        TODO("Not yet implemented")
    }

    override fun getAllTaxDeductions(): Double {
        TODO("Not yet implemented")
    }

    override fun getTaxList(): List<TaxAndAmount> {
        TODO("Not yet implemented")
    }

}