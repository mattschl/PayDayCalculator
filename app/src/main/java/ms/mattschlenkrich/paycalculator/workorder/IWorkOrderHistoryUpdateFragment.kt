package ms.mattschlenkrich.paycalculator.workorder

interface IWorkOrderHistoryUpdateFragment {
    fun setTempWorkOrderHistoryInfo()
    fun gotoMaterialUpdateFragment()
    fun gotoMaterialQuantityUpdateFragment()
    fun gotoWorkOrderHistoryMaterialUpdateFragment()
}