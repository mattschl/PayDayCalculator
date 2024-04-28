package ms.mattschlenkrich.paydaycalculator.payFunctions

import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates

class HourlyCalculations(
    private val workDates: List<WorkDates>
) {
    private var hoursReg = 0.0
    private var hoursOt = 0.0
    private var hoursDblOt = 0.0
    private var hoursStat = 0.0
    private var daysWorked = 0

    init {
        hoursReg = findHoursReg()
        hoursOt = findHoursOt()
        hoursDblOt = findHoursDblOt()
        hoursStat = findHoursStat()
        daysWorked = findDaysWorked()
    }

    fun getHoursReg(): Double {
        return hoursReg
    }

    fun getHoursOt(): Double {
        return hoursOt
    }

    fun getHoursDblOt(): Double {
        return hoursDblOt
    }

    fun getHoursStat(): Double {
        return hoursStat
    }

    fun getDaysWorked(): Int {
        return daysWorked
    }

    fun getHoursWorked(): Double {
        return getHoursReg() + getHoursOt() + getHoursDblOt()
    }

    fun getHoursAll(): Double {
        return getHoursWorked() + getHoursStat()
    }

    fun findHoursReg(): Double {
        var hours = 0.0
        for (day in workDates) {
            if (!day.wdIsDeleted) hours += day.wdRegHours
        }
        return hours
    }

    fun findHoursOt(): Double {
        var hours = 0.0
        for (day in workDates) {
            if (!day.wdIsDeleted) hours += day.wdOtHours
        }
        return hours
    }

    fun findHoursDblOt(): Double {
        var hours = 0.0
        for (day in workDates) {
            if (!day.wdIsDeleted) hours += day.wdDblOtHours
        }
        return hours
    }

    fun findHoursStat(): Double {
        var hours = 0.0
        for (day in workDates) {
            if (!day.wdIsDeleted) hours += day.wdStatHours
        }
        return hours
    }

    fun findDaysWorked(): Int {
        var days = 0
        for (day in workDates) {
            days++
        }
        return days
    }
}