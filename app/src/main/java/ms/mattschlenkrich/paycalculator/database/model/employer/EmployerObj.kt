package ms.mattschlenkrich.paycalculator.database.model.employer

import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.PayDayFrequencies
import ms.mattschlenkrich.paycalculator.common.WorkDayOfWeek

class EmployerObj {
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    var employerId: Long = 0L
    var employerName: String = "Default Employer"
    var payFrequency: String = PayDayFrequencies.BI_WEEKLY.frequency
    var startDate: String = df.getCurrentDateAsString()
    var dayOfWeek: String = WorkDayOfWeek.FRIDAY.day
    var cutoffDaysBefore: String = "6"
    var midMonthlyDate: String = "15"
    var mainMonthlyDate: String = "31"
    var employerIsDeleted: Boolean = false
    var employerUpdateTime: String = ""

    fun getEmployer(): Employers {
        return Employers(
            if (employerId != 0L) employerId else nf.generateRandomIdAsLong(),
            employerName,
            payFrequency,
            startDate,
            dayOfWeek,
            cutoffDaysBefore.toInt(),
            midMonthlyDate.toInt(),
            mainMonthlyDate.toInt(),
            employerIsDeleted,
            df.getCurrentTimeAsString()
        )
    }

    fun setEmployer(employer: Employers) {
        employerId = employer.employerId
        employerName = employer.employerName
        payFrequency = employer.payFrequency
        startDate = employer.startDate
        dayOfWeek = employer.dayOfWeek
        cutoffDaysBefore = employer.cutoffDaysBefore.toString()
        midMonthlyDate = employer.midMonthlyDate.toString()
        mainMonthlyDate = employer.mainMonthlyDate.toString()
        employerIsDeleted = employer.employerIsDeleted
        employerUpdateTime = employer.employerUpdateTime
    }
}