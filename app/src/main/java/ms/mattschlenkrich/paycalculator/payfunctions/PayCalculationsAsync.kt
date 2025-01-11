package ms.mattschlenkrich.paycalculator.payfunctions

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerPayRates
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtraAndTypeAndDef
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.database.model.tax.TaxAndAmount
import ms.mattschlenkrich.paycalculator.database.model.tax.TaxTypes
import ms.mattschlenkrich.paycalculator.database.model.tax.WorkTaxRules
import ms.mattschlenkrich.paycalculator.ui.MainActivity

private const val TAG = "PayCalculationsAsync"

class PayCalculationsAsync(
    private val mainActivity: MainActivity,
    private val employer: Employers,
    private val currentPayPeriod: PayPeriods,
) : IPayCalculations {
    private val defaultScope = Dispatchers.Default
    private lateinit var workDates: List<WorkDates>
    private lateinit var workDateExtrasFull: List<WorkDateExtraAndTypeAndDef>
    private lateinit var workExtrasByPay: List<ExtraDefinitionAndType>
    private val workExtrasByPercentage = ArrayList<ExtraDefinitionAndType>()
    private val extraTypes = ArrayList<WorkExtraTypes>()
    private val taxRules = ArrayList<WorkTaxRules>()
    private val taxTypes = ArrayList<TaxTypes>()
    private val payPeriodExtras = ArrayList<WorkPayPeriodExtras>()
    private var taxAndAmountList: ArrayList<TaxAndAmount>? = null
    private var debitExtraAndTotalByPay: ArrayList<ExtraAndTotal>? = null
    private var creditExtraAndTotalByDate: ArrayList<ExtraAndTotal>? = null
    private var creditExtraAndTotalByPay: ArrayList<ExtraAndTotal>? = null
    private var creditExtraAndTotalByPercentage: ArrayList<ExtraAndTotal>? = null
    private var creditTotalByDate = 0.0
    private var creditTotalsByPay = 0.0
    private var creditTotalsByPercentage = 0.0
    private var debitTotalsByPay = 0.0
    private var taxDeductions = 0.0
    private var taxFactor = 0.0
    private var payRate = 0.0
    private var regHours = 0.0
    private var otHours = 0.0
    private var dblOtHours = 0.0
    private var statHours = 0.0
    private var daysWorked = 0
    private var df = DateFunctions()

    init {
        CoroutineScope(defaultScope).launch {
            val payRateDeferred = async { calculatePayRateFromDb() }
            payRate = payRateDeferred.await()
            val workDatesDeferred = async { getWorkDatesList() }
            workDates = workDatesDeferred.await()
            if (workDates.isNotEmpty()) {
                Log.d(
                    TAG, "first work date is ${workDates.first().wdDate} and last is " +
                            "${workDates.last().wdDate} and count id ${workDates.size}."
                )
            }
            if (processAllHours()) {
                Log.d(
                    TAG, "days worked: $daysWorked | reg hours: $regHours | " +
                            "ot hours: $otHours | dbl ot hours: $dblOtHours | " +
                            "stat hours $statHours"
                )
            }
            val workDateExtraFullDeferred = async { getExtraListForDates() }
            workDateExtrasFull = workDateExtraFullDeferred.await()
            val workExtrasByPayDeferred = async { getPerPayExtrasList() }
            workExtrasByPay = workExtrasByPayDeferred.await()
            var i = 1
            for (extra in workDateExtrasFull) {
                Log.d(
                    TAG,
                    "$i work date extras is ${extra.extra.wdeName} value is ${extra.extra.wdeValue}\n" +
                            "$extra"
                )
                i++
            }

//            processExtrasPerPay()
//            processExtrasCustomPerPay()
//            processExtrasPerPercentage()
//            processExtraTypes()
//            processTaxRates()
//            processCreditExtrasAndTotalsByDate()
//            processCreditExtrasAndTotalsByPay()
//            processDebitExtrasAndTotalsByPay()
//            processCreditExtrasAndTotalsByPercentage()
//            processTaxList()
//            processTaxDeductions()
        }
    }

    suspend fun calculatePayRateFromDb(): Double {
        var extractedPayRate: Double
        withContext(defaultScope) {
            val rawPay = async {
                mainActivity.payCalculationsViewModel.getPayRate(
                    employer.employerId, currentPayPeriod.ppCutoffDate
                )
            }
            extractedPayRate =
                fixRateByInterval(rawPay.await())
        }
        Log.d(TAG, "calculatePayRateFromDb returns $extractedPayRate")
        return extractedPayRate
    }

    private fun fixRateByInterval(rate: EmployerPayRates): Double {
        when (rate.eprPerPeriod) {
            0 -> return rate.eprPayRate

            1 -> return rate.eprPayRate / 8

            2 -> return rate.eprPayRate / 40
        }
        return 0.0
    }

    private suspend fun getWorkDatesList(): List<WorkDates> =
        withContext(defaultScope) {
            mainActivity.payCalculationsViewModel.getWorkDateList(
                employer.employerId, currentPayPeriod.ppCutoffDate
            )
        }


    private suspend fun processAllHours(): Boolean =
        withContext(Dispatchers.Default) {
            val daysWorkedDeferred = async {
                mainActivity.payDetailViewModel.getDaysWorked(
                    employer.employerId, currentPayPeriod.ppCutoffDate
                )
            }
            daysWorked = daysWorkedDeferred.await()
            val regHoursDeferred = async {
                mainActivity.payDetailViewModel.getHoursReg(
                    employer.employerId, currentPayPeriod.ppCutoffDate
                )
            }
            regHours = regHoursDeferred.await()
            val otHoursDeferred = async {
                mainActivity.payDetailViewModel.getHoursOt(
                    employer.employerId, currentPayPeriod.ppCutoffDate
                )
            }
            otHours = otHoursDeferred.await()
            val dblOtHoursDeferred = async {
                mainActivity.payDetailViewModel.getHoursDblOt(
                    employer.employerId, currentPayPeriod.ppCutoffDate
                )
            }
            dblOtHours = dblOtHoursDeferred.await()
            val statHoursDeferred = async {
                mainActivity.payDetailViewModel.getHoursStat(
                    employer.employerId, currentPayPeriod.ppCutoffDate
                )
            }
            statHours = statHoursDeferred.await()
            return@withContext true
        }


    private suspend fun getExtraListForDates(): List<WorkDateExtraAndTypeAndDef> =
        withContext(Dispatchers.Default) {
            mainActivity.payCalculationsViewModel.getWorkDateExtrasPerPay(
                employer.employerId, currentPayPeriod.ppCutoffDate
            )
        }

    private suspend fun getPerPayExtrasList(): List<ExtraDefinitionAndType> =
        withContext(Dispatchers.Default) {
            mainActivity.payCalculationsViewModel.getExtraTypesAndCurrentDef(
                employer.employerId, currentPayPeriod.ppCutoffDate, 3
            )
        }

//        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
//            mainActivity.workExtraViewModel.getExtraTypesAndDef(
//                employer.employerId, currentPayPeriod.ppCutoffDate, 3
//            ).observe(lifecycleOwner) { list ->
//                workExtrasByPay.clear()
//                var curType = ""
//                list.listIterator().forEach {
//                    if (curType != it.extraType.wetName) {
//                        curType = it.extraType.wetName
//                        workExtrasByPay.add(it)
//                    }
//                }
//            }
//        }
//    }

    private fun processExtrasCustomPerPay() {
//        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
//            mainActivity.payDayViewModel.getPayPeriodExtras(
//                currentPayPeriod.payPeriodId
//            ).observe(lifecycleOwner) { list ->
//                list.listIterator().forEach {
//                    payPeriodExtras.add(it)
//                }
//            }
//        }
    }

    private fun processExtrasPerPercentage() {
//        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
//            mainActivity.workExtraViewModel.getExtraTypesAndDef(
//                employer.employerId,
//                currentPayPeriod.ppCutoffDate, 4
//            ).observe(lifecycleOwner) { list ->
//                workExtrasByPercentage.clear()
//                list.listIterator().forEach {
//                    workExtrasByPercentage.add(it)
//                }
//            }
//        }
    }

    private fun processExtraTypes() {
//        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
//            mainActivity.workExtraViewModel.getWorkExtraTypeList(
//                employer.employerId
//            ).observe(
//                lifecycleOwner
//            ) { list ->
//                extraTypes.clear()
//                list.listIterator().forEach {
//                    extraTypes.add(it)
//                }
//            }
//        }
    }

    private fun processTaxRates() {
//        var effectiveDate = ""
//        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
//            mainActivity.workTaxViewModel.getCurrentEffectiveDate(
//                currentPayPeriod.ppCutoffDate
//            ).observe(lifecycleOwner) { date ->
//                effectiveDate = if (date.isNullOrBlank()) {
//                    df.getCurrentDateAsString()
//                } else {
//                    date
//                }
//            }
//        }
//        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
//            mainActivity.workTaxViewModel.getTaxTypesByEmployer(
//                employer.employerId
//            ).observe(lifecycleOwner) { types ->
//                if (types.isNotEmpty()) {
//                    taxTypes.clear()
//                    types.listIterator().forEach {
//                        taxTypes.add(it)
//                    }
//                }
//            }
//        }
//        CoroutineScope(Dispatchers.Main).launch {
//            delay(WAIT_250)
//            mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
//                mainActivity.workTaxViewModel.getTaxDefByDate(
//                    effectiveDate
//                ).observe(lifecycleOwner) { list ->
//                    if (list.isNotEmpty()) {
//                        taxRules.clear()
//                        list.listIterator().forEach {
//                            taxRules.add(it)
//
//                        }
//                    }
//                }
//            }
//        }
    }

//    private fun processCreditExtrasAndTotalsByDate() {
//        val extraList = ArrayList<ExtraAndTotal>()
//        var total = 0.0
//        for (i in 0 until workDateExtrasFull.size) {
//            if (workDateExtrasFull[i].extra.wdeIsCredit) {
//                if (workDateExtrasFull[i].extra.wdeAppliesTo == 0 &&
//                    workDateExtrasFull[i].extra.wdeAttachTo == 1 &&
//                    workDateExtrasFull[i].extra.wdeIsFixed
//                ) {
//                    for (date in workDates) {
//                        if (date.workDateId == workDateExtrasFull[i].extra.wdeWorkDateId) {
//                            total += workDateExtrasFull[i].extra.wdeValue * (
//                                    date.wdRegHours + date.wdOtHours + date.wdDblOtHours
//                                    )
//                        }
//                    }
//                } else if (workDateExtrasFull[i].extra.wdeAppliesTo == 0 &&
//                    workDateExtrasFull[i].extra.wdeAttachTo == 1 &&
//                    !workDateExtrasFull[i].extra.wdeIsFixed
//                ) {
//                    for (date in workDates) {
//                        if (date.workDateId == workDateExtrasFull[i]
//                                .extra.wdeWorkDateId
//                        ) {
//                            total += workDateExtrasFull[i].extra.wdeValue * payRate * (
//                                    date.wdRegHours + date.wdOtHours + date.wdDblOtHours
//                                    )
//                        }
//                    }
//                } else if (workDateExtrasFull[i].extra.wdeAppliesTo == 1 &&
//                    workDateExtrasFull[i].extra.wdeAttachTo == 1 &&
//                    workDateExtrasFull[i].extra.wdeIsFixed
//                ) {
//                    for (date in workDates) {
//                        if (date.workDateId == workDateExtrasFull[i].extra.wdeWorkDateId) {
//                            total += workDateExtrasFull[i].extra.wdeValue
//                        }
//                    }
//                } else if (workDateExtrasFull[i].extra.wdeAppliesTo == 1 &&
//                    workDateExtrasFull[i].extra.wdeAttachTo == 1 &&
//                    !workDateExtrasFull[i].extra.wdeIsFixed
//                ) {
//                    for (date in workDates) {
//                        if (date.workDateId == workDateExtrasFull[i].extra.wdeWorkDateId) {
//                            total += workDateExtrasFull[i].extra.wdeValue * payRate * (
//                                    date.wdRegHours + date.wdOtHours + date.wdDblOtHours
//                                    )
//                        }
//                    }
//                }
//            }
//            if (workDateExtrasFull.size == 1) {
//                extraList.add(ExtraAndTotal(workDateExtrasFull[i].extra.wdeName, total))
//                total = 0.0
//            } else if (i < workDateExtrasFull.size - 1 &&
//                (workDateExtrasFull[i].extra.wdeName != workDateExtrasFull[i + 1].extra.wdeName)
//            ) {
//                extraList.add(ExtraAndTotal(workDateExtrasFull[i].extra.wdeName, total))
//                total = 0.0
//            } else if (i == workDateExtrasFull.size - 1) {
//                extraList.add(
//                    ExtraAndTotal(
//                        workDateExtrasFull[i].extra.wdeName, total
//                    )
//                )
//                total = 0.0
//            }
//        }
//        creditExtraAndTotalByDate = extraList
//        var subTotal = 0.0
//        if (!creditExtraAndTotalByDate.isNullOrEmpty()) {
//            for (extra in creditExtraAndTotalByDate!!) {
//                subTotal += extra.amount
//            }
//        }
//        creditTotalByDate = if (getPayAllHourly() > 0.0) subTotal else 0.0
//    }

//    private fun processDebitExtrasAndTotalsByPay() {
//        val debitList = ArrayList<ExtraAndTotal>()
//        for (i in 0 until workExtrasByPay.size) {
//            var notFound = true
//            for (extra in payPeriodExtras) {
//                if (workExtrasByPay[i].extraType.wetName == extra.ppeName) {
//                    notFound = false
//                }
//            }
//            if (notFound) {
//                if (!workExtrasByPay[i].extraType.wetIsCredit &&
//                    workExtrasByPay[i].extraType.wetIsDefault &&
//                    workExtrasByPay[i].extraType.wetAppliesTo == 3
//                ) {
//                    if (workExtrasByPay[i].definition.weIsFixed
//                    ) {
//                        debitList.add(
//                            ExtraAndTotal(
//                                workExtrasByPay[i].extraType.wetName,
//                                workExtrasByPay[i].definition.weValue
//                            )
//                        )
//                    } else {
//                        debitList.add(
//                            ExtraAndTotal(
//                                workExtrasByPay[i].extraType.wetName,
//                                workExtrasByPay[i].definition.weValue *
//                                        getPayTimeWorked() / 100
//                            )
//                        )
//                    }
//                } else if (!workExtrasByPay[i].extraType.wetIsCredit &&
//                    workExtrasByPay[i].extraType.wetIsDefault &&
//                    workExtrasByPay[i].extraType.wetAppliesTo == 0 &&
//                    !workExtrasByPay[i].definition.weIsFixed
//                ) {
//                    debitList.add(
//                        ExtraAndTotal(
//                            workExtrasByPay[i].extraType.wetName,
//                            workExtrasByPay[i].definition.weValue *
//                                    getPayTimeWorked() / 100
//                        )
//                    )
//                }
//            }
//        }
//        for (extra in payPeriodExtras) {
//            if (!extra.ppeIsCredit &&
//                !extra.ppeIsDeleted
//            ) {
//                when (extra.ppeAppliesTo) {
//                    0 -> {
//                        val debitTotal = if (extra.ppeIsFixed) {
//                            getHoursWorked() * extra.ppeValue
//                        } else {
//                            getPayTimeWorked() +
//                                    extra.ppeValue / 100
//                        }
//                        debitList.add(
//                            ExtraAndTotal(extra.ppeName, debitTotal)
//                        )
//                    }
//
//                    1 -> {
//                        val debitTotal = if (extra.ppeIsFixed) {
//                            getDaysWorked() * extra.ppeValue
//                        } else {
//                            getPayTimeWorked() * extra.ppeValue / 100
//                        }
//                        debitList.add(
//                            ExtraAndTotal(extra.ppeName, debitTotal)
//                        )
//                    }
//
//                    3 -> {
//                        val debitTotal = if (extra.ppeIsFixed) {
//                            extra.ppeValue
//                        } else {
//                            getPayAllHourly() * extra.ppeValue / 100
//                        }
//                        debitList.add(
//                            ExtraAndTotal(extra.ppeName, debitTotal)
//                        )
//                    }
//                }
//            }
//        }
//        debitExtraAndTotalByPay = debitList
//        var subTotal = 0.0
//        if (!debitExtraAndTotalByPay.isNullOrEmpty())
//            for (extra in debitExtraAndTotalByPay!!) {
//                subTotal += extra.amount
//            }
//        debitTotalsByPay = if (getPayAllHourly() > 0.0) subTotal else 0.0
//    }

//    private fun processCreditExtrasAndTotalsByPay() {
//        val extraList = ArrayList<ExtraAndTotal>()
//        for (i in 0 until workExtrasByPay.size) {
//            var notFound = true
//            for (extra in payPeriodExtras) {
//                if (workExtrasByPay[i].extraType.wetName ==
//                    extra.ppeName
//                ) {
//                    notFound = false
//                }
//            }
//            if (notFound) {
//                if (workExtrasByPay[i].extraType.wetIsCredit &&
//                    workExtrasByPay[i].extraType.wetIsDefault &&
//                    workExtrasByPay[i].extraType.wetAppliesTo == 3
//                ) {
//                    if (workExtrasByPay[i].definition.weIsFixed
//                    ) {
//                        extraList.add(
//                            ExtraAndTotal(
//                                workExtrasByPay[i].extraType.wetName,
//                                workExtrasByPay[i].definition.weValue
//                            )
//                        )
//                    } else {
//                        extraList.add(
//                            ExtraAndTotal(
//                                workExtrasByPay[i].extraType.wetName,
//                                workExtrasByPay[i].definition.weValue *
//                                        getPayTimeWorked() / 100
//                            )
//                        )
//                    }
//                } else if (workExtrasByPay[i].extraType.wetIsCredit &&
//                    workExtrasByPay[i].extraType.wetIsDefault &&
//                    workExtrasByPay[i].extraType.wetAppliesTo == 0 &&
//                    !workExtrasByPay[i].definition.weIsFixed
//                ) {
//                    extraList.add(
//                        ExtraAndTotal(
//                            workExtrasByPay[i].extraType.wetName,
//                            workExtrasByPay[i].definition.weValue *
//                                    getPayTimeWorked() / 100
//                        )
//                    )
//                }
//            }
//        }
//        for (extra in payPeriodExtras) {
//            if (extra.ppeIsCredit &&
//                !extra.ppeIsDeleted
//            ) {
//                when (extra.ppeAppliesTo) {
//                    0 -> {
//                        val creditTotal = if (extra.ppeIsFixed) {
//                            getHoursWorked() * extra.ppeValue
//                        } else {
//                            getPayTimeWorked() *
//                                    extra.ppeValue / 100
//                        }
//                        extraList.add(
//                            ExtraAndTotal(extra.ppeName, creditTotal)
//                        )
//                    }
//
//                    1 -> {
//                        val creditTotal = if (extra.ppeIsFixed) {
//                            getDaysWorked() * extra.ppeValue
//                        } else {
//                            getPayTimeWorked() * extra.ppeValue / 100
//                        }
//                        extraList.add(
//                            ExtraAndTotal(extra.ppeName, creditTotal)
//                        )
//                    }
//
//                    3 -> {
//                        val creditTotal = if (extra.ppeIsFixed) {
//                            extra.ppeValue
//                        } else {
//                            getPayAllHourly() * extra.ppeValue / 100
//                        }
//                        extraList.add(
//                            ExtraAndTotal(extra.ppeName, creditTotal)
//                        )
//                    }
//
//                    4 -> {
//                        val creditTotal =
//                            getPayGross() * extra.ppeValue / 100
//                        extraList.add(
//                            ExtraAndTotal(extra.ppeName, creditTotal)
//                        )
//                    }
//                }
//            }
//        }
//        creditExtraAndTotalByPay = extraList
//        var subTotal = 0.0
//        if (!creditExtraAndTotalByPay.isNullOrEmpty()) {
//            for (extra in creditExtraAndTotalByPay!!) {
//                subTotal += extra.amount
//            }
//        }
//        creditTotalsByPay = subTotal
//    }
//
//    private fun processCreditExtrasAndTotalsByPercentage() {
//        val extraList = ArrayList<ExtraAndTotal>()
//        for (i in 0 until workExtrasByPercentage.size) {
//            var notFound = true
//            for (extra in payPeriodExtras) {
//                if (workExtrasByPercentage[i].extraType.wetName ==
//                    extra.ppeName
//                ) {
//                    notFound = false
//                }
//            }
//            if (notFound) {
//                if (workExtrasByPercentage[i].extraType.wetIsCredit &&
//                    workExtrasByPercentage[i].extraType.wetIsDefault &&
//                    workExtrasByPercentage[i].extraType.wetAppliesTo == 4
//                ) {
//                    if (!workExtrasByPercentage[i].definition.weIsFixed) {
//                        val extraValue = workExtrasByPercentage[i].definition.weValue *
//                                (getPayAllHourly() + creditTotalByDate + creditTotalsByPay) / 100
//                        extraList.add(
//                            ExtraAndTotal(
//                                workExtrasByPercentage[i].extraType.wetName,
//                                extraValue
//                            )
//                        )
//                        creditTotalsByPercentage = extraValue
//                    }
//                }
//            }
//            creditExtraAndTotalByPercentage = extraList
//        }
//    }

//    private fun processTaxList() {
//        val taxesAndAmounts = ArrayList<TaxAndAmount>()
//        for (taxType in taxTypes) {
////            Log.d(TAG, "looping through types - $type")
//            var taxTotal = 0.0
//
//            var runningRemainder =
//                when (taxType.ttBasedOn) {
//                    0 -> {
//                        getPayTimeWorked()
//                    }
//
//                    1 -> {
//                        getPayAllHourly()
//                    }
//
//                    2 -> {
//                        getPayGross()
//                    }
//
//                    else -> {
//                        0.0
//                    }
//                }
//            for (definition in taxRules) {
//                if (definition.wtType == taxType.taxType && runningRemainder > 0) {
//                    var taxable: Double
//                    runningRemainder -=
//                        if (definition.wtHasExemption) {
//                            getTaxFactor(definition.wtExemptionAmount)
//                        } else {
//                            0.0
//                        }
//                    if (runningRemainder < 0.0) {
//                        runningRemainder = 0.0
//                    }
//                    if (definition.wtHasBracket &&
//                        runningRemainder >= getTaxFactor(definition.wtBracketAmount)
//                    ) {
//                        taxable = getTaxFactor(definition.wtBracketAmount)
//                        runningRemainder -= getTaxFactor(definition.wtBracketAmount)
//                    } else {
//                        taxable = runningRemainder
//                        runningRemainder = 0.0
//                    }
//                    taxTotal += taxable * definition.wtPercent
//                }
//            }
//            taxesAndAmounts.add(
//                TaxAndAmount(
//                    taxType.taxType, taxTotal
//                )
//            )
//        }
//        taxAndAmountList = taxesAndAmounts
//    }
//
//    private fun processTaxDeductions() {
//        taxDeductions = 0.0
//        if (!taxAndAmountList.isNullOrEmpty()) {
//            for (taxAndAmount in taxAndAmountList!!) {
//                taxDeductions += taxAndAmount.amount
//            }
//        }
//    }

    private fun getTaxFactor(amount: Double): Double {
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
        return taxFactor
    }

    override fun getDebitExtrasListByPay(): List<ExtraAndTotal>? {
        return debitExtraAndTotalByPay
    }

    override fun getCreditExtrasListByDate(): List<ExtraAndTotal>? {
        return creditExtraAndTotalByDate
    }

    override fun getCreditExtrasListByPay(): List<ExtraAndTotal>? {
        return creditExtraAndTotalByPay
    }

    override fun getCreditExtrasListByPercentageOfAll(): List<ExtraAndTotal>? {
        return creditExtraAndTotalByPercentage
    }

    override fun getDebitTotalsByPay(): Double {
        return debitTotalsByPay
    }

    override fun getCreditTotalAll(): Double {
        return creditTotalByDate + creditTotalsByPay + creditTotalsByPercentage
    }

    override fun getHoursWorked(): Double {
        return regHours + otHours + dblOtHours
    }

    override fun getHoursAll(): Double {
        return getHoursWorked() + statHours
    }

    override fun getHoursReg(): Double {
        return regHours
    }

    override fun getHoursOt(): Double {
        return otHours
    }

    override fun getHoursDblOt(): Double {
        return dblOtHours
    }

    override fun getHoursStat(): Double {
        return statHours
    }

    override fun getDaysWorked(): Int {
        return daysWorked
    }

    override fun getPayRate(): Double {
        return payRate
    }

    override fun getPayReg(): Double {
        return regHours * payRate
    }

    override fun getPayOt(): Double {
        return otHours * payRate * 1.5
    }

    override fun getPayDblOt(): Double {
        return dblOtHours * payRate * 2
    }

    override fun getPayAllHourly(): Double {
        return getPayReg() + getPayOt() + getPayDblOt() + getPayStat()
    }

    override fun getPayStat(): Double {
        return statHours * payRate
    }

    override fun getPayGross(): Double {
        return getPayAllHourly() + creditTotalByDate + creditTotalsByPay + creditTotalsByPercentage
    }

    override fun getPayTimeWorked(): Double {
        return getPayReg() + getPayOt() + getPayDblOt()
    }

    override fun getAllTaxDeductions(): Double {
        return taxDeductions
    }

    override fun getCredits(): List<ExtraAndTotal>? {
        TODO("Not yet implemented")
    }

    override fun getTaxList(): List<TaxAndAmount>? {
        return taxAndAmountList
    }
}