package ms.mattschlenkrich.paycalculator.ui.paydays

import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtraTypes

interface IWorkDateAddFragment {
    fun addToExtraList(include: Boolean, extraType: WorkExtraTypes)

    //    fun saveWorkDate(goBackTo: String)
    fun validateWorkDateToSave(fragment: String)
}