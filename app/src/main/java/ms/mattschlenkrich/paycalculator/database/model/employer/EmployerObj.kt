package ms.mattschlenkrich.paycalculator.database.model.employer

import android.util.Log
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.PayDayFrequencies
import ms.mattschlenkrich.paycalculator.common.WorkDayOfWeek

class EmployerObj {
    private val df = DateFunctions()
    private val nf = NumberFunctions()

    var employerId: Long = 0L
    var employerName: String = "Default Employer"
        set(value) {
            field = value
            name = value
            Log.d("EmployerObj", "employerName $employerName")
        }
    var name: String = "Default Employer"

    var payFrequency: String = PayDayFrequencies.BI_WEEKLY.frequency

    var payFrequencyId: Int = 0
        set(value) {
            field = value
            payFrequency = when (value) {
                PayDayFrequencies.BI_WEEKLY.value -> PayDayFrequencies.BI_WEEKLY.frequency
                PayDayFrequencies.WEEKLY.value -> PayDayFrequencies.WEEKLY.frequency
                else -> PayDayFrequencies.BI_WEEKLY.frequency
            }
        }

    var startDate: String = df.getCurrentDateAsString()

    var dayOfWeek: String = WorkDayOfWeek.FRIDAY.day

    var dayOfWeekId: Int = 0
        set(value) {
            dayOfWeek = when (value) {
                WorkDayOfWeek.FRIDAY.value -> WorkDayOfWeek.FRIDAY.day
                WorkDayOfWeek.MONDAY.value -> WorkDayOfWeek.MONDAY.day
                WorkDayOfWeek.SATURDAY.value -> WorkDayOfWeek.SATURDAY.day
                WorkDayOfWeek.SUNDAY.value -> WorkDayOfWeek.SUNDAY.day
                WorkDayOfWeek.THURSDAY.value -> WorkDayOfWeek.THURSDAY.day
                WorkDayOfWeek.TUESDAY.value -> WorkDayOfWeek.TUESDAY.day
                WorkDayOfWeek.WEDNESDAY.value -> WorkDayOfWeek.WEDNESDAY.day
                WorkDayOfWeek.WEEK_DAY.value -> WorkDayOfWeek.WEEK_DAY.day
                else -> WorkDayOfWeek.ANY_DAY.day
            }
        }

    var cutoffDaysBefore: String = "6"
        set(value) {
            field = value
            cutoffDaysBeforeId = value.toInt()
        }

    var cutoffDaysBeforeId: Int = 6

    var midMonthlyDate: String = "15"
        set(value) {
            field = value
            midMonthlyDateId = value.toInt()
        }
    var midMonthlyDateId: Int = 15

    var mainMonthlyDate: String = "31"
        set(value) {
            field = value
            mainMonthlyDateId = value.toInt()
        }
    var mainMonthlyDateId: Int = 31

    var employerIsDeleted: Boolean = false
    var employerUpdateTime: String = ""

    fun getEmployer(): Employers {
        return Employers(
            if (employerId != 0L) employerId else nf.generateRandomIdAsLong(),
            name,
            payFrequency,
            startDate,
            dayOfWeek,
            cutoffDaysBeforeId,
            midMonthlyDateId,
            mainMonthlyDateId,
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