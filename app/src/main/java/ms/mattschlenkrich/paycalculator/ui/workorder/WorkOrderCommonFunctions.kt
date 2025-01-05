package ms.mattschlenkrich.paycalculator.ui.workorder

import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class WorkOrderCommonFunctions(val mainActivity: MainActivity) {

    fun getWorkDateObject(): WorkDates? {
        return mainActivity.mainViewModel.getWorkDateObject()
    }

    fun getEmployer(): Employers? {
        return mainActivity.mainViewModel.getEmployer()
    }

    fun getWorkOrderHistory(): WorkOrderHistory? {
        return mainActivity.mainViewModel.getWorkOrderHistory()
    }
}