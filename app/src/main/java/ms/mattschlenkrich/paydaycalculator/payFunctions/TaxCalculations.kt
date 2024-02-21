package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.TaxAndAmount
import ms.mattschlenkrich.paydaycalculator.model.TaxComplete

class TaxCalculations(
    private val mainActivity: MainActivity,
    private val employer: Employers,
    private val cutOff: String,
    private val mView: View,
) {
    private val workTaxAndDef = ArrayList<TaxComplete>()
    private val taxTypes = ArrayList<String>()

    init {
        findTaxRates()
    }

    fun getAllTaxDeductions(gross: Double): Double {
        var totalTax = 0.0
        for (taxAndAmount in getTaxes(gross)) {
            totalTax += taxAndAmount.amount
        }
        return totalTax
    }

    fun getTaxes(gross: Double): ArrayList<TaxAndAmount> {
        val taxesAndAmounts = ArrayList<TaxAndAmount>()
        for (type in taxTypes) {
            var taxTotal = 0.0
            var runningRemainder = gross
            for (taxDef in workTaxAndDef) {
                if (taxDef.taxType.taxType == type && runningRemainder > 0) {
                    var taxable = 0.0
                    runningRemainder -=
                        if (taxDef.taxRule.wtHasExemption) taxDef.taxRule.wtExemptionAmount
                        else 0.0
                    if (taxDef.taxRule.wtHasBracket &&
                        runningRemainder >= taxDef.taxRule.wtBracketAmount
                    ) {
                        taxable = taxDef.taxRule.wtBracketAmount
                        runningRemainder -= taxDef.taxRule.wtBracketAmount
                    } else {
                        taxable = runningRemainder
                        runningRemainder = 0.0
                    }
                    taxTotal += taxable * taxDef.taxRule.wtPercent
                }
            }
            taxesAndAmounts.add(
                TaxAndAmount(
                    type, taxTotal
                )
            )
        }
        return taxesAndAmounts
    }

    private fun findTaxRates() {
        var effectiveDate = ""
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workTaxViewModel.getCurrentEffectiveDate(
                cutOff
            ).observe(lifecycleOwner) { date ->
                effectiveDate = date
            }
        }
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workTaxViewModel.getTaxTypesByEmployer(
                employer.employerId
            ).observe(lifecycleOwner) { types ->
                taxTypes.clear()
                types.listIterator().forEach {
                    taxTypes.add(it)
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(WAIT_250)
            mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
                mainActivity.workTaxViewModel.getTaxTypeAndDef(
                    effectiveDate
                ).observe(lifecycleOwner) { list ->
                    workTaxAndDef.clear()
                    list.listIterator().forEach {
                        workTaxAndDef.add(it)
                    }
                }
            }
        }
    }
}