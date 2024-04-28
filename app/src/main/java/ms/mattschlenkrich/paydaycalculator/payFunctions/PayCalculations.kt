package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxAndAmount
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxTypes
import ms.mattschlenkrich.paydaycalculator.model.tax.WorkTaxRules
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

//private const val TAG = "PayCalculations"

class PayCalculations(
    private val mainActivity: MainActivity,
    private val employer: Employers,
    private val cutOff: String,
    private val mView: View,
) {
    private val workDates = ArrayList<WorkDates>()
    private lateinit var extraTypes: ArrayList<WorkExtraTypes>

    private val taxRules = ArrayList<WorkTaxRules>()
    private val taxTypes = ArrayList<TaxTypes>()
    private lateinit var hourlyCalculations: HourlyCalculations
    private lateinit var extraCalculations: ExtraCalculations
    private lateinit var extraCreditCalculations: ExtraCreditCalculations
    private lateinit var extraDebitCalculations: ExtraDebitCalculations
    private lateinit var hourlyPayCalculations: HourlyPayCalculations
    var payRate = 0.0
    val hours = Hours()
    val pay = Pay()
    val credits = Credits()
    val tax = TAX()
    val deductions = Deductions()

    init {
        findWorkDates()
        findRate()
        findTaxRates()
        performHourlyCalculations()
        performHourlyPayCalculations()
        performExtraCalculations()
        performCreditCalculations()
        performDebitCalculations()
    }

    private fun performHourlyPayCalculations() {
        hourlyPayCalculations = HourlyPayCalculations(
            hourlyCalculations,
            payRate
        )
    }

    private fun performDebitCalculations() {
        extraDebitCalculations = ExtraDebitCalculations(
            extraCalculations.getWorkExtrasByPay(),
            hourlyPayCalculations
        )
    }

    private fun performCreditCalculations() {
        extraCreditCalculations = ExtraCreditCalculations(
            hourlyCalculations,
            payRate,
            workDates,
            extraCalculations.getWorkDateExtrasFull(),
            extraCalculations.getWorkExtrasByPay()
        )
    }

    private fun performExtraCalculations() {
        extraCalculations = ExtraCalculations(
            mView, mainActivity, employer, cutOff
        )
        extraTypes = extraCalculations.getExtraTypes()
    }

    private fun performHourlyCalculations() {
        hourlyCalculations = HourlyCalculations(workDates)
    }

    inner class Deductions {
        fun getDebitExtraAndTotalByPay() = extraDebitCalculations.getDebitList()
    }

    inner class Credits {
        fun getCreditExtraAndTotalsByDate() =
            extraCreditCalculations.getCreditExtraAndTotalsByDate()

        fun getCreditExtrasAndTotalsByPay() =
            extraCreditCalculations.getCreditExtrasAndTotalsByPay()
    }

    inner class Hours {
        fun getHoursWorked() = hourlyCalculations.getHoursWorked()
        fun getHoursAll() = hourlyCalculations.getHoursAll()
        fun getHoursReg() = hourlyCalculations.getHoursReg()
        fun getHoursOt() = hourlyCalculations.getHoursOt()
        fun getHoursDblOt() = hourlyCalculations.getHoursDblOt()
        fun getHoursStat() = hourlyCalculations.getHoursStat()
        fun getDaysWorked() = hourlyCalculations.getDaysWorked()
    }

    inner class Pay {
        fun getPayReg() = hourlyPayCalculations.getPayReg()
        fun getPayOt() = hourlyPayCalculations.getPayOt()
        fun getPayDblOt() = hourlyPayCalculations.getPayDblOt()
        fun getPayHourly() = hourlyPayCalculations.getPayHourly()
        fun getPayStat() = hourlyPayCalculations.getPayStat()
        fun getPayGross(): Double {
            return if (getPayHourly() > 0.0) {
                getPayHourly() + getCreditTotalByDate() + getCreditTotalsByPay()
            } else {
                0.0
            }
        }

        fun getPayTimeWorked() = hourlyPayCalculations.getPayTimeWorked()

        fun getCreditTotalAll() = extraCreditCalculations.getCreditTotal()

        fun getCreditTotal() = extraCreditCalculations.getCreditTotal()

        fun getCreditTotalByDate(): Double {
            var total = 0.0
            for (extra in credits.getCreditExtraAndTotalsByDate()) {
                total += extra.amount
            }
            return if (getPayHourly() > 0.0) {
                total
            } else {
                0.0
            }
        }

        fun getCreditTotalsByPay(): Double {
            var total = 0.0
            for (extra in credits.getCreditExtrasAndTotalsByPay()) {
//                Log.d(TAG, "extra is ${extra.extraName} and amount is ${extra.amount}")
                total += extra.amount
            }
            return total
        }

        fun getDebitTotalsByPay(): Double {
            var total = 0.0
            for (extra in deductions.getDebitExtraAndTotalByPay()) {
//                Log.d(TAG, "extra is ${extra.extraName} and amount is ${extra.amount}")
                total += extra.amount
            }
            return if (getPayHourly() > 0.0) {
                total
            } else {
                0.0
            }
        }
    }

    inner class TAX {

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

        fun getAllTaxDeductions(): Double {
            var totalTax = 0.0
            for (taxAndAmount in getTaxList()) {
                totalTax += taxAndAmount.amount
            }
            return if (pay.getPayHourly() > 0.0) {
                totalTax
            } else {
                0.0
            }
        }

        fun getTaxList():
                ArrayList<TaxAndAmount> {
            val taxesAndAmounts = ArrayList<TaxAndAmount>()
            for (taxType in taxTypes) {
//            Log.d(TAG, "looping through types - $type")
                var taxTotal = 0.0

                var runningRemainder =
                    when (taxType.ttBasedOn) {
                        0 -> {
                            pay.getPayTimeWorked()
                        }

                        1 -> {
                            pay.getPayHourly()
                        }

                        2 -> {
                            pay.getPayGross()
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

    private fun findRate() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.employerViewModel.getEmployerPayRates(
                employer.employerId
            ).observe(lifecycleOwner) { rates ->
                rates.listIterator().forEach {
                    if (it.eprEffectiveDate <= cutOff) {
                        payRate = it.eprPayRate
                    }
                }
            }
        }
    }

    private fun findWorkDates() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateList(
                employer.employerId, cutOff
            ).observe(lifecycleOwner) { list ->
                workDates.clear()
                list.listIterator().forEach {
                    workDates.add(it)
                }
            }
        }
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