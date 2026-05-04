package ms.mattschlenkrich.paycalculator.ui.paydetail

import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.model.ExtraContainer
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.entity.WorkPayPeriodExtras

fun insertOrUpdateExtraOnChange(
    extraContainer: ExtraContainer,
    delete: Boolean,
    payPeriodId: Long,
    payDayViewModel: PayDayViewModel,
    nf: NumberFunctions,
    df: DateFunctions
) {
    if (extraContainer.payPeriodExtra != null) {
        val payPeriodExtra = extraContainer.payPeriodExtra!!
        val newExtra = WorkPayPeriodExtras(
            payPeriodExtra.workPayPeriodExtraId,
            payPeriodExtra.ppePayPeriodId,
            payPeriodExtra.ppeExtraTypeId,
            payPeriodExtra.ppeName,
            payPeriodExtra.ppeAppliesTo,
            3,
            payPeriodExtra.ppeValue,
            payPeriodExtra.ppeIsFixed,
            payPeriodExtra.ppeIsCredit,
            delete,
            df.getCurrentUTCTimeAsString()
        )
        extraContainer.payPeriodExtra = newExtra
        payDayViewModel.updatePayPeriodExtra(newExtra)
    } else if (extraContainer.extraDefinitionAndType != null) {
        val extraDefinitionAndType = extraContainer.extraDefinitionAndType!!
        val newExtra = WorkPayPeriodExtras(
            nf.generateRandomIdAsLong(),
            payPeriodId,
            extraDefinitionAndType.extraType.workExtraTypeId,
            extraDefinitionAndType.extraType.wetName,
            extraDefinitionAndType.extraType.wetAppliesTo,
            extraDefinitionAndType.extraType.wetAttachTo,
            extraDefinitionAndType.definition.weValue,
            extraDefinitionAndType.definition.weIsFixed,
            extraDefinitionAndType.extraType.wetIsCredit,
            delete,
            df.getCurrentUTCTimeAsString()
        )
        extraContainer.payPeriodExtra = newExtra
        payDayViewModel.insertPayPeriodExtra(newExtra)
    }
}