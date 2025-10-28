package ms.mattschlenkrich.paycalculator.Objects

import ms.mattschlenkrich.paycalculator.common.ANSWER_OK
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.payfunctions.PayDateProjections
import ms.mattschlenkrich.paycalculator.ui.MainActivity


class EmployerObject(
    var employerId: Long = 0,
    var employerName: String = "",
    var payFrequency: String = "",
    var startDate: String = "",
    var dayOfWeek: String = "",
    var cutoffDaysBefore: Int = 0,
    var midMonthlyDate: Int = 15,
    var mainMonthlyDate: Int = 31,
    var employerIsDeleted: Boolean = false,
    var employerUpdateTime: String = "",
    val mainActivity: MainActivity
) {

    private val df: DateFunctions = DateFunctions()
    private val payDateProjections: PayDateProjections = PayDateProjections()
    private val employerViewModel = mainActivity.employerViewModel


    fun setEmployerId(employerId: Long) {
        this.employerId = employerId
    }

    fun getEmployerId(): Long {
        return employerId
    }

    fun setName(employerName: String) {
        this.employerName = employerName
    }

    fun getName(): String {
        return employerName
    }

    fun setPayFrequency(payFrequency: String) {
        this.payFrequency = payFrequency
    }

    fun getPayFrequency(): String {
        return payFrequency
    }

    fun setPayStartDate(startDate: String) {
        this.startDate = startDate
    }

    fun getPayStartDate(): String {
        return startDate
    }

    fun setPayDayOfWeek(dayOfWeek: String) {
        this.dayOfWeek = dayOfWeek
    }

    fun getPayDayOfWeek(): String {
        return dayOfWeek
    }

    fun setCutoffDaysBefore(cutoffDaysBefore: Int) {
        this.cutoffDaysBefore = cutoffDaysBefore
    }

    fun getCutoffDaysBefore(): Int {
        return cutoffDaysBefore
    }

    fun setMidMonthlyDate(midMonthlyDate: Int) {
        this.midMonthlyDate = midMonthlyDate
    }

    fun getMidMonthlyDate(): Int {
        return midMonthlyDate
    }

    fun setMainMonthlyDate(mainMonthlyDate: Int) {
        this.mainMonthlyDate = mainMonthlyDate
    }

    fun getMainMonthlyDate(): Int {
        return mainMonthlyDate
    }

    fun setIsDeleted(employerIsDeleted: Boolean) {
        this.employerIsDeleted = employerIsDeleted
    }

    fun getIsDeleted(): Boolean {
        return employerIsDeleted
    }

    fun setUpdateTime(employerUpdateTime: String) {
        this.employerUpdateTime = employerUpdateTime
    }

    fun getUpdateTime(): String {
        return employerUpdateTime
    }

    fun getCurrentEmployer(): Employers {
        return Employers(
            employerId,
            employerName,
            payFrequency,
            startDate,
            dayOfWeek,
            cutoffDaysBefore,
            midMonthlyDate,
            mainMonthlyDate,
            employerIsDeleted,
            employerUpdateTime
        )
    }

    fun getNextPayDate(lastPayDate: String): String {
        return payDateProjections.generateNextCutOff(
            getCurrentEmployer(),
            if (lastPayDate == "") df.getCurrentDateAsString() else lastPayDate
        )
    }

    fun addEmployer() {
        employerViewModel.insertEmployer(getCurrentEmployer())
    }

    fun addEmployer(employer: Employers) = employerViewModel.insertEmployer(employer)


    fun updateEmployer() {
        employerViewModel.updateEmployer(getCurrentEmployer())
    }

    fun removeEmployer(employers: Employers) =
        employerViewModel.deleteEmployer(employerId, df.getCurrentTimeAsString())

    fun getAllEmployers() = employerViewModel.getEmployers()

    fun validateEmployer(): String {
        if (employerName == "") {
            return "Please enter an employer name"
        } else if (payFrequency == "") {
            return "Please select a pay frequency"
        } else if (startDate == "") {
            return "Please select a start date"
        } else if (dayOfWeek == "") {
            return "Please select a day of the week"
        } else if (cutoffDaysBefore == 0) {
            return "Please enter a cutoff days before"
        }
        return ANSWER_OK

    }

}
