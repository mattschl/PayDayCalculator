package ms.mattschlenkrich.paycalculator.employer

interface IEmployerUpdateFragment {
    fun populateTaxes(employerId: Long)
    fun populateExtras(employerId: Long)
    fun gotoEmployerExtraDefinitionUpdateFragment()
}