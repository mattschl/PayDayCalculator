package ms.mattschlenkrich.paydaycalculator.payFunctions

class HourlyPayCalculations(
    private val hourlyCalculations: HourlyCalculations,
    private val payRate: Double
) {

    fun getPayReg(): Double {
        return hourlyCalculations.getHoursReg() * payRate
    }

    fun getPayOt(): Double {
        return hourlyCalculations.getHoursOt() * payRate * 1.5
    }

    fun getPayDblOt(): Double {
        return hourlyCalculations.getHoursDblOt() * payRate * 2
    }

    fun getPayHourly(): Double {
        return getPayTimeWorked() + getPayStat()
    }

    fun getPayTimeWorked(): Double {
        return getPayReg() + getPayOt() + getPayDblOt()
    }

    fun getPayStat(): Double {
        return hourlyCalculations.getHoursStat() * payRate
    }
}