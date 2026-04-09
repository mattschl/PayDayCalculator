package ms.mattschlenkrich.paycalculator.data


data class PayPeriodHourlySummary(
    var daysWorked: Int,
    var hoursReg: Double,
    var hoursOt: Double,
    var hoursDblOt: Double,
    var hoursStat: Double,
    var payRate: Double,
)