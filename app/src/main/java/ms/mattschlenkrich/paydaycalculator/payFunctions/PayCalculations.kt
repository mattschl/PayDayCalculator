package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

//private const val TAG = "PayCalculations"

class PayCalculations(
    private val mainActivity: MainActivity,
    private val employer: Employers,
    private val mView: View,
    private val curPayPeriod: PayPeriods,
) {
    private val workDates = ArrayList<WorkDates>()
    private val workDateExtrasFull = ArrayList<WorkDateExtraAndTypeFull>()
    private val workExtrasByPay = ArrayList<ExtraDefinitionAndType>()
    private val extraTypes = ArrayList<WorkExtraTypes>()
    private val taxRules = ArrayList<WorkTaxRules>()
    private val taxTypes = ArrayList<TaxTypes>()
    private val payPeriodExtras = ArrayList<WorkPayPeriodExtras>()
    var rate = 0.0
    val hours = Hours()
    val pay = Pay()
    val extras = Extras()
    val tax = TAX()
    val deductions = Deductions()

    init {
        CoroutineScope(Dispatchers.Main).launch {

            calculateRate()
            delay(WAIT_250)
            calculateWorkDates()
            calculateExtrasCustomPerPay()
            calculateExtrasPerDay()
            calculateExtrasPerPay()
            calculateExtraTypes()
            calculateTaxRates()
        }
    }

    inner class Deductions {

        private var debitExtraAndTotalByPay: ArrayList<ExtraAndTotal>? = null

        fun getDebitExtraAndTotalByPay(): ArrayList<ExtraAndTotal> {
            if (debitExtraAndTotalByPay != null) {
                return debitExtraAndTotalByPay!!
            }
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
            debitExtraAndTotalByPay = debitList
            return debitList
        }
    }

    inner class Extras {

        private var creditExtraAndTotalByDate: ArrayList<ExtraAndTotal>? = null

        fun getCreditExtraAndTotalsByDate(): ArrayList<ExtraAndTotal> {
            if (creditExtraAndTotalByDate != null) {
                return creditExtraAndTotalByDate!!
            }
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
            creditExtraAndTotalByDate = extraList
            return extraList
        }

        private var creditExtraAndTotalByPay: ArrayList<ExtraAndTotal>? = null

        fun getCreditExtrasAndTotalsByPay(): ArrayList<ExtraAndTotal> {
            if (creditExtraAndTotalByPay != null) {
                return creditExtraAndTotalByPay!!
            }
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
            creditExtraAndTotalByPay = extraList
            return extraList
        }
    }

    inner class Hours {
        private var regHours: Double? = null
        private var otHours: Double? = null
        private var dblOtHours: Double? = null
        private var statHours: Double? = null
        private var daysWorked: Int? = null

        fun getHoursWorked(): Double {
            return getHoursReg() + getHoursOt() + getHoursDblOt()
        }

        fun getHoursReg(): Double {
            if (regHours != null) {
                return regHours!!
            } else {
                var hours = 0.0
                for (day in workDates) {
                    if (!day.wdIsDeleted) hours += day.wdRegHours
                }
                regHours = hours
                return hours
            }
        }

        fun getHoursOt(): Double {
            if (otHours != null) {
                return otHours!!
            } else {
                var hours = 0.0
                for (day in workDates) {
                    if (!day.wdIsDeleted) hours += day.wdOtHours
                }
                otHours = hours
                return hours
            }
        }

        fun getHoursDblOt(): Double {
            if (dblOtHours != null) {
                return dblOtHours!!
            } else {
                var hours = 0.0
                for (day in workDates) {
                    if (!day.wdIsDeleted) hours += day.wdDblOtHours
                }
                dblOtHours = hours
                return hours
            }
        }

        fun getHoursStat(): Double {
            if (statHours != null) {
                return statHours!!
            } else {
                var hours = 0.0
                for (day in workDates) {
                    if (!day.wdIsDeleted) hours += day.wdStatHours
                }
                statHours = hours
                return hours
            }
        }

        fun getDaysWorked(): Int {
            if (daysWorked != null) {
                return daysWorked!!
            }
            var days = 0
            for (day in workDates) {
                days++
            }
            daysWorked = days
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

        private var creditTotalByDate: Double? = null
        private var creditTotalsByPay: Double? = null
        private var debitTotalsByPay: Double? = null

        private fun getCreditTotalByDate(): Double {
            if (creditTotalByDate != null) {
                return creditTotalByDate!!
            }
            var total = 0.0
            for (extra in extras.getCreditExtraAndTotalsByDate()) {
                total += extra.amount
            }
            creditTotalByDate = if (getPayHourly() > 0.0) total else 0.0
            return creditTotalByDate!!
        }

        private fun getCreditTotalsByPay(): Double {
            if (creditTotalsByPay != null) {
                return creditTotalsByPay!!
            }
            var total = 0.0
            for (extra in extras.getCreditExtrasAndTotalsByPay()) {
//                Log.d(TAG, "extra is ${extra.extraName} and amount is ${extra.amount}")
                total += extra.amount
            }
            creditTotalsByPay = total
            return total
        }

        fun getDebitTotalsByPay(): Double {
            if (debitTotalsByPay != null) {
                return debitTotalsByPay!!
            }
            var total = 0.0
            for (extra in deductions.getDebitExtraAndTotalByPay()) {
//                Log.d(TAG, "extra is ${extra.extraName} and amount is ${extra.amount}")
                total += extra.amount
            }
            debitTotalsByPay = if (getPayHourly() > 0.0) total else 0.0
            return if (getPayHourly() > 0.0) {
                total
            } else {
                0.0
            }
        }
    }

    inner class TAX {
        private var taxFactor: Double? = null
        private fun getTaxFactor(amount: Double): Double {
            if (taxFactor != null) {
                return taxFactor!!
            }
            when (employer.payFrequency) {
                "Bi-Weekly" -> {
                    taxFactor = amount / 26
                }

                "Weekly" -> {
                    taxFactor = amount / 52
                }

                "Semi-Monthly" -> {
                    taxFactor = amount / 24
                }

                "Monthly" -> {
                    taxFactor = amount / 12
                }

                else -> {
                    taxFactor = 0.0
                }
            }
            return taxFactor!!
        }

        private var allTaxDeductions: Double? = null
        fun getAllTaxDeductions(): Double {
            if (allTaxDeductions != null) {
                return allTaxDeductions!!
            }
            var totalTax = 0.0
            for (taxAndAmount in getTaxList()) {
                totalTax += taxAndAmount.amount
            }
            allTaxDeductions = if (pay.getPayHourly() > 0.0) totalTax else 0.0
            return if (pay.getPayHourly() > 0.0) {
                totalTax
            } else {
                0.0
            }
        }

        private var taxAndAmountList: ArrayList<TaxAndAmount>? = null
        fun getTaxList():
                ArrayList<TaxAndAmount> {
            if (taxAndAmountList != null) {
                return taxAndAmountList!!
            }
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
            taxAndAmountList = taxesAndAmounts
            return taxesAndAmounts
        }
    }

    private fun calculateRate() {
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

    private fun calculateWorkDates() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateList(
                employer.employerId, curPayPeriod.ppCutoffDate
            ).observe(lifecycleOwner) { list ->
                workDates.clear()
                list.listIterator().forEach {
                    workDates.add(it)
                }
            }
        }
    }

    private fun calculateExtrasPerDay() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateExtrasPerPay(
                employer.employerId, curPayPeriod.ppCutoffDate
            ).observe(lifecycleOwner) { list ->
                workDateExtrasFull.clear()
                list.listIterator().forEach {
                    workDateExtrasFull.add(it)
                }
            }
        }
    }

    private fun calculateExtrasPerPay() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workExtraViewModel.getExtraTypesAndDef(
                employer.employerId, curPayPeriod.ppCutoffDate, 3
            ).observe(lifecycleOwner) { list ->
                workExtrasByPay.clear()
                list.listIterator().forEach {
                    workExtrasByPay.add(it)
                }
            }
        }
    }

//    private fun findPayPeriod() {
//        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
//            mainActivity.payDayViewModel.getPayPeriod(
//                cutOff, employer.employerId
//            ).observe(lifecycleOwner) { payPeriod ->
//                curPayPeriod = payPeriod
//            }
//        }
//    }

    private fun calculateExtrasCustomPerPay() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getPayPeriodExtras(
                curPayPeriod.payPeriodId
            ).observe(lifecycleOwner) { list ->
                list.listIterator().forEach {
                    payPeriodExtras.add(it)
                }
            }
        }
    }

    private fun calculateExtraTypes() {
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

    private fun calculateTaxRates() {
        var effectiveDate = ""
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workTaxViewModel.getCurrentEffectiveDate(
                curPayPeriod.ppCutoffDate
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