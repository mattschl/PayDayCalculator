package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import ms.mattschlenkrich.paydaycalculator.database.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.database.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.PayPeriodHourlySummary
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class PercentCreditCalculations(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val currentPayPeriod: PayPeriods,
    private val hourlySummary: PayPeriodHourlySummary,
    private val creditCalculations: CreditCalculations
) {
    fun getExtraList(): List<ExtraAndTotal> {
        val extraList = ArrayList<ExtraAndTotal>()
        for (extra in getExtrasByPercent()) {
            val total =
                extra.definition.weValue *
                        (creditCalculations.getCreditTotal() +
                                hourlySummary.payRate *
                                (hourlySummary.hoursReg +
                                        hourlySummary.hoursOt +
                                        hourlySummary.hoursDblOt +
                                        hourlySummary.hoursStat)) / 100
            extraList.add(
                ExtraAndTotal(
                    extra.extraType.wetName,
                    total
                )
            )
        }

        return extraList
    }


    private fun getExtrasByPercent(): List<ExtraDefinitionAndType> {
        val extraList = ArrayList<ExtraDefinitionAndType>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workExtraViewModel.getExtraTypesAndDef(
                currentPayPeriod.ppEmployerId,
                currentPayPeriod.ppCutoffDate,
                4
            ).observe(lifecycleOwner) { list ->
                extraList.clear()
                list.listIterator().forEach {
                    extraList.add(it)
                }
            }
        }
        return extraList
    }
}