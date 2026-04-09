package ms.mattschlenkrich.paycalculator.logic

interface IWorkTimesFragment {
    fun populateValues()
    fun populateUi()
    fun setClickActions()
    fun gotoCallingFragment()
    fun gotoWorkOrderHistoryTimeUpdateFragment()

    fun updateUi()
}