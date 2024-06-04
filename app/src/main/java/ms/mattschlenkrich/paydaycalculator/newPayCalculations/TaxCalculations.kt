package ms.mattschlenkrich.paydaycalculator.newPayCalculations

import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxAndAmount
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxTypes
import ms.mattschlenkrich.paydaycalculator.model.tax.WorkTaxRules

class TaxCalculations(
    private val employer: Employers,
    private val taxRules: List<WorkTaxRules>,
    private val taxTypes: List<TaxTypes>,
    private val hourlyPayCalculations: HourlyPayCalculations,
    private val extraCreditCalculations: ExtraCreditCalculations

) {
    private lateinit var taxList: List<TaxAndAmount>
    private var totalTax = 0.0

    init {
        calculateTaxList()
        calculateTotalTax()
    }

    private fun calculateTotalTax() {
        totalTax = findAllTaxDeductions()
    }

    private fun calculateTaxList() {
        taxList = findTaxList()
    }

    fun getTaxList(): List<TaxAndAmount> {
        return taxList
    }

    fun getAllTaxDeductions(): Double {
        return totalTax
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

    private fun findAllTaxDeductions(): Double {
        var totalTax = 0.0
        for (taxAndAmount in taxList) {
            totalTax += taxAndAmount.amount
        }
        return if (hourlyPayCalculations.getPayHourly() > 0.0) {
            totalTax
        } else {
            0.0
        }
    }

    private fun findTaxList():
            ArrayList<TaxAndAmount> {
        val taxesAndAmounts = ArrayList<TaxAndAmount>()
        for (taxType in taxTypes) {
//            Log.d(TAG, "looping through types - $type")
            var taxTotal = 0.0

            var runningRemainder =
                when (taxType.ttBasedOn) {
                    0 -> {
                        hourlyPayCalculations.getPayTimeWorked()
                    }

                    1 -> {
                        hourlyPayCalculations.getPayHourly()
                    }

                    2 -> {
                        hourlyPayCalculations.getPayHourly() +
                                extraCreditCalculations.getCreditTotal()
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
}