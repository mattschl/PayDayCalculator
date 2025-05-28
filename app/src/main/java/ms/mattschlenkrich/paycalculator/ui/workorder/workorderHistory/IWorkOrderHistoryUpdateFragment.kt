package ms.mattschlenkrich.paycalculator.ui.workorder.workorderHistory

interface IWorkOrderHistoryUpdateFragment {
    fun setTempWorkOrderHistoryInfo()
    fun gotoMaterialUpdateFragment()
    fun gotoMaterialQuantityUpdateFragment()
    fun gotoWorkOrderHistoryMaterialUpdateFragment()
}