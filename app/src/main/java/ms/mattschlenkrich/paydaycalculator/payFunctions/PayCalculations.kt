package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import ms.mattschlenkrich.paydaycalculator.database.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.database.model.tax.TaxAndAmount
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class PayCalculations(
    mainActivity: MainActivity,
    mView: View,
    currentPayPeriod: PayPeriods,
) : IPayCalculations {

    init {
        val workDateCalculations = WorkDateCalculations(
            mainActivity, currentPayPeriod.ppEmployerId, mView, currentPayPeriod
        )
    }

    override fun getDebitExtrasListByPay(): List<ExtraAndTotal>? {
        TODO("Not yet implemented")
    }

    override fun getDebitTotalsByPay(): Double {
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

    override fun getTaxList(): List<TaxAndAmount>? {
        TODO("Not yet implemented")
    }

    override fun getCreditExtrasListByPercentageOfAll(): List<ExtraAndTotal>? {
        TODO("Not yet implemented")
    }
}