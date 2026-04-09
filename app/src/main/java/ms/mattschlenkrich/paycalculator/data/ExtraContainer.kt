package ms.mattschlenkrich.paycalculator.data

data class ExtraContainer(
    var extraName: String,
    var amount: Double,
    var extraDefinitionAndType: ExtraDefinitionAndType?,
    var workDateExtra: WorkDateExtras?,
    var payPeriodExtra: WorkPayPeriodExtras?,
)