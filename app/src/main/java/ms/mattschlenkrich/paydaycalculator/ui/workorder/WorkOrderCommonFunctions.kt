package ms.mattschlenkrich.paydaycalculator.ui.workorder

import ms.mattschlenkrich.paydaycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

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