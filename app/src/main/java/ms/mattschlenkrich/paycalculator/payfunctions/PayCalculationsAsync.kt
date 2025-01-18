package ms.mattschlenkrich.paycalculator.payfunctions

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.database.model.employer.EmployerPayRates
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraAndTotal
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
    private lateinit var defaultExtrasByPay: List<ExtraDefinitionAndType>
    private lateinit var workExtrasByPercentage: List<ExtraDefinitionAndType>
    private lateinit var extraTypes: List<WorkExtraTypes>
    private lateinit var taxRules: List<WorkTaxRules>
    private lateinit var taxTypes: List<TaxTypes>
    private var taxAndAmountList = ArrayList<TaxAndAmount>()
    private var debitExtraAndTotalByPay = ArrayList<ExtraAndTotal>()
    private var creditExtraAndTotalByDate = ArrayList<ExtraAndTotal>()
    private var creditExtraAndTotalByPay = ArrayList<ExtraAndTotal>()
    private var creditExtraAndTotalByPercentage = ArrayList<ExtraAndTotal>()
    private var debitExtraAndTotalsByPercentage = ArrayList<ExtraAndTotal>()
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
    private var totalBeforePercentageAdjustment = 0.0
//    private var df = DateFunctions()/

    init {
        CoroutineScope(defaultScope).launch {
            processListsFromDb()
            processPayTotals()
            var credits = 0.0
            for (credit in creditExtraAndTotalByDate) {
                credits += credit.amount
                Log.d(TAG, "credit is ${credit.amount} for ${credit.extraName}")
            }
            for (credit in creditExtraAndTotalByPay) {
                credits += credit.amount
                Log.d(TAG, "credit is ${credit.amount} for ${credit.extraName}")
            }
            Log.d(TAG, "Total hourly is ${getPayAllHourly()}")
            Log.d(TAG, "Total before percentage adjustment is $totalBeforePercentageAdjustment")
            for (credit in creditExtraAndTotalByPercentage) {
                credits += credit.amount
                Log.d(TAG, "Credit is ${credit.amount} for ${credit.extraName}")
            }
            Log.d(
                TAG, "Total credits is $credits + Total Hourly is ${getPayAllHourly()} for a " +
                        "total gross pay of ${credits + getPayAllHourly()}"
            )
            for (debit in debitExtraAndTotalByPay) {
                Log.d(TAG, "debit is ${debit.amount} for ${debit.extraName}")
            }
            Log.d(TAG, "***************************************")
            //TODO: Find out how to use live data to signal the end
        }
    }

    private suspend fun processListsFromDb() {
        processPayRate()
        processWorkDates()
        processAllHours()
        processWorkDateExtras()
        processDefaultWorkExtrasByPay()
        processCustomExtrasByPay()
        processExtrasPercentageOfAll()
        processExtraTypes()
        processTaxRates()
    }

    private suspend fun processPayTotals() {
        calculateExtrasByDates()
        calculateDefaultExtrasAndTotalsByPay()
        calculateCustomExtrasAndTotalsByPay()
        calculateTotalsBeforePercentageAdjustment()
        calculateExtraByPercentageOfAll()
        calculateTotalPercentageOfAll()
        calculateTaxAndAmounts()
        calculateTaxTotal()
    }

    private suspend fun calculateCustomExtrasAndTotalsByPay() =
        withContext(defaultScope) {
            for (extra in customExtrasByPay) {
                val total: Double = if (extra.ppeIsFixed) {
                    extra.ppeValue
                } else {
                    when (extra.ppeAppliesTo) {
                        0 -> getPayTimeWorked() * extra.ppeValue / 100
                        1 -> getPayTimeWorked() * extra.ppeValue / 100
                        3 -> getPayAllHourly() * extra.ppeValue / 100
                        else -> extra.ppeValue
                    }
                }
                if (extra.ppeIsCredit) {
                    creditExtraAndTotalByPay.add(
                        ExtraAndTotal(
                            extra.ppeName, total
                        )
                    )
                } else {
                    debitExtraAndTotalByPay.add(
                        ExtraAndTotal(
                            extra.ppeName, total
                        )
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
                        0 -> getPayTimeWorked()
                        1 -> getPayAllHourly()
                        2 -> getPayGross()
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
            if (creditExtraAndTotalByPercentage.isNotEmpty()) {
                var subTotal = 0.0
                for (credit in creditExtraAndTotalByPercentage) {
                    subTotal += credit.amount
                }
                creditTotalsByPercentage = subTotal
            }
        }

    private suspend fun calculateExtraByPercentageOfAll() =
        withContext(defaultScope) {
            if (workExtrasByPercentage.isNotEmpty()) {
                var subTotal = 0.0
                var workingExtra = workExtrasByPercentage.first().extraType.wetName
                for (i in workExtrasByPercentage.indices) {
                    val currentExtra = workExtrasByPercentage[i]
                    if (currentExtra.extraType.wetName != workingExtra) {
                        if (workExtrasByPercentage[i - 1].extraType.wetIsCredit) {
                            creditExtraAndTotalByPay.add(
                                ExtraAndTotal(
                                    workingExtra, subTotal
                                )
                            )
                        } else {
                            debitExtraAndTotalByPay.add(
                                ExtraAndTotal(
                                    workingExtra, subTotal
                                )
                            )
                        }
                        workingExtra = currentExtra.extraType.wetName
                        subTotal = 0.0
                    }
                    subTotal += totalBeforePercentageAdjustment *
                            currentExtra.definition.weValue / 100
                    if (i == workExtrasByPercentage.size - 1) {
                        if (currentExtra.extraType.wetIsCredit) {
                            creditExtraAndTotalByPercentage.add(
                                ExtraAndTotal(
                                    workingExtra, subTotal
                                )
                            )
                        } else {
                            debitExtraAndTotalsByPercentage.add(
                                ExtraAndTotal(
                                    workingExtra, subTotal
                                )
                            )
                        }
                    }
                }
            }
        }

    private suspend fun calculateTotalsBeforePercentageAdjustment() =
        withContext(defaultScope) {
            var subTotal = getPayAllHourly()
            var tempTotal = 0.0
            for (credit in creditExtraAndTotalByDate) {
                subTotal += credit.amount
            }
            creditTotalByDate = tempTotal
            tempTotal = 0.0
            for (credit in creditExtraAndTotalByPay) {
                subTotal += credit.amount
            }
            creditTotalsByPay = tempTotal
            totalBeforePercentageAdjustment = subTotal
        }

    private suspend fun calculateDefaultExtrasAndTotalsByPay() =
        withContext(defaultScope) {
            if (defaultExtrasByPay.isNotEmpty()) {
                var subTotal = 0.0
                var workingExtra = defaultExtrasByPay.first().extraType.wetName
                for (i in defaultExtrasByPay.indices) {
                    val currentExtra = defaultExtrasByPay[i]
                    if (currentExtra.extraType.wetName != workingExtra) {
                        if (defaultExtrasByPay[i - 1].extraType.wetIsCredit) {
                            creditExtraAndTotalByPay.add(
                                ExtraAndTotal(
                                    workingExtra, subTotal
                                )
                            )
                        } else {
                            debitExtraAndTotalByPay.add(
                                ExtraAndTotal(
                                    workingExtra, subTotal
                                )
                            )
                        }
                        workingExtra = currentExtra.extraType.wetName
                        subTotal = 0.0
                    }
                    if (currentExtra.extraType.wetAppliesTo == 0 &&
                        currentExtra.definition.weIsFixed
                    ) {
                        subTotal += getHoursWorked() * currentExtra.definition.weValue
                    } else if (currentExtra.extraType.wetAppliesTo == 1 &&
                        currentExtra.definition.weIsFixed
                    ) {
                        subTotal += daysWorked * currentExtra.definition.weValue
                    } else if (currentExtra.extraType.wetAppliesTo == 3 &&
                        currentExtra.definition.weIsFixed
                    ) {
                        subTotal += currentExtra.definition.weValue
                    } else if (!currentExtra.definition.weIsFixed) {
                        subTotal += currentExtra.definition.weValue *
                                (getHoursAll()) * payRate / 100
                    }
                    if (i == defaultExtrasByPay.size - 1) {
                        if (currentExtra.extraType.wetIsCredit) {
                            creditExtraAndTotalByPay.add(
                                ExtraAndTotal(
                                    workingExtra, subTotal
                                )
                            )
                        } else {
                            debitExtraAndTotalByPay.add(
                                ExtraAndTotal(
                                    workingExtra, subTotal
                                )
                            )
                        }
                    }
                }
            }
        }

    private suspend fun calculateExtrasByDates() =
        withContext(defaultScope) {
            if (customWorkDateExtras.isNotEmpty()) {
                var subTotal = 0.0
                var workingExtra = customWorkDateExtras.first().wdeName
                for (i in customWorkDateExtras.indices) {
                    val currentExtra = customWorkDateExtras[i]
                    if (currentExtra.wdeName != workingExtra) {
                        if (customWorkDateExtras[i - 1].wdeIsCredit) {
                            creditExtraAndTotalByDate.add(
                                ExtraAndTotal(
                                    workingExtra, subTotal
                                )
                            )
                        } else {
                            debitExtraAndTotalByPay.add(
                                ExtraAndTotal(
                                    workingExtra, subTotal
                                )
                            )
                        }
                        workingExtra = currentExtra.wdeName
                        subTotal = 0.0
                    }
                    if (currentExtra.wdeAppliesTo == 0 &&
                        currentExtra.wdeIsFixed
                    ) {
                        for (date in workDates) {
                            if (date.workDateId == currentExtra.wdeWorkDateId) {
                                subTotal += currentExtra.wdeValue *
                                        (date.wdRegHours + date.wdOtHours + date.wdDblOtHours)
                            }
                        }
                    } else if ((currentExtra.wdeAppliesTo == 0 ||
                                currentExtra.wdeAppliesTo == 1) &&
                        !currentExtra.wdeIsFixed
                    ) {
                        for (date in workDates) {
                            if (date.workDateId == currentExtra.wdeWorkDateId) {
                                subTotal += currentExtra.wdeValue * getPayRate() *
                                        (date.wdRegHours + date.wdOtHours + date.wdDblOtHours)
                            }
                        }
                    } else if (currentExtra.wdeAppliesTo == 1
                    ) {
                        subTotal += currentExtra.wdeValue
                    }
                    if (i == customWorkDateExtras.size - 1) {
                        if (currentExtra.wdeIsCredit) {
                            creditExtraAndTotalByDate.add(
                                ExtraAndTotal(
                                    workingExtra, subTotal
                                )
                            )
                        } else {
                            debitExtraAndTotalByPay.add(
                                ExtraAndTotal(
                                    workingExtra, subTotal
                                )
                            )
                        }
                    }
                }
            }
        }

    private suspend fun processDefaultWorkExtrasByPay() {
        coroutineScope {
            val workExtrasByPayDeferred = async { getPerPayExtrasList() }
            defaultExtrasByPay = workExtrasByPayDeferred.await()
        }
    }

    private suspend fun processWorkDateExtras() {
        coroutineScope {
            val workDateExtraFullDeferred = async { getExtraListForDates() }
            customWorkDateExtras = workDateExtraFullDeferred.await()
        }
    }

    private suspend fun processWorkDates() {
        coroutineScope {
            val workDatesDeferred = async { getWorkDatesList() }
            workDates = workDatesDeferred.await()
        }
    }

    private suspend fun processPayRate() {
        coroutineScope {
            val payRateDeferred = async { calculatePayRateFromDb() }
            payRate = payRateDeferred.await()
        }
    }

    private suspend fun processCustomExtrasByPay() {
        coroutineScope {
            val customExtrasByPay = async { getCustomExtraByPay() }
            this@PayCalculationsAsync.customExtrasByPay = customExtrasByPay.await()
        }
    }

    private suspend fun processExtrasPercentageOfAll() {
        coroutineScope {
            val extraByPercentageOfAllDeferred =
                async { getExtraPercentageOfAll() }
            workExtrasByPercentage = extraByPercentageOfAllDeferred.await()
        }
    }

    private suspend fun processExtraTypes() {
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


    private suspend fun getExtraListForDates(): List<WorkDateExtras> =
        withContext(Dispatchers.Default) {
            mainActivity.payCalculationsViewModel.getWorkDateExtrasPerPay(
                employer.employerId, currentPayPeriod.ppCutoffDate
            )
        }

    private suspend fun getPerPayExtrasList(): List<ExtraDefinitionAndType> =
        withContext(defaultScope) {
            mainActivity.payCalculationsViewModel.getDefaultExtraTypesAndCurrentDef(
                employer.employerId, currentPayPeriod.ppCutoffDate, 3
            )
        }

    private suspend fun getCustomExtraByPay(): List<WorkPayPeriodExtras> =
        withContext(defaultScope) {
            mainActivity.payCalculationsViewModel.getCustomPayPeriodExtras(
                currentPayPeriod.payPeriodId
            )
        }

    private suspend fun getExtraPercentageOfAll() =
        withContext(defaultScope) {
            mainActivity.payCalculationsViewModel.getDefaultExtraTypesAndCurrentDef(
                employer.employerId, currentPayPeriod.ppCutoffDate, 4
            )
        }

    private suspend fun getExtraTypes() =
        withContext(defaultScope) {
            mainActivity.payCalculationsViewModel.getExtraTypes(
                employer.employerId
            )
        }

    private suspend fun processTaxRates() {
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

    override fun getDebitExtrasListByPay(): List<ExtraAndTotal> {
        return debitExtraAndTotalByPay
    }

    override fun getCreditExtrasListByDate(): List<ExtraAndTotal> {
        return creditExtraAndTotalByDate
    }

    override fun getCreditExtrasListByPay(): List<ExtraAndTotal> {
        return creditExtraAndTotalByPay
    }

    override fun getCreditExtrasListByPercentageOfAll(): List<ExtraAndTotal> {
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
        return getPayAllHourly() + creditTotalByDate +
                creditTotalsByPay + creditTotalsByPercentage
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

    override fun getTaxList(): List<TaxAndAmount> {
        return taxAndAmountList.toList()
    }
}