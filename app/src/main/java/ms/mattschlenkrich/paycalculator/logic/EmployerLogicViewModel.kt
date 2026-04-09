package ms.mattschlenkrich.paycalculator.logic

import android.app.Application
import androidx.lifecycle.ViewModel
import ms.mattschlenkrich.paycalculator.data.EmployerObj
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.Employers

class EmployerLogicViewModel(
    val app: Application,
    val employerViewModel: EmployerViewModel,
) : ViewModel() {
    var previousEmployerObj: EmployerObj = EmployerObj()
    var currentEmployerObj: EmployerObj = EmployerObj()
    private lateinit var employerList: List<Employers>

    init {
        employerViewModel.getEmployers().observeForever { list ->
            employerList = list
        }
    }

//    fun validateEmployer(): String {
//        if (currentEmployerObj.employerName.isEmpty()) {
//            return app.getString(R.string.the_employer_must_have_a_name)
//        }
//        if (currentEmployerObj.employerName != previousEmployerObj.employerName) {
//            for (employer in employerList) {
//                if (employer.employerName == currentEmployerObj.employerName) {
//                    return app.getString(R.string.this_employer_already_exists)
//                }
//            }
//        }
//        if (currentEmployerObj.cutoffDaysBefore.isBlank()) {
//            return app.getString(R.string.the_number_of_days_before_the_pay_day_is_required)
//        }
//        if (currentEmployerObj.midMonthlyDate.isBlank()) {
//            return app.getString(R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day)
//        }
//        return ANSWER_OK
//    }

    fun getEmployerList(): List<Employers> {
        return employerList
    }

    fun addEmployer() {
        employerViewModel.insertEmployer(currentEmployerObj.getEmployer())
    }

    fun updateEmployer() {
        employerViewModel.updateEmployer(currentEmployerObj.getEmployer())
    }

}