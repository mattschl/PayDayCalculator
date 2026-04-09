package ms.mattschlenkrich.paycalculator.workdate

import ms.mattschlenkrich.paycalculator.data.WorkExtraTypes

interface IWorkDateAddFragment {
    fun addToExtraList(include: Boolean, extraType: WorkExtraTypes)

    //    fun saveWorkDate(goBackTo: String)
    fun validateWorkDateToSave(fragment: String, isAutomaticallySaved: Boolean)
}