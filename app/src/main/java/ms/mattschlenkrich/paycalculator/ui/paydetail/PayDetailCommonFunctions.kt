package ms.mattschlenkrich.paycalculator.ui.paydetail

import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.data.model.ExtraContainer
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDayViewModel

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
        val extraAndType = extraContainer.extraDefinitionAndType!!
        val type = extraAndType.extraType
        val def = extraAndType.definition
        val newExtra = WorkPayPeriodExtras(
            nf.generateRandomIdAsLong(),
            payPeriodId,
            type.workExtraTypeId,
            type.wetName,
            type.wetAppliesTo,
            type.wetAttachTo,
            def.weValue,
            def.weIsFixed,
            type.wetIsCredit,
            delete,
            df.getCurrentUTCTimeAsString()
        )
        extraContainer.payPeriodExtra = newExtra
        payDayViewModel.insertPayPeriodExtra(newExtra)
    }
}