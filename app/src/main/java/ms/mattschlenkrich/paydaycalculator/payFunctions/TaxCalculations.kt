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
import ms.mattschlenkrich.paydaycalculator.model.TaxTypes
import ms.mattschlenkrich.paydaycalculator.model.WorkTaxRules

private const val TAG = "TaxCalculations"

class TaxCalculations(
    private val mainActivity: MainActivity,
    private val employer: Employers,
    private val cutOff: String,
    private val mView: View,
) {
    private val taxRules = ArrayList<WorkTaxRules>()
    private val taxTypes = ArrayList<TaxTypes>()

    init {
        findTaxRates()
    }

    private fun getTaxFactor(amount: Double): Double {
        when (employer.payFrequency) {
            "Bi-Weekly" -> {
                return amount / 26
            }

            "Weekly" -> {
                return amount / 52
            }

            "Semi-Monthly" -> {
                return amount / 24
            }

            "Monthly" -> {
                return amount / 12
            }

            else -> {
                return 0.0
            }
        }
    }

    fun getAllTaxDeductions(gross: Double, payTimeWorked: Double, payHourly: Double): Double {
        var totalTax = 0.0
        for (taxAndAmount in getTaxeList(gross, payTimeWorked, payHourly)) {
            totalTax += taxAndAmount.amount
        }
        return totalTax
    }

    fun getTaxeList(payGross: Double, payTimeWorked: Double, payHourly: Double):
            ArrayList<TaxAndAmount> {
        val taxesAndAmounts = ArrayList<TaxAndAmount>()
        for (taxType in taxTypes) {
//            Log.d(TAG, "looping through types - $type")
            var taxTotal = 0.0

            var runningRemainder =
                when (taxType.ttBasedOn) {
                    0 -> {
                        payTimeWorked
                    }

                    1 -> {
                        payHourly
                    }

                    2 -> {
                        payGross
                    }

                    else -> {
                        0.0
                    }
                }
            for (def in taxRules) {
//                Log.d(
//                    TAG, "looping through taxDef - ${def.wtType} " +
//                            "and - ${def.wtPercent} "
//                )
                if (def.wtType == taxType.taxType && runningRemainder > 0) {
                    var taxable: Double
                    runningRemainder -=
                        if (def.wtHasExemption) {
                            getTaxFactor(def.wtExemptionAmount)
                        } else {
                            0.0
                        }
                    if (runningRemainder < 0.0) {
                        runningRemainder = 0.0
                    }
                    if (def.wtHasBracket &&
                        runningRemainder >= getTaxFactor(def.wtBracketAmount)
                    ) {
                        taxable = getTaxFactor(def.wtBracketAmount)
                        runningRemainder -= getTaxFactor(def.wtBracketAmount)
                    } else {
                        taxable = runningRemainder
                        runningRemainder = 0.0
                    }
                    taxTotal += taxable * def.wtPercent
                }
            }
            taxesAndAmounts.add(
                TaxAndAmount(
                    taxType.taxType, taxTotal
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
                effectiveDate = date[0]
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
                mainActivity.workTaxViewModel.getTaxDefByDate(
                    effectiveDate
                ).observe(lifecycleOwner) { list ->
                    taxRules.clear()
//                    var counter = 0
                    list.listIterator().forEach {
                        taxRules.add(it)
//                        counter += 1
//                        Log.d(
//                            TAG, "iterating tax def for ${it.wtType} value is " +
//                                    "${it.wtPercent} | counter $counter " +
//                                    "LEVEL is ${it.wtLevel}"
//                        )
                    }
                }
            }
        }
    }
}