package ms.mattschlenkrich.paydaycalculator.newPayCalculations

import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraDefinitionAndType

class ExtraDebitCalculations(
    private val workExtrasByPay: ArrayList<ExtraDefinitionAndType>,
    private val hourlyPayCalculations: HourlyPayCalculations
) {

    private lateinit var debitList: List<ExtraAndTotal>
    private var debitTotal = 0.0

    init {
        calculateDebitExtraAndTotalByPay()
        calculateDebitTotalByPay()
    }

    private fun calculateDebitTotalByPay() {
        debitTotal = findDebitTotalsByPay()
    }

    private fun calculateDebitExtraAndTotalByPay() {
        debitList = findDebitExtraAndTotalByPay()
    }

    fun getDebitList(): List<ExtraAndTotal> {
        return debitList
    }

    fun getDebitTotalsByPay(): Double {
        return debitTotal
    }

    fun findDebitTotalsByPay(): Double {
        var total = 0.0
        for (extra in debitList) {
//                Log.d(TAG, "extra is ${extra.extraName} and amount is ${extra.amount}")
            total += extra.amount
        }
        return total
    }

    private fun findDebitExtraAndTotalByPay(): ArrayList<ExtraAndTotal> {
        val debitList = ArrayList<ExtraAndTotal>()
        for (i in 0 until workExtrasByPay.size) {
            if (!workExtrasByPay[i].extraType.wetIsCredit &&
                workExtrasByPay[i].extraType.wetIsDefault &&
                workExtrasByPay[i].extraType.wetAppliesTo == 3
            ) {
                if (workExtrasByPay[i].definition.weIsFixed
                ) {
                    debitList.add(
                        ExtraAndTotal(
                            workExtrasByPay[i].extraType.wetName,
                            workExtrasByPay[i].definition.weValue
                        )
                    )
                } else {
                    debitList.add(
                        ExtraAndTotal(
                            workExtrasByPay[i].extraType.wetName,
                            workExtrasByPay[i].definition.weValue *
                                    hourlyPayCalculations.getPayTimeWorked() / 100
                        )
                    )
                }
            } else if (!workExtrasByPay[i].extraType.wetIsCredit &&
                workExtrasByPay[i].extraType.wetIsDefault &&
                workExtrasByPay[i].extraType.wetAppliesTo == 0 &&
                !workExtrasByPay[i].definition.weIsFixed
            ) {
                debitList.add(
                    ExtraAndTotal(
                        workExtrasByPay[i].extraType.wetName,
                        workExtrasByPay[i].definition.weValue *
                                hourlyPayCalculations.getPayTimeWorked() / 100
                    )
                )
            }
        }
        return debitList
    }
}