package ms.mattschlenkrich.paycalculator.data.model

import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkPayPeriodExtras

data class ExtraContainer(
    var extraName: String,
    var amount: Double,
    var extraDefinitionAndType: ExtraDefinitionAndType? = null,
    var workDateExtra: WorkDateExtras? = null,
    var payPeriodExtra: WorkPayPeriodExtras? = null,
)