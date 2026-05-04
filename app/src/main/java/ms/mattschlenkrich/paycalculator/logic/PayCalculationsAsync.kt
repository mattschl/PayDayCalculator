package ms.mattschlenkrich.paycalculator.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.common.ExtraAppliesToFrequencies
import ms.mattschlenkrich.paycalculator.common.ExtraAttachToFrequencies
import ms.mattschlenkrich.paycalculator.common.PayRateBasedOn
import ms.mattschlenkrich.paycalculator.common.TaxBasedOn
import ms.mattschlenkrich.paycalculator.data.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.data.TaxAndAmount
import ms.mattschlenkrich.paycalculator.data.entity.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.entity.TaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkTaxRules
import ms.mattschlenkrich.paycalculator.data.model.ExtraContainer
import ms.mattschlenkrich.paycalculator.data.model.ExtraDefinitionAndType

//private const val TAG = "PayCalculationsAsync"

class PayCalculationsAsync(
    private val payCalculationsViewModel: PayCalculationsViewModel,
    private val payDetailViewModel: PayDetailViewModel,
    private val employer: Employers,
    private val currentPayPeriod: PayPeriods,
) : IPayCalculations {
    private val defaultScope = Dispatchers.Default
    private val calculationJob = CoroutineScope(defaultScope).launch {
        getListsFromDb()
        processExtraContainers()
        processPayTotals()
    }

    override suspend fun waitForCalculations() {
        calculationJob.join()
    }

    private lateinit var workDates: List<WorkDates>
    private lateinit var customWorkDateExtras: List<WorkDateExtras>
    private lateinit var customExtrasByPay: List<WorkPayPeriodExtras>
    private val defaultExtrasByHour = ArrayList<ExtraDefinitionAndType>()
    private val defaultExtrasByDay = ArrayList<ExtraDefinitionAndType>()
    private val defaultExtrasByPay = ArrayList<ExtraDefinitionAndType>()
    private val defaultExtrasByPercentageOfAll = ArrayList<ExtraDefinitionAndType>()
    private lateinit var extraTypes: List<WorkExtraTypes>
    private lateinit var taxRules: List<WorkTaxRules>
    private lateinit var taxTypes: List<TaxTypes>
    private var taxAndAmountList = ArrayList<TaxAndAmount>()
    private val creditExtraContainers = ArrayList<ExtraContainer>()
    private val debitExtraContainers = ArrayList<ExtraContainer>()
    private var creditExtraContainersByPercentage = ArrayList<ExtraContainer>()
    private var creditTotalsBeforePercentageAdjustment = 0.0
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
    private var totalBeforePercentageAdjustment = 0.0

    private suspend fun getListsFromDb() {
        getPayRateFromDb()
        getWorkDatesFromDb()
        processAllHours()
        getCustomWorkDateExtrasFromDb()
        getCustomExtrasByPayFromDb()
        getDefaultWorkExtrasFromDbAndRemoveDuplicates()
        getExtraTypesFromDb()
        getTaxRatesFromDb()
    }

    private suspend fun processExtraContainers() {
        processCustomExtraContainersByDates()
        processCustomExtraContainersByPay()
        processDefaultExtraContainersByHour()
        processDefaultExtraContainersByDay()
        processDefaultExtraContainersByPay()
        calculateTotalsBeforePercentageAdjustment()
        processDefaultExtraContainersByPercentageOfAll()
        calculateTotalPercentageOfAll()
        calculateDeductions()
    }

    private suspend fun processPayTotals() {
//        calculateTaxAndAmounts()
        calculateTaxesAndPopulateList()
        calculateTaxTotal()
    }

    private suspend fun calculateDeductions() {
        withContext(defaultScope) {
            var subTotal = 0.0
            for (debit in debitExtraContainers) {
                subTotal += debit.amount
            }
            debitTotalsByPay = subTotal
        }
    }


    private suspend fun processCustomExtraContainersByPay() =
        withContext(defaultScope) {
            for (extra in customExtrasByPay) {
                val value = if (!extra.ppeIsFixed && extra.ppeValue >= 1.0)
                    extra.ppeValue / 100.0
                else extra.ppeValue
                val total: Double = if (extra.ppeIsDeleted) {
                    0.0
                } else if (extra.ppeIsFixed) {
                    when (extra.ppeAppliesTo) {
                        ExtraAttachToFrequencies.HOURLY.value -> getHoursWorked() * value
                        ExtraAttachToFrequencies.DAILY.value -> getDaysWorked() * value
                        ExtraAttachToFrequencies.PER_PAY.value -> value
                        else -> value
                    }
                } else {
                    when (extra.ppeAppliesTo) {
                        ExtraAttachToFrequencies.HOURLY.value -> getPayTimeWorked() * value
                        ExtraAttachToFrequencies.DAILY.value -> getPayTimeWorked() * value
                        ExtraAttachToFrequencies.PER_PAY.value -> getPayAllHourly() * value
                        else -> value
                    }
                }
//                Log.d(TAG, "-------- value of $total for ${extra.ppeName} ---------")
                val newExtraContainer = ExtraContainer(
                    extra.ppeName,
                    total,
                    null,
                    null,
                    extra,
                )
                if (extra.ppeIsCredit) {
                    creditExtraContainers.add(
                        newExtraContainer
                    )
                } else {
                    debitExtraContainers.add(
                        newExtraContainer
                    )
                }
            }
        }

    private suspend fun calculateTaxTotal() =
        withContext(defaultScope) {
            if (taxAndAmountList.isNotEmpty()) {
                for (taxAndAmount in taxAndAmountList) {
                    taxDeductions += taxAndAmount.amount
                }
            }
        }

    private suspend fun calculateTaxesAndPopulateList() =
        withContext(defaultScope) {
            for (type in taxTypes) {
                var subTotal = 0.0
                var runningRemainder =
                    when (type.ttBasedOn) {
                        TaxBasedOn.TIME_WORKED_ONLY.value -> getPayTimeWorked()
                        TaxBasedOn.TIME_WORK_AND_STATS.value -> getPayAllHourly()
                        TaxBasedOn.TIME_WORKED_STATS_AND_EXTRAS.value -> getPayGross()
                        else -> 0.0
                    }
//                Log.d(TAG, "\n------------------------------------------------------")
//                Log.d(TAG, "tax rules size is ${taxRules.size}")
                var previousBracket = 0.0
                val tDefinitions = taxRules.filter { tRule -> tRule.wtType == type.taxType }
//                Log.d(TAG, "size of filtered list is ${tDefinitions.size}")
                for (rule in tDefinitions) {
//                    Log.d(TAG, "calculating level ${rule.wtLevel}")
                    if (runningRemainder > 0.0
                    ) {
                        var taxable: Double
//                        Log.d(TAG, "Tax calculations for ${type.taxType}")
//                        Log.d(TAG, "Running remainder BEFORE exemption $runningRemainder")
                        runningRemainder -=
                            if (rule.wtHasExemption) {
                                getTotalAdjustedForTax(rule.wtExemptionAmount)
                            } else {
                                0.0
                            }
//                        Log.d(TAG, "Running remainder AFTER exemption $runningRemainder")
                        if (runningRemainder < 0.0) {
                            runningRemainder = 0.0
                        }
                        if (rule.wtHasBracket &&
                            runningRemainder >= getTotalAdjustedForTax(rule.wtBracketAmount - previousBracket)
                        ) {
//                            Log.d(TAG, "Bracket is ${rule.wtBracketAmount}")
                            taxable = getTotalAdjustedForTax(rule.wtBracketAmount - previousBracket)
//                            Log.d(TAG, "taxable = $taxable")
                            runningRemainder -= taxable
                            previousBracket = rule.wtBracketAmount
                        } else {
                            taxable = runningRemainder
                            runningRemainder = 0.0
                        }
//                        Log.d(TAG, "New running remainder is $runningRemainder")
                        subTotal += taxable * rule.wtPercent
//                        Log.d(TAG, "running tax total is $subTotal")
                    }
                }
                taxAndAmountList.add(
                    TaxAndAmount(
                        type.taxType, subTotal
                    )
                )
            }
        }

    private suspend fun calculateTotalPercentageOfAll() =
        withContext(defaultScope) {
            if (creditExtraContainersByPercentage.isNotEmpty()) {
                var subTotal = 0.0
                for (credit in creditExtraContainersByPercentage) {
                    subTotal += credit.amount
                }
                creditTotalsByPercentage = subTotal
            }
        }

    private suspend fun processDefaultExtraContainersByPercentageOfAll() =
        withContext(defaultScope) {
            if (defaultExtrasByPercentageOfAll.isNotEmpty()) {
                for (extraAndDef in defaultExtrasByPercentageOfAll) {
                    val value =
                        if (!extraAndDef.definition.weIsFixed && extraAndDef.definition.weValue >= 1.0)
                            extraAndDef.definition.weValue / 100.0
                        else extraAndDef.definition.weValue
                    val subTotal =
                        if (extraAndDef.extraType.wetIsDeleted ||
                            extraAndDef.definition.weIsDeleted
                        ) {
                            0.0
                        } else {
                            value * totalBeforePercentageAdjustment
                        }
                    if (extraAndDef.extraType.wetIsCredit) {
                        creditExtraContainers.add(
                            ExtraContainer(
                                extraAndDef.extraType.wetName,
                                subTotal,
                                extraAndDef,
                                null,
                                null
                            )
                        )
                        creditExtraContainersByPercentage.add(
                            ExtraContainer(
                                extraAndDef.extraType.wetName,
                                subTotal,
                                extraAndDef,
                                null,
                                null
                            )
                        )
                    } else {
                        debitExtraContainers.add(
                            ExtraContainer(
                                extraAndDef.extraType.wetName,
                                subTotal,
                                extraAndDef,
                                null,
                                null
                            )
                        )
                    }
                }
            }
        }

    private suspend fun calculateTotalsBeforePercentageAdjustment() =
        withContext(defaultScope) {
            var subTotal = getPayAllHourly()
            var tempTotal = 0.0
            for (credit in creditExtraContainers) {
                subTotal += credit.amount
                tempTotal += credit.amount
            }
            creditTotalsBeforePercentageAdjustment = tempTotal
            totalBeforePercentageAdjustment = subTotal
        }

    private suspend fun processDefaultExtraContainersByHour() =
        withContext(defaultScope) {
            if (defaultExtrasByHour.isNotEmpty()) {
                for (extraAndDef in defaultExtrasByHour) {
                    val value =
                        if (!extraAndDef.definition.weIsFixed && extraAndDef.definition.weValue >= 1.0)
                            extraAndDef.definition.weValue / 100.0
                        else extraAndDef.definition.weValue
                    val subTotal =
                        if (extraAndDef.extraType.wetIsDeleted ||
                            extraAndDef.definition.weIsDeleted
                        ) {
                            0.0
                        } else {
                            if (extraAndDef.definition.weIsFixed) {
                                extraAndDef.definition.weValue * getHoursWorked()
                            } else {
                                value * getPayTimeWorked()
                            }
                        }
                    val newExtraContainer = ExtraContainer(
                        extraAndDef.extraType.wetName,
                        subTotal,
                        extraAndDef,
                        null,
                        null
                    )
                    if (extraAndDef.extraType.wetIsCredit) {
                        creditExtraContainers.add(
                            newExtraContainer
                        )
                    } else {
                        debitExtraContainers.add(
                            newExtraContainer
                        )
                    }
                }
            }
        }

    private suspend fun processDefaultExtraContainersByDay() =
        withContext(defaultScope) {
            if (defaultExtrasByDay.isNotEmpty()) {
                for (extraAndDef in defaultExtrasByDay) {
                    val value =
                        if (!extraAndDef.definition.weIsFixed && extraAndDef.definition.weValue >= 1.0)
                            extraAndDef.definition.weValue / 100.0
                        else extraAndDef.definition.weValue
                    val subTotal =
                        if (extraAndDef.extraType.wetIsDeleted ||
                            extraAndDef.definition.weIsDeleted
                        ) {
                            0.0
                        } else {
                            if (extraAndDef.definition.weIsFixed) {
                                extraAndDef.definition.weValue * getDaysWorked()
                            } else {
                                value * getPayTimeWorked()
                            }
                        }
                    val newExtraContainer = ExtraContainer(
                        extraAndDef.extraType.wetName,
                        subTotal,
                        extraAndDef,
                        null,
                        null
                    )
                    if (extraAndDef.extraType.wetIsCredit) {
                        creditExtraContainers.add(
                            newExtraContainer
                        )
                    } else {
                        debitExtraContainers.add(
                            newExtraContainer
                        )
                    }
                }
            }
        }

    private suspend fun processDefaultExtraContainersByPay() =
        withContext(defaultScope) {
            if (defaultExtrasByPay.isNotEmpty()) {
                for (extraAndDef in defaultExtrasByPay) {
                    val value =
                        if (!extraAndDef.definition.weIsFixed && extraAndDef.definition.weValue >= 1.0)
                            extraAndDef.definition.weValue / 100.0
                        else extraAndDef.definition.weValue
                    val subTotal =
                        if (extraAndDef.extraType.wetIsDeleted ||
                            extraAndDef.definition.weIsDeleted
                        ) {
                            0.0
                        } else {
                            if (extraAndDef.definition.weIsFixed) {
                                extraAndDef.definition.weValue
                            } else {
                                value * getPayTimeWorked()
                            }
                        }
//                    Log.d(
//                        TAG,
//                        "----- Value of $subTotal for ${extraAndDef.extraType.wetName} --------"
//                    )
                    val newExtraContainer = ExtraContainer(
                        extraAndDef.extraType.wetName,
                        subTotal,
                        extraAndDef,
                        null,
                        null
                    )
                    if (extraAndDef.extraType.wetIsCredit) {
                        creditExtraContainers.add(
                            newExtraContainer
                        )
                    } else {
                        debitExtraContainers.add(
                            newExtraContainer
                        )
                    }
                }
            }
        }

    private suspend fun processCustomExtraContainersByDates() =
        withContext(defaultScope) {
            if (customWorkDateExtras.isNotEmpty()) {
                var subTotal = 0.0
                var workingExtra = customWorkDateExtras.first().wdeName
                for (i in customWorkDateExtras.indices) {
                    val currentExtra = customWorkDateExtras[i]
                    if (currentExtra.wdeName != workingExtra) {
                        val newExtraContainer = ExtraContainer(
                            workingExtra, subTotal,
                            null,
                            currentExtra,
                            null
                        )
                        if (customWorkDateExtras[i - 1].wdeIsCredit) {
                            creditExtraContainers.add(
                                newExtraContainer
                            )
                        } else {
                            debitExtraContainers.add(
                                newExtraContainer
                            )
                        }
                        workingExtra = currentExtra.wdeName
                        subTotal = 0.0
                    }
                    val value = if (!currentExtra.wdeIsFixed && currentExtra.wdeValue >= 1.0)
                        currentExtra.wdeValue / 100.0
                    else currentExtra.wdeValue
                    if (currentExtra.wdeAppliesTo == ExtraAppliesToFrequencies.HOURLY.value &&
                        currentExtra.wdeIsFixed
                    ) {
                        for (date in workDates) {
                            if (date.workDateId == currentExtra.wdeWorkDateId) {
                                subTotal += value *
                                        (date.wdRegHours + date.wdOtHours + date.wdDblOtHours)
                            }
                        }
                    } else if ((currentExtra.wdeAppliesTo == ExtraAppliesToFrequencies.HOURLY.value ||
                                currentExtra.wdeAppliesTo == ExtraAppliesToFrequencies.DAILY.value) &&
                        !currentExtra.wdeIsFixed
                    ) {
                        for (date in workDates) {
                            if (date.workDateId == currentExtra.wdeWorkDateId) {
                                subTotal += value * getPayRate() *
                                        (date.wdRegHours + date.wdOtHours * 1.5 + date.wdDblOtHours * 2.0)
                            }
                        }
                    } else if (currentExtra.wdeAppliesTo == ExtraAppliesToFrequencies.DAILY.value
                    ) {
                        subTotal += value
                    }
                    if (i == customWorkDateExtras.size - 1) {
                        val newExtraContainer = ExtraContainer(
                            workingExtra, subTotal,
                            null,
                            currentExtra,
                            null
                        )
                        if (currentExtra.wdeIsCredit) {
                            creditExtraContainers.add(
                                newExtraContainer
                            )
                        } else {
                            debitExtraContainers.add(
                                newExtraContainer
                            )
                        }
                    }
                }
            }
        }

    private suspend fun getDefaultWorkExtrasFromDbAndRemoveDuplicates() {
        coroutineScope {
            val workExtrasByPayDeferred = async { getDefaultExtraListFromDb() }
            val tempExtras = workExtrasByPayDeferred.await()
            for (extra in tempExtras) {
                var excludeAsDuplicate = false
                for (customExtra in customWorkDateExtras) {
                    if (extra.extraType.wetName == customExtra.wdeName) {
                        excludeAsDuplicate = true
                        break
                    }
                }
                for (customExtra in customExtrasByPay) {
                    if (extra.extraType.wetName == customExtra.ppeName) {
                        excludeAsDuplicate = true
                        break
                    }
                }
                if (!excludeAsDuplicate) {
                    when (extra.extraType.wetAppliesTo) {
                        ExtraAppliesToFrequencies.HOURLY.value -> {
                            defaultExtrasByHour.add(extra)
                        }

                        ExtraAppliesToFrequencies.DAILY.value -> {
                            defaultExtrasByDay.add(extra)
                        }

                        ExtraAppliesToFrequencies.PER_PAY_FOR_HOURLY_WAGES.value -> {
                            defaultExtrasByPay.add(extra)
                        }

                        ExtraAppliesToFrequencies.PER_PAY_PERCENTAGE_OF_ALL.value -> {
                            defaultExtrasByPercentageOfAll.add(extra)
                        }
                    }
                }
            }
        }
    }

    private suspend fun getCustomWorkDateExtrasFromDb() {
        coroutineScope {
            val workDateExtraFullDeferred = async { getExtraListForDatesFromDb() }
            customWorkDateExtras = workDateExtraFullDeferred.await()
        }
    }

    private suspend fun getWorkDatesFromDb() {
        coroutineScope {
            val workDatesDeferred = async { getWorkDatesList() }
            workDates = workDatesDeferred.await()
        }
    }

    private suspend fun getPayRateFromDb() {
        coroutineScope {
            val payRateDeferred = async { calculatePayRateFromDb() }
            payRate = payRateDeferred.await()
        }
    }

    private suspend fun getCustomExtrasByPayFromDb() {
        coroutineScope {
            val customExtrasDeferred = async { getCustomExtraListByPayFromDb() }
            customExtrasByPay = customExtrasDeferred.await()
        }
    }


    private suspend fun getExtraTypesFromDb() {
        coroutineScope {
            val extraTypesDeferred =
                async { getExtraTypes() }
            extraTypes = extraTypesDeferred.await()
        }
    }

    suspend fun calculatePayRateFromDb(): Double {
        var calculatedPayRate: Double
        withContext(defaultScope) {
            val rawPay = async {
                payCalculationsViewModel.getPayRate(
                    employer.employerId, currentPayPeriod.ppCutoffDate
                )
            }
            calculatedPayRate =
                fixRateByInterval(rawPay.await())
        }
        return calculatedPayRate
    }

    private fun fixRateByInterval(rate: EmployerPayRates): Double {
        when (rate.eprPerPeriod) {
            PayRateBasedOn.HOURLY.value -> return rate.eprPayRate

            PayRateBasedOn.DAILY.value -> return rate.eprPayRate / 8

            PayRateBasedOn.WEEKLY.value -> return rate.eprPayRate / 40

            PayRateBasedOn.BI_WEEKLY.value -> return rate.eprPayRate / 80

            PayRateBasedOn.MONTHLY.value -> return rate.eprPayRate / (365 / 12) / 8
        }
        return 0.0
    }

    private suspend fun getWorkDatesList(): List<WorkDates> =
        withContext(defaultScope) {
            payCalculationsViewModel.getWorkDateList(
                employer.employerId, currentPayPeriod.ppCutoffDate
            )
        }


    private suspend fun processAllHours() =
        withContext(Dispatchers.Default) {
            val daysWorkedDeferred = async {
                payDetailViewModel.getDaysWorked(
                    employer.employerId, currentPayPeriod.ppCutoffDate
                )
            }
            daysWorked = daysWorkedDeferred.await()
            val regHoursDeferred = async {
                payDetailViewModel.getHoursReg(
                    employer.employerId, currentPayPeriod.ppCutoffDate
                )
            }
            regHours = regHoursDeferred.await()
            val otHoursDeferred = async {
                payDetailViewModel.getHoursOt(
                    employer.employerId, currentPayPeriod.ppCutoffDate
                )
            }
            otHours = otHoursDeferred.await()
            val dblOtHoursDeferred = async {
                payDetailViewModel.getHoursDblOt(
                    employer.employerId, currentPayPeriod.ppCutoffDate
                )
            }
            dblOtHours = dblOtHoursDeferred.await()
            val statHoursDeferred = async {
                payDetailViewModel.getHoursStat(
                    employer.employerId, currentPayPeriod.ppCutoffDate
                )
            }
            statHours = statHoursDeferred.await()
        }


    private suspend fun getExtraListForDatesFromDb(): List<WorkDateExtras> =
        withContext(Dispatchers.Default) {
            payCalculationsViewModel.getWorkDateExtrasPerPay(
                employer.employerId, currentPayPeriod.ppCutoffDate
            )
        }

    private suspend fun getDefaultExtraListFromDb(): List<ExtraDefinitionAndType> =
        withContext(defaultScope) {
            payCalculationsViewModel.getDefaultExtraTypesAndCurrentDef(
                employer.employerId,
                currentPayPeriod.ppCutoffDate,
            )
        }

    private suspend fun getCustomExtraListByPayFromDb(): List<WorkPayPeriodExtras> =
        withContext(defaultScope) {
            payCalculationsViewModel.getCustomPayPeriodExtras(
                currentPayPeriod.payPeriodId
            )
        }

    private suspend fun getExtraTypes() =
        withContext(defaultScope) {
            payCalculationsViewModel.getExtraTypes(
                employer.employerId
            )
        }

    private suspend fun getTaxRatesFromDb() {
        coroutineScope {
            val taxTypesDeferred =
                async { getTaxTypes() }
            taxTypes = taxTypesDeferred.await()
            val effectiveDateDeferred =
                async { getCurrentEffectiveDate() }
            val taxRulesDeferred = async {
                getTaxRules(effectiveDateDeferred.await())
            }
            taxRules = taxRulesDeferred.await()
        }
    }

    private suspend fun getTaxTypes(): List<TaxTypes> =
        withContext(defaultScope) {
            payCalculationsViewModel
                .getTaxTypes(employer.employerId)
        }

    private suspend fun getCurrentEffectiveDate(): String =
        withContext(defaultScope) {
            payCalculationsViewModel
                .getCurrentEffectiveDate(currentPayPeriod.ppCutoffDate)
        }

    private suspend fun getTaxRules(effectiveDate: String) =
        withContext(defaultScope) {
            payCalculationsViewModel
                .getTaxRules(effectiveDate)
        }

    private fun getTotalAdjustedForTax(amount: Double): Double {
        if (taxFactor == 0.0) {
            when (employer.payFrequency) {
                "Bi-Weekly" -> {
                    taxFactor = 26.0
                }

                "Weekly" -> {
                    taxFactor = 52.0
                }
//
//                "Semi-Monthly" -> {
//                    taxFactor = amount / 24
//                }
//
//                "Monthly" -> {
//                    taxFactor = amount / 12
//                }

                else -> {
                    taxFactor = 0.0
                }
            }
        }
        return amount / taxFactor
    }

    override fun getDebitTotalsByPay(): Double {
        return debitTotalsByPay
    }

    override fun getCreditTotalAll(): Double {
        return creditTotalsBeforePercentageAdjustment + creditTotalsByPercentage
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
        return getPayAllHourly() +
                creditTotalsBeforePercentageAdjustment +
                creditTotalsByPercentage
    }

    override fun getPayTimeWorked(): Double {
        return getPayReg() + getPayOt() + getPayDblOt()
    }

    override fun getAllTaxDeductions(): Double {
        return taxDeductions
    }

    override fun getCredits(): List<ExtraContainer> {
        return creditExtraContainers
    }

    override fun getDebits(): List<ExtraContainer> {
        return debitExtraContainers
    }


    override fun getTaxList(): List<TaxAndAmount> {
        return taxAndAmountList
    }
}