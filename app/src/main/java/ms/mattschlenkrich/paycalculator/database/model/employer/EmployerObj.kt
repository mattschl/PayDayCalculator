package ms.mattschlenkrich.paycalculator.database.model.employer

import ms.mattschlenkrich.paycalculator.common.PayDayFrequencies
import ms.mattschlenkrich.paycalculator.common.WorkDayOfWeek

class EmployerObj {
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
        }
    var payFrequencyId: Int = 0
        set(value) {
            field = value
            payFrequency = when (value) {
                PayDayFrequencies.BI_WEEKLY.value -> PayDayFrequencies.BI_WEEKLY.frequency
                PayDayFrequencies.WEEKLY.value -> PayDayFrequencies.WEEKLY.frequency
                else -> PayDayFrequencies.BI_WEEKLY.frequency
            }
        }
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
        set(value) {
            field = value
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
    var cutoffDaysBefore: Int = 6
    var midMonthlyDate: Int = 15
    var mainMonthlyDate: Int = 31
    var employerIsDeleted: Boolean = false
    var employerUpdateTime: String = ""
}