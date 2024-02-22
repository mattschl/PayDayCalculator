package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.util.Log
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

private const val TAG = "TaxCalculations"

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
        for (i in 0 until taxTypes.size) {
//            Log.d(TAG, "looping through types - $type")
            var taxTotal = 0.0
            var runningRemainder = gross
            for (t in 0 until workTaxAndDef.size) {
//                Log.d(
//                    TAG, "looping through taxDef - ${workTaxAndDef[t].taxType.taxType} " +
//                            "and - ${workTaxAndDef[t].taxRule.wtPercent} ==" +
//                            "${workTaxAndDef.size} entries"
//                )
                if (workTaxAndDef[t].taxType.taxType == taxTypes[i] && runningRemainder > 0) {
                    var taxable: Double
                    runningRemainder -=
                        if (workTaxAndDef[t].taxRule.wtHasExemption)
                            workTaxAndDef[t].taxRule.wtExemptionAmount
                        else 0.0
                    if (workTaxAndDef[t].taxRule.wtHasBracket &&
                        runningRemainder >= workTaxAndDef[t].taxRule.wtBracketAmount
                    ) {
                        taxable = workTaxAndDef[t].taxRule.wtBracketAmount
                        runningRemainder -= workTaxAndDef[t].taxRule.wtBracketAmount
                    } else {
                        taxable = runningRemainder
                        runningRemainder = 0.0
                    }
                    taxTotal += taxable * workTaxAndDef[t].taxRule.wtPercent
                }
            }
            taxesAndAmounts.add(
                TaxAndAmount(
                    taxTypes[i], taxTotal
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
                effectiveDate = date[0].toString()
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
                    var counter = 0
                    list.listIterator().forEach {
                        workTaxAndDef.add(it)
                        counter += 1
                        Log.d(
                            TAG, "iterating tax def for ${it.taxType.taxType} value is " +
                                    "${it.taxRule.wtPercent} | counter $counter " +
                                    "LEVEL is ${it.taxRule.wtLevel}"
                        )
                    }
                }
            }
        }
    }
}