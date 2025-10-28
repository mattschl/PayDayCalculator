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
    var employerName: String = ""
    var payFrequency: String = ""
        set(value) {
            field = value
            payFrequencyId = when (value) {
                PayDayFrequencies.BI_WEEKLY.frequency -> PayDayFrequencies.BI_WEEKLY.value
                PayDayFrequencies.WEEKLY.frequency -> PayDayFrequencies.WEEKLY.value
                else -> PayDayFrequencies.BI_WEEKLY.value
            }
            Log.d("EmployerObj", "payFrequency $payFrequency")
        }
    var payFrequencyId: Int = 0

    var startDate: String = ""
    var dayOfWeek: String = ""
        set(value) {
            field = value
            dayOfWeekId = when (value) {
                WorkDayOfWeek.FRIDAY.day -> WorkDayOfWeek.FRIDAY.value
                WorkDayOfWeek.MONDAY.day -> WorkDayOfWeek.MONDAY.value
                WorkDayOfWeek.SATURDAY.day -> WorkDayOfWeek.SATURDAY.value
                WorkDayOfWeek.SUNDAY.day -> WorkDayOfWeek.SUNDAY.value
                WorkDayOfWeek.THURSDAY.day -> WorkDayOfWeek.THURSDAY.value
                WorkDayOfWeek.TUESDAY.day -> WorkDayOfWeek.TUESDAY.value
                WorkDayOfWeek.WEDNESDAY.day -> WorkDayOfWeek.WEDNESDAY.value
                WorkDayOfWeek.WEEK_DAY.day -> WorkDayOfWeek.WEEK_DAY.value
                else -> WorkDayOfWeek.ANY_DAY.value
            }
        }
    var dayOfWeekId: Int = 0

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
            employerName,
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