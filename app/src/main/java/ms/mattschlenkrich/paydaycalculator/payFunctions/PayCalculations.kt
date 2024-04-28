package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDateExtraAndTypeFull
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
    private val workDateExtrasFull = ArrayList<WorkDateExtraAndTypeFull>()
    private val workExtrasByPay = ArrayList<ExtraDefinitionAndType>()
    private val extraTypes = ArrayList<WorkExtraTypes>()

    private val taxRules = ArrayList<WorkTaxRules>()
    private val taxTypes = ArrayList<TaxTypes>()
    private lateinit var hourlyCalculations: HourlyCalculations
    var rate = 0.0
    val hours = Hours()
    val pay = Pay()
    val extras = Extras()
    val tax = TAX()
    val deductions = Deductions()

    init {
        findWorkDates()
        findExtrasPerDay()
        findExtrasPerPay()
        findRate()
        findExtraTypes()
        findTaxRates()
        calculateHours()
    }

    private fun calculateHours() {
        hourlyCalculations = HourlyCalculations(workDates)
    }

    inner class Deductions {
        fun getDebitExtraAndTotalByPay(): ArrayList<ExtraAndTotal> {
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
                                        pay.getPayTimeWorked() / 100
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
                                    pay.getPayTimeWorked() / 100
                        )
                    )
                }
            }
            return debitList
        }
    }

    inner class Extras {

        fun getCreditExtraAndTotalsByDate(): ArrayList<ExtraAndTotal> {
            val extraList = ArrayList<ExtraAndTotal>()
            var total = 0.0
            for (i in 0 until workDateExtrasFull.size) {
                if (workDateExtrasFull[i].extra.wdeIsCredit) {
                    if (workDateExtrasFull[i].extra.wdeAppliesTo == 0 &&
                        workDateExtrasFull[i].extra.wdeAttachTo == 1 &&
                        workDateExtrasFull[i].extra.wdeIsFixed
                    ) {
                        for (date in workDates) {
                            if (date.workDateId == workDateExtrasFull[i].extra.wdeWorkDateId) {
                                total += workDateExtrasFull[i].extra.wdeValue * (
                                        date.wdRegHours + date.wdOtHours + date.wdDblOtHours
                                        )
                            }
                        }
                    } else if (workDateExtrasFull[i].extra.wdeAppliesTo == 0 &&
                        workDateExtrasFull[i].extra.wdeAttachTo == 1 &&
                        !workDateExtrasFull[i].extra.wdeIsFixed
                    ) {
                        for (date in workDates) {
                            if (date.workDateId == workDateExtrasFull[i].extra.wdeWorkDateId) {
                                total += workDateExtrasFull[i].extra.wdeValue * rate * (
                                        date.wdRegHours + date.wdOtHours + date.wdDblOtHours
                                        )
                            }
                        }
                    } else if (workDateExtrasFull[i].extra.wdeAppliesTo == 1 &&
                        workDateExtrasFull[i].extra.wdeAttachTo == 1 &&
                        workDateExtrasFull[i].extra.wdeIsFixed
                    ) {
                        for (date in workDates) {
                            if (date.workDateId == workDateExtrasFull[i].extra.wdeWorkDateId) {
                                total += workDateExtrasFull[i].extra.wdeValue
                            }
                        }
                    } else if (workDateExtrasFull[i].extra.wdeAppliesTo == 1 &&
                        workDateExtrasFull[i].extra.wdeAttachTo == 1 &&
                        !workDateExtrasFull[i].extra.wdeIsFixed
                    ) {
                        for (date in workDates) {
                            if (date.workDateId == workDateExtrasFull[i].extra.wdeWorkDateId) {
                                total += workDateExtrasFull[i].extra.wdeValue * rate * (
                                        date.wdRegHours + date.wdOtHours + date.wdDblOtHours
                                        )
                            }
                        }
                    }
                }
                if (workDateExtrasFull.size == 1) {
                    extraList.add(ExtraAndTotal(workDateExtrasFull[i].extra.wdeName, total))
                    total = 0.0
                } else if (i < workDateExtrasFull.size - 1 &&
                    (workDateExtrasFull[i].extra.wdeName != workDateExtrasFull[i + 1].extra.wdeName)
                ) {
                    extraList.add(ExtraAndTotal(workDateExtrasFull[i].extra.wdeName, total))
                    total = 0.0
                } else if (i == workDateExtrasFull.size - 1) {
                    extraList.add(ExtraAndTotal(workDateExtrasFull[i].extra.wdeName, total))
                    total = 0.0
                }
            }
            return extraList
        }

        fun getCreditExtrasAndTotalsByPay(): ArrayList<ExtraAndTotal> {
            val extraList = ArrayList<ExtraAndTotal>()
            for (i in 0 until workExtrasByPay.size) {
                if (workExtrasByPay[i].extraType.wetIsCredit &&
                    workExtrasByPay[i].extraType.wetIsDefault &&
                    workExtrasByPay[i].extraType.wetAppliesTo == 3
                ) {
                    if (workExtrasByPay[i].definition.weIsFixed
                    ) {
                        extraList.add(
                            ExtraAndTotal(
                                workExtrasByPay[i].extraType.wetName,
                                workExtrasByPay[i].definition.weValue
                            )
                        )
                    } else {
                        extraList.add(
                            ExtraAndTotal(
                                workExtrasByPay[i].extraType.wetName,
                                workExtrasByPay[i].definition.weValue *
                                        pay.getPayTimeWorked() / 100
                            )
                        )
                    }
                } else if (workExtrasByPay[i].extraType.wetIsCredit &&
                    workExtrasByPay[i].extraType.wetIsDefault &&
                    workExtrasByPay[i].extraType.wetAppliesTo == 0 &&
                    !workExtrasByPay[i].definition.weIsFixed
                ) {
                    extraList.add(
                        ExtraAndTotal(
                            workExtrasByPay[i].extraType.wetName,
                            workExtrasByPay[i].definition.weValue *
                                    pay.getPayTimeWorked() / 100

                        )
                    )
                }
            }
            return extraList
        }
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
        fun getPayReg(): Double {
            return hours.getHoursReg() * rate
        }

        fun getPayOt(): Double {
            return hours.getHoursOt() * rate * 1.5
        }

        fun getPayDblOt(): Double {
            return hours.getHoursDblOt() * rate * 2
        }

        fun getPayHourly(): Double {
            return getPayTimeWorked() + getPayStat()
        }

        fun getPayStat(): Double {
            return hours.getHoursStat() * rate
        }

//        fun getPayNet(): Double {
//            return if (getPayGross() - getDebitTotalsByPay() - tax.getAllTaxDeductions() > 0.0) {
//                getPayGross() - getDebitTotalsByPay() - tax.getAllTaxDeductions()
//            } else {
//                0.0
//            }
//        }

        fun getPayGross(): Double {
            return if (getPayHourly() > 0.0) {
                getPayHourly() + getCreditTotalByDate() + getCreditTotalsByPay()
            } else {
                0.0
            }
        }

        fun getPayTimeWorked(): Double {
            return getPayReg() + getPayOt() + getPayDblOt()
        }

        fun getCreditTotalAll(): Double {
            return getCreditTotalByDate() + getCreditTotalsByPay()
        }

        fun getCreditTotalByDate(): Double {
            var total = 0.0
            for (extra in extras.getCreditExtraAndTotalsByDate()) {
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
            for (extra in extras.getCreditExtrasAndTotalsByPay()) {
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
                        rate = it.eprPayRate
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

    private fun findExtrasPerDay() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateExtrasPerPay(
                employer.employerId, cutOff
            ).observe(lifecycleOwner) { list ->
                workDateExtrasFull.clear()
                list.listIterator().forEach {
                    workDateExtrasFull.add(it)
                }
            }
        }
    }

    private fun findExtrasPerPay() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workExtraViewModel.getExtraTypesAndDef(
                employer.employerId, cutOff, 3
            ).observe(lifecycleOwner) { list ->
                workExtrasByPay.clear()
                list.listIterator().forEach {
                    workExtrasByPay.add(it)
                }
            }
        }
    }

    private fun findExtraTypes() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workExtraViewModel.getWorkExtraTypeList(
                employer.employerId
            ).observe(
                lifecycleOwner
            ) { list ->
                extraTypes.clear()
                list.listIterator().forEach {
                    extraTypes.add(it)
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