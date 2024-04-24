package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.util.Log
import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.model.employer.EmployerPayRates
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paydaycalculator.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDateExtraAndTypeFull
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkPayPeriodExtras
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxAndAmount
import ms.mattschlenkrich.paydaycalculator.model.tax.TaxTypes
import ms.mattschlenkrich.paydaycalculator.model.tax.WorkTaxRules
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

private const val TAG = "PayCalculations"

class PayCalculations(
    private val mainActivity: MainActivity,
    private val employer: Employers,
//    private val cutOff: String,
    private val mView: View,
    private val curPayPeriod: PayPeriods,
) {
    private var workDates: ArrayList<WorkDates>
    private var workDateExtrasFull: ArrayList<WorkDateExtraAndTypeFull>
    private var workExtrasByPay: ArrayList<ExtraDefinitionAndType>
    private var extraTypes: ArrayList<WorkExtraTypes>
    private var taxRules: ArrayList<WorkTaxRules>
    private var taxTypes: ArrayList<TaxTypes>
    private var payPeriodExtras: ArrayList<WorkPayPeriodExtras>
    var rate = 0.0
    val hours = Hours()
    val pay = Pay()
    val extras = Extras()
    val tax = TAX()
    val deductions = Deductions()

    init {
        runBlocking {
            findRate()
            val deferWorkDates =
                async { findWorkDates() }
            val deferTaxTypes =
                async { getTaxTypes() }
            taxTypes = deferTaxTypes.await()
            val deferTaxRules =
                async { getTaxTypesAndRates() }
            taxRules = deferTaxRules.await()
            workDates = deferWorkDates.await()
            val deferExtrasPerPay =
                async { getExtrasPerDay() }
            workDateExtrasFull = deferExtrasPerPay.await()
            val deferWorkExtrasByPay =
                async { getExtrasPerPay() }
            workExtrasByPay = deferWorkExtrasByPay.await()
            val deferExtraCustomPerPay =
                async { getExtrasCustomPerPay() }
            payPeriodExtras = deferExtraCustomPerPay.await()
            val deferExtraTypes =
                async { getExtraTypes() }
            extraTypes = deferExtraTypes.await()
            delay(WAIT_250)
        }
    }

    inner class Deductions {

        private fun getCustomDebitExtraAndTotalByPay(): ArrayList<ExtraAndTotal> {
            val debitList = ArrayList<ExtraAndTotal>()
            for (i in 0 until workExtrasByPay.size) {
                var notFound = true
                for (extra in payPeriodExtras) {
                    if (workExtrasByPay[i].extraType.wetName == extra.ppeName) {
                        notFound = false
                    }
                }
                if (notFound) {
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
            }
            return debitList
        }

        private fun getRegularDebitExtraAndTotalByPay(): ArrayList<ExtraAndTotal> {
            val debitList = ArrayList<ExtraAndTotal>()
            for (extra in payPeriodExtras) {
                if (!extra.ppeIsCredit &&
                    !extra.ppeIsDeleted
                ) {
                    when (extra.ppeAppliesTo) {
                        0 -> {
                            val debitTotal = if (extra.ppeIsFixed) {
                                hours.getHoursWorked() * extra.ppeValue
                            } else {
                                pay.getPayTimeWorked() +
                                        extra.ppeValue / 100
                            }
                            debitList.add(
                                ExtraAndTotal(extra.ppeName, debitTotal)
                            )
                        }

                        1 -> {
                            val debitTotal = if (extra.ppeIsFixed) {
                                hours.getDaysWorked() * extra.ppeValue
                            } else {
                                pay.getPayTimeWorked() * extra.ppeValue / 100
                            }
                            debitList.add(
                                ExtraAndTotal(extra.ppeName, debitTotal)
                            )
                        }

                        3 -> {
                            val debitTotal = if (extra.ppeIsFixed) {
                                extra.ppeValue
                            } else {
                                pay.getPayHourly() * extra.ppeValue / 100
                            }
                            debitList.add(
                                ExtraAndTotal(extra.ppeName, debitTotal)
                            )
                        }
                    }
                }
            }
            return debitList
        }

        fun getDebitExtraAndTotalByPay(): ArrayList<ExtraAndTotal> {
            val debitList = getCustomDebitExtraAndTotalByPay()
            debitList.addAll(getRegularDebitExtraAndTotalByPay())
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

        private fun getCustomCreditExtrasAndTotalsByPay(): ArrayList<ExtraAndTotal> {
            val extraList = ArrayList<ExtraAndTotal>()
            for (i in 0 until workExtrasByPay.size) {
                var notFound = true
                for (extra in payPeriodExtras) {
                    if (workExtrasByPay[i].extraType.wetName == extra.ppeName) {
                        notFound = false
                    }
                }
                if (notFound) {
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
            }
            return extraList
        }

        private fun getRegularCreditExtrasAndTotalsByPay(): ArrayList<ExtraAndTotal> {
            val extraList = ArrayList<ExtraAndTotal>()
            for (extra in payPeriodExtras) {
                if (extra.ppeIsCredit &&
                    !extra.ppeIsDeleted
                ) {
                    when (extra.ppeAppliesTo) {
                        0 -> {
                            val creditTotal = if (extra.ppeIsFixed) {
                                hours.getHoursWorked() * extra.ppeValue
                            } else {
                                pay.getPayTimeWorked() +
                                        extra.ppeValue / 100
                            }
                            extraList.add(
                                ExtraAndTotal(extra.ppeName, creditTotal)
                            )
                        }

                        1 -> {
                            val creditTotal = if (extra.ppeIsFixed) {
                                hours.getDaysWorked() * extra.ppeValue
                            } else {
                                pay.getPayTimeWorked() * extra.ppeValue / 100
                            }
                            extraList.add(
                                ExtraAndTotal(extra.ppeName, creditTotal)
                            )
                        }

                        3 -> {
                            val creditTotal = if (extra.ppeIsFixed) {
                                extra.ppeValue
                            } else {
                                pay.getPayHourly() * extra.ppeValue / 100
                            }
                            extraList.add(
                                ExtraAndTotal(extra.ppeName, creditTotal)
                            )
                        }
                    }
                }
            }
            return extraList
        }

        fun getCreditExtrasAndTotalsByPay(): ArrayList<ExtraAndTotal> {
            val extraList = getCustomCreditExtrasAndTotalsByPay()
            extraList.addAll(getRegularCreditExtrasAndTotalsByPay())
            return extraList
        }

    }

    inner class Hours {
        fun getHoursWorked(): Double {
            return getHoursReg() + getHoursOt() + getHoursDblOt()
        }

        fun getHoursReg(): Double {
            var hours = 0.0
            for (day in workDates) {
                if (!day.wdIsDeleted) hours += day.wdRegHours
            }
            return hours
        }

        fun getHoursOt(): Double {
            var hours = 0.0
            for (day in workDates) {
                if (!day.wdIsDeleted) hours += day.wdOtHours
            }
            return hours
        }

        fun getHoursDblOt(): Double {
            var hours = 0.0
            for (day in workDates) {
                if (!day.wdIsDeleted) hours += day.wdDblOtHours
            }
            return hours
        }

        fun getHoursStat(): Double {
            var hours = 0.0
            for (day in workDates) {
                if (!day.wdIsDeleted) hours += day.wdStatHours
            }
            return hours
        }

        fun getDaysWorked(): Int {
            var days = 0
            for (day in workDates) {
                days++
            }
            return days
        }
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

//        fun getCreditTotalAll(): Double {
//            return getCreditTotalByDate() + getCreditTotalsByPay()
//        }

        private fun getCreditTotalByDate(): Double {
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

        private fun getCreditTotalsByPay(): Double {
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

        suspend fun getAllTaxDeductions(): Double {
            var totalTax = 0.0
//            CoroutineScope(Dispatchers.Main).launch {

            for (taxAndAmount in getTaxList()) {
//                    Log.d(TAG, "adding taxAmount ${taxAndAmount.amount}")
                totalTax += taxAndAmount.amount
            }

            return if (pay.getPayHourly() > 0.0) {
                totalTax
            } else {
                0.0
            }
        }

        suspend fun getTaxList():
                ArrayList<TaxAndAmount> {
            val taxesAndAmounts = ArrayList<TaxAndAmount>()
//                delay(WAIT_250)
            for (taxType in taxTypes) {
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
                Log.d(TAG, "Taxes based on $runningRemainder as total")
                for (def in taxRules) {
                    if (def.wtType == taxType.taxType) {
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
                        Log.d(TAG, " in loop $taxTotal += $taxable * ${def.wtPercent}")
                    }
                }
                Log.d(TAG, "adding tax ${taxType.taxType} and $taxTotal")
                taxesAndAmounts.add(
                    TaxAndAmount(
                        taxType.taxType, taxTotal
                    )
                )
            }
            Log.d(TAG, "in getTaxList")
            return taxesAndAmounts
        }
    }

    private fun findRate() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.employerViewModel.getEmployerPayRates(
                employer.employerId
            ).observe(lifecycleOwner) { rates ->
                rates.listIterator().forEach {
                    if (it.eprEffectiveDate <= curPayPeriod.ppCutoffDate) {
                        rate = fixRateByInterval(it)
                    }
                }
            }
        }
    }

    private fun fixRateByInterval(rate: EmployerPayRates): Double {
        var fixedRate = 0.0
        when (rate.eprPerPeriod) {
            0 -> {
                fixedRate = rate.eprPayRate
            }

            1 -> {
                fixedRate = rate.eprPayRate / 8
            }

            2 -> {
                fixedRate = rate.eprPayRate / 40
            }
        }
        return fixedRate
    }

    private fun findWorkDates(): ArrayList<WorkDates> {
        val innerWorkDates = ArrayList<WorkDates>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateList(
                employer.employerId, curPayPeriod.ppCutoffDate
            ).observe(lifecycleOwner) { list ->
                list.listIterator().forEach {
                    innerWorkDates.add(it)
                }
            }
        }
        return innerWorkDates
    }

    private fun getExtrasPerDay(): ArrayList<WorkDateExtraAndTypeFull> {
        val innerExtras = ArrayList<WorkDateExtraAndTypeFull>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateExtrasPerPay(
                employer.employerId, curPayPeriod.ppCutoffDate
            ).observe(lifecycleOwner) { list ->
                list.listIterator().forEach {
                    innerExtras.add(it)
                }
            }
        }
        return innerExtras
    }

    private fun getExtrasPerPay(): ArrayList<ExtraDefinitionAndType> {
        val innerExtras = ArrayList<ExtraDefinitionAndType>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workExtraViewModel.getExtraTypesAndDef(
                employer.employerId, curPayPeriod.ppCutoffDate, 3
            ).observe(lifecycleOwner) { list ->
                list.listIterator().forEach {
                    innerExtras.add(it)
                }
            }
        }
        return innerExtras
    }

    private fun getExtrasCustomPerPay(): ArrayList<WorkPayPeriodExtras> {
        val innerExtras = ArrayList<WorkPayPeriodExtras>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getPayPeriodExtras(
                curPayPeriod.payPeriodId
            ).observe(lifecycleOwner) { list ->
                list.listIterator().forEach {
                    innerExtras.add(it)
                }
            }
        }
        return innerExtras
    }

    private fun getExtraTypes(): ArrayList<WorkExtraTypes> {
        val innerExtraTypes = ArrayList<WorkExtraTypes>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workExtraViewModel.getWorkExtraTypeList(
                employer.employerId
            ).observe(
                lifecycleOwner
            ) { list ->
                list.listIterator().forEach {
                    innerExtraTypes.add(it)
                }
            }
        }
        return innerExtraTypes
    }


    private fun getTaxTypes(): ArrayList<TaxTypes> {
        val innerTaxTypes = ArrayList<TaxTypes>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workTaxViewModel.getTaxTypesByEmployer(
                employer.employerId
            ).observe(lifecycleOwner) { types ->
                types.listIterator().forEach {
                    innerTaxTypes.add(it)
                }
            }
        }
        return innerTaxTypes
    }

    private fun getTaxTypesAndRates(): ArrayList<WorkTaxRules> {
        val innerTaxRules = ArrayList<WorkTaxRules>()
        var effectiveDate: String
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workTaxViewModel.getCurrentEffectiveDate(
                curPayPeriod.ppCutoffDate
            ).observe(lifecycleOwner) { date ->
                effectiveDate = date.toString()
                mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner2 ->
                    mainActivity.workTaxViewModel.getTaxDefByDate(
                        effectiveDate
                    ).observe(lifecycleOwner2) { list ->
                        list.listIterator().forEach {
                            Log.d(
                                TAG, "adding the tax rules def is " +
                                        "${it.wtType} rate is ${it.wtHasBracket} "
                            )
                            innerTaxRules.add(it)
                        }
                    }

                }
            }
        }
        return innerTaxRules
    }
}