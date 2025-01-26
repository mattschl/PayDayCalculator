package ms.mattschlenkrich.paycalculator.payfunctions

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.common.AppliesToFrequencies
import ms.mattschlenkrich.paycalculator.common.AttachToFrequencies
import ms.mattschlenkrich.paycalculator.common.TaxBasedOn
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerPayRates
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraContainer
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtras
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
//    private var df = DateFunctions()/

    init {
        CoroutineScope(defaultScope).launch {
            getListsFromDb()
            processExtraContainers()
            processPayTotals()
            var credits = 0.0
            for (credit in getCredits()) {
                credits += credit.amount
                Log.d(TAG, "credit is ${credit.amount} for ${credit.extraName}")
            }
            Log.d(
                TAG, "Total credits is $credits + Total Hourly is ${getPayAllHourly()} for a " +
                        "total gross pay of ${credits + getPayAllHourly()}"
            )
            var debits = 0.0
            for (debit in debitExtraContainers) {
                Log.d(TAG, "debit is ${debit.amount} for ${debit.extraName}")
                debits += debit.amount
            }
            Log.d(TAG, "Total debits is $debits")
            var taxes = 0.0
            for (tax in taxAndAmountList) {
                Log.d(TAG, "Tas of ${tax.amount} for ${tax.taxType}")
                taxes += tax.amount
            }
            Log.d(TAG, "NET pay is ${credits + getPayAllHourly() - debits - taxes}")
            Log.d(TAG, "***************************************")
            //TODO: Find out how to use live data to signal the end
        }
    }

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
    }

    private suspend fun processPayTotals() {
        calculateTaxAndAmounts()
        calculateTaxTotal()
    }


    private suspend fun processCustomExtraContainersByPay() =
        withContext(defaultScope) {
            for (extra in customExtrasByPay) {
                val total: Double = if (extra.ppeIsDeleted) {
                    0.0
                } else if (extra.ppeIsFixed) {
                    when (extra.ppeAppliesTo) {
                        AttachToFrequencies.Hourly.value -> getHoursWorked() * extra.ppeValue
                        AttachToFrequencies.Daily.value -> getDaysWorked() * extra.ppeValue
                        AttachToFrequencies.PerPay.value -> extra.ppeValue
                        else -> extra.ppeValue
                    }
                } else {
                    when (extra.ppeAppliesTo) {
                        AttachToFrequencies.Hourly.value -> getPayTimeWorked() * extra.ppeValue
                        AttachToFrequencies.Daily.value -> getPayTimeWorked() * extra.ppeValue
                        AttachToFrequencies.PerPay.value -> getPayAllHourly() * extra.ppeValue
                        else -> extra.ppeValue
                    }
                }
                Log.d(TAG, "-------- value of $total for ${extra.ppeName} ---------")
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

    private suspend fun calculateTaxAndAmounts() =
        withContext(defaultScope) {
            for (type in taxTypes) {
                var taxTotal = 0.0
                var runningRemainder =
                    when (type.ttBasedOn) {
                        TaxBasedOn.TimeWorkedOnly.value -> getPayTimeWorked()
                        TaxBasedOn.TimeWorkedAndStat.value -> getPayAllHourly()
                        TaxBasedOn.TimeWorkedStatsAndExtras.value -> getPayGross()
                        else -> 0.0
                    }
                for (rule in taxRules) {
                    if (rule.wtType == type.taxType &&
                        runningRemainder > 0.0
                    ) {
                        var taxable: Double
                        runningRemainder -=
                            if (rule.wtHasExemption) {
                                getTaxFactor(rule.wtExemptionAmount)
                            } else {
                                0.0
                            }
                        if (runningRemainder < 0.0) {
                            runningRemainder = 0.0
                        }
                        if (rule.wtHasBracket &&
                            runningRemainder >= getTaxFactor(rule.wtBracketAmount)
                        ) {
                            taxable = getTaxFactor(rule.wtBracketAmount)
                            runningRemainder -= getTaxFactor(rule.wtBracketAmount)
                        } else {
                            taxable = runningRemainder
                            runningRemainder = 0.0
                        }
                        taxTotal += taxable * rule.wtPercent
                    }
                }
                taxAndAmountList.add(
                    TaxAndAmount(
                        type.taxType, taxTotal
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
                    val subTotal =
                        if (extraAndDef.extraType.wetIsDeleted ||
                            extraAndDef.definition.weIsDeleted
                        ) {
                            0.0
                        } else {
                            extraAndDef.definition.weValue * totalBeforePercentageAdjustment
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
                    val subTotal =
                        if (extraAndDef.extraType.wetIsDeleted ||
                            extraAndDef.definition.weIsDeleted
                        ) {
                            0.0
                        } else {
                            if (extraAndDef.definition.weIsFixed) {
                                extraAndDef.definition.weValue * getHoursWorked()
                            } else {
                                extraAndDef.definition.weValue * getPayTimeWorked()
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
                    val subTotal =
                        if (extraAndDef.extraType.wetIsDeleted ||
                            extraAndDef.definition.weIsDeleted
                        ) {
                            0.0
                        } else {
                            if (extraAndDef.definition.weIsFixed) {
                                extraAndDef.definition.weValue * getDaysWorked()
                            } else {
                                extraAndDef.definition.weValue * getPayTimeWorked()
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
                    val subTotal =
                        if (extraAndDef.extraType.wetIsDeleted ||
                            extraAndDef.definition.weIsDeleted
                        ) {
                            0.0
                        } else {
                            if (extraAndDef.definition.weIsFixed) {
                                extraAndDef.definition.weValue
                            } else {
                                extraAndDef.definition.weValue * getPayTimeWorked() / 100
                            }
                        }
                    Log.d(
                        TAG,
                        "----- Value of $subTotal for ${extraAndDef.extraType.wetName} --------"
                    )
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
                    if (currentExtra.wdeAppliesTo == AppliesToFrequencies.Hourly.value &&
                        currentExtra.wdeIsFixed
                    ) {
                        for (date in workDates) {
                            if (date.workDateId == currentExtra.wdeWorkDateId) {
                                subTotal += currentExtra.wdeValue *
                                        (date.wdRegHours + date.wdOtHours + date.wdDblOtHours)
                            }
                        }
                    } else if ((currentExtra.wdeAppliesTo == AppliesToFrequencies.Hourly.value ||
                                currentExtra.wdeAppliesTo == AppliesToFrequencies.Daily.value) &&
                        !currentExtra.wdeIsFixed
                    ) {
                        for (date in workDates) {
                            if (date.workDateId == currentExtra.wdeWorkDateId) {
                                subTotal += currentExtra.wdeValue * getPayRate() *
                                        (date.wdRegHours + date.wdOtHours + date.wdDblOtHours)
                            }
                        }
                    } else if (currentExtra.wdeAppliesTo == AppliesToFrequencies.Daily.value
                    ) {
                        subTotal += currentExtra.wdeValue
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
                        AppliesToFrequencies.Hourly.value -> {
                            defaultExtrasByHour.add(extra)
                        }

                        AppliesToFrequencies.Daily.value -> {
                            defaultExtrasByDay.add(extra)
                        }

                        AppliesToFrequencies.PerPayForHourlyWages.value -> {
                            defaultExtrasByPay.add(extra)
                        }

                        AppliesToFrequencies.PerPayPercentageOfAll.value -> {
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
            val customExtras = async { getCustomExtraByPayFromDb() }
            customExtrasByPay = customExtras.await()
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
                mainActivity.payCalculationsViewModel.getPayRate(
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
            AttachToFrequencies.Hourly.value -> return rate.eprPayRate

            AttachToFrequencies.Daily.value -> return rate.eprPayRate / 8

            AttachToFrequencies.Weekly.value -> return rate.eprPayRate / 40
        }
        return 0.0
    }

    private suspend fun getWorkDatesList(): List<WorkDates> =
        withContext(defaultScope) {
            mainActivity.payCalculationsViewModel.getWorkDateList(
                employer.employerId, currentPayPeriod.ppCutoffDate
            )
        }


    private suspend fun processAllHours() =
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
        }


    private suspend fun getExtraListForDatesFromDb(): List<WorkDateExtras> =
        withContext(Dispatchers.Default) {
            mainActivity.payCalculationsViewModel.getWorkDateExtrasPerPay(
                employer.employerId, currentPayPeriod.ppCutoffDate
            )
        }

    private suspend fun getDefaultExtraListFromDb(): List<ExtraDefinitionAndType> =
        withContext(defaultScope) {
            mainActivity.payCalculationsViewModel.getDefaultExtraTypesAndCurrentDef(
                employer.employerId,
                currentPayPeriod.ppCutoffDate,
            )
        }

    private suspend fun getCustomExtraByPayFromDb(): List<WorkPayPeriodExtras> =
        withContext(defaultScope) {
            mainActivity.payCalculationsViewModel.getCustomPayPeriodExtras(
                currentPayPeriod.payPeriodId
            )
        }

    private suspend fun getExtraTypes() =
        withContext(defaultScope) {
            mainActivity.payCalculationsViewModel.getExtraTypes(
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
            mainActivity.payCalculationsViewModel
                .getTaxTypes(employer.employerId)
        }

    private suspend fun getCurrentEffectiveDate(): String =
        withContext(defaultScope) {
            mainActivity.payCalculationsViewModel
                .getCurrentEffectiveDate(currentPayPeriod.ppCutoffDate)
        }

    private suspend fun getTaxRules(effectiveDate: String) =
        withContext(defaultScope) {
            mainActivity.payCalculationsViewModel
                .getTaxRules(effectiveDate)
        }

    private fun getTaxFactor(amount: Double): Double {
        if (taxFactor == 0.0) {
            when (employer.payFrequency) {
                "Bi-Weekly" -> {
                    taxFactor = amount / 26
                }

                "Weekly" -> {
                    taxFactor = amount / 52
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
        return taxFactor
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