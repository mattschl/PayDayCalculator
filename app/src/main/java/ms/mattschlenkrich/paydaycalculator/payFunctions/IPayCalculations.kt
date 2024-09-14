package ms.mattschlenkrich.paydaycalculator.payFunctions

import ms.mattschlenkrich.paydaycalculator.database.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.database.model.tax.TaxAndAmount


interface IPayCalculations {
    fun getDaysWorked(): Int
    fun getCreditTotalAll(): Double
    fun getHoursWorked(): Double
    fun getHoursAll(): Double
    fun getHoursReg(): Double
    fun getHoursOt(): Double
    fun getHoursDblOt(): Double
    fun getHoursStat(): Double
    fun getPayRate(): Double
    fun getPayReg(): Double
    fun getPayAllHourly(): Double
    fun getPayOt(): Double
    fun getPayDblOt(): Double
    fun getPayStat(): Double
    fun getPayGross(): Double
    fun getPayTimeWorked(): Double
    fun getCreditExtrasListByDate(): List<ExtraAndTotal>?
    fun getCreditExtrasListByPay(): List<ExtraAndTotal>?
    fun getCreditExtrasListByPercentageOfAll(): List<ExtraAndTotal>?
    fun getDebitExtrasListByPay(): List<ExtraAndTotal>?
    fun getDebitTotalsByPay(): Double
    fun getTaxList(): List<TaxAndAmount>?
    fun getAllTaxDeductions(): Double
}