package ms.mattschlenkrich.paycalculator.database.model.extras

import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkPayPeriodExtras

data class ExtraContainer(
    var extraName: String,
    var value: Double,
    var extraDefinitionAndType: ExtraDefinitionAndType?,
    var workDateExtra: WorkDateExtras?,
    var payPeriodExtra: WorkPayPeriodExtras?,
)
