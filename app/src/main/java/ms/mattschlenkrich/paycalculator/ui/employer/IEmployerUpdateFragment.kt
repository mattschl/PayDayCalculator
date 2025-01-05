package ms.mattschlenkrich.paycalculator.ui.employer

interface IEmployerUpdateFragment {
    fun populateTaxes(employerId: Long)
    fun populateExtras(employerId: Long)
}