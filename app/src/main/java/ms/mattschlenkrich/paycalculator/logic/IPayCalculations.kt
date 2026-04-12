package ms.mattschlenkrich.paycalculator.logic

import ms.mattschlenkrich.paycalculator.data.ExtraContainer
import ms.mattschlenkrich.paycalculator.data.TaxAndAmount


interface IPayCalculations {
    suspend fun waitForCalculations() {}
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
    fun getDebitTotalsByPay(): Double
    fun getTaxList(): List<TaxAndAmount>?
    fun getAllTaxDeductions(): Double
    fun getCredits(): List<ExtraContainer>
    fun getDebits(): List<ExtraContainer>
}