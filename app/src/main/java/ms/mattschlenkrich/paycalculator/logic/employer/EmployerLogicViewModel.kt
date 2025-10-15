package ms.mattschlenkrich.paycalculator.logic.employer

import androidx.lifecycle.ViewModel
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerObj
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.viewModel.EmployerViewModel

class EmployerLogicViewModel(val employerViewModel: EmployerViewModel) : ViewModel() {
    var previousEmployerObj: EmployerObj = EmployerObj()
    var currentEmployerObj: EmployerObj = EmployerObj()
    private lateinit var employerList: List<Employers>

    init {
        employerViewModel.getEmployers().observeForever { list ->
            employerList = list
        }
    }

    fun getEmployerList(): List<Employers> {
        return employerList
    }


}