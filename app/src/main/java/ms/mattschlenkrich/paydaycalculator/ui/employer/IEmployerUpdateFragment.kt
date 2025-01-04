package ms.mattschlenkrich.paydaycalculator.ui.employer

interface IEmployerUpdateFragment {
    fun populateTaxes(employerId: Long)
    fun populateExtras(employerId: Long)
}