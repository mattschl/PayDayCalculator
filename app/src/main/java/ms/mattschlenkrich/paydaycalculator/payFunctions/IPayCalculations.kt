package ms.mattschlenkrich.paydaycalculator.payFunctions

import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxAndAmount

interface IPayCalculations {
    fun getDebitExtraAndTotalByPay(): List<ExtraAndTotal>
    fun getDebitTotalsByPay(): Double
    fun getCreditExtraAndTotalsByDate(): List<ExtraAndTotal>
    fun getCreditExtrasAndTotalsByPay(): List<ExtraAndTotal>
    fun getCreditTotalAll(): Double
    fun getHoursWorked(): Double
    fun getHoursAll(): Double
    fun getHoursReg(): Double
    fun getHoursOt(): Double
    fun getHoursDblOt(): Double
    fun getHoursStat(): Double
    fun getDaysWorked(): Int
    fun getPayRate(): Double
    fun getPayReg(): Double
    fun getPayOt(): Double
    fun getPayDblOt(): Double
    fun getPayHourly(): Double
    fun getPayStat(): Double
    fun getPayGross(): Double
    fun getPayTimeWorked(): Double
    fun getAllTaxDeductions(): Double
    fun getTaxList(): List<TaxAndAmount>
}