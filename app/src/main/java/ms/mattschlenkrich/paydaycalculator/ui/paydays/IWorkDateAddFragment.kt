package ms.mattschlenkrich.paydaycalculator.ui.paydays

import ms.mattschlenkrich.paydaycalculator.database.model.extras.WorkExtraTypes

interface IWorkDateAddFragment {
    fun addToExtraList(include: Boolean, extraType: WorkExtraTypes)
    fun saveWorkDate(goBackTo: String)
}