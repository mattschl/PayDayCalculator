package ms.mattschlenkrich.paycalculator.ui.workorder

import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.WorkDates
import ms.mattschlenkrich.paycalculator.data.WorkOrderHistory

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