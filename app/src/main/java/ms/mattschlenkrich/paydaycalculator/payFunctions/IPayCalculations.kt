package ms.mattschlenkrich.paydaycalculator.payFunctions

import ms.mattschlenkrich.paydaycalculator.database.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.database.model.tax.TaxAndAmount


interface IPayCalculations {
    fun getDebitExtrasListByPay(): List<ExtraAndTotal>?
    fun getDebitTotalsByPay(): Double
    fun getCreditExtrasListByDate(): List<ExtraAndTotal>?
    fun getCreditExtrasListByPay(): List<ExtraAndTotal>?
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
    fun getTaxList(): List<TaxAndAmount>?
}