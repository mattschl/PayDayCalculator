package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import ms.mattschlenkrich.paydaycalculator.common.NumberFunctions
import ms.mattschlenkrich.paydaycalculator.database.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.database.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paydaycalculator.database.model.extras.WorkExtraTypes
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.PayPeriodHourlySummary
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDateExtraAndTypeAndDef
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.database.model.payperiod.WorkPayPeriodExtras
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity
import kotlin.math.round

class CreditCalculationsInitial(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val currentPayPeriod: PayPeriods,
    private val workDateList: ArrayList<WorkDates>,
    private val hourlySummary: PayPeriodHourlySummary
) {
    private val creditList = getAllCreditsAndTotals()
    private val creditTotal = getTotalAllCredits()
    private val nf = NumberFunctions()

    fun getCreditList(): List<ExtraAndTotal> {
        return creditList
    }

    fun getCreditTotal(): Double {
        return creditTotal
    }

    private fun getTotalAllCredits(): Double {
        var total = 0.0
        for (credit in creditList) {
            total += credit.amount
        }
        return total
    }

    private fun getAllCreditsAndTotals(): List<ExtraAndTotal> {
        return getCreditExtrasByDates() +
                getRegularCreditsByPayDay() +
                getCustomCreditsByPayDay()
    }

    private fun getCreditExtrasByDates(): ArrayList<ExtraAndTotal> {
        val workDateExtraFinalList = ArrayList<ExtraAndTotal>()
        val workDateExtras = getWorkDateExtras()
        for (i in 0 until workDateExtras.size) {
            var total = 0.0
            if (workDateExtras[i].extra.wdeIsCredit) {
                if (workDateExtras[i].extra.wdeAppliesTo == 0 &&
                    workDateExtras[i].extra.wdeAttachTo == 1
                ) {
                    for (date in workDateList) {
                        if (!date.wdIsDeleted) {
                            val factor =
                                if (workDateExtras[i].def!!.weIsFixed) {
                                    workDateExtras[i].def!!.weValue
                                } else {
                                    workDateExtras[i].def!!.weValue / 100
                                }
                            total +=
                                nf.roundTo2Decimals(
                                    (date.wdRegHours
                                            + date.wdDblOtHours
                                            + date.wdDblOtHours)
                                            * factor
                                )
                        }
                    }
                } else if (workDateExtras[1].extra.wdeAppliesTo == 1 &&
                    workDateExtras[i].extra.wdeAttachTo == 1
                ) {
                    for (date in workDateList) {
                        if (!date.wdIsDeleted &&
                            (date.wdRegHours != 0.0 ||
                                    date.wdOtHours != 0.0 ||
                                    date.wdDblOtHours != 0.0)
                        ) {
                            total +=
                                nf.roundTo2Decimals(
                                    workDateExtras[i].def!!.weValue
                                )
                        }
                    }
                }
            }
        }
        return workDateExtraFinalList
    }

    private fun getWorkDateExtras(): ArrayList<WorkDateExtraAndTypeAndDef> {
        val payPeriodExtras = ArrayList<WorkDateExtraAndTypeAndDef>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateExtrasPerPay(
                currentPayPeriod.ppEmployerId,
                currentPayPeriod.ppCutoffDate
            ).observe(lifecycleOwner) { list ->
                payPeriodExtras.clear()
                list.listIterator().forEach {
                    payPeriodExtras.add(it)
                }
            }
        }
        return payPeriodExtras
    }

    private fun getCustomCreditsByPayDay(): ArrayList<ExtraAndTotal> {
        val extraList = ArrayList<ExtraAndTotal>()
        val payPeriodExtras = getCustomPayPeriodExtras()
        for (extra in payPeriodExtras) {
            if (extra.ppeIsCredit &&
                !extra.ppeIsDeleted
            ) {
                when (extra.ppeAppliesTo) {
                    0 -> {
                        val creditTotal =
                            if (extra.ppeIsFixed) {
                                round(
                                    (hourlySummary.hoursReg +
                                            hourlySummary.hoursOt +
                                            hourlySummary.hoursDblOt) *
                                            extra.ppeValue
                                ) / 100
                            } else {
                                round(
                                    (hourlySummary.hoursReg +
                                            hourlySummary.hoursOt +
                                            hourlySummary.hoursDblOt) *
                                            extra.ppeValue
                                ) / 100
                            }
                        extraList.add(
                            ExtraAndTotal(extra.ppeName, creditTotal)
                        )
                    }

                    1 -> {
                        val creditTotal =
                            if (extra.ppeIsFixed) {
                                hourlySummary.daysWorked *
                                        extra.ppeValue
                            } else {
                                round(
                                    hourlySummary.payRate *
                                            (hourlySummary.hoursReg +
                                                    hourlySummary.hoursOt +
                                                    hourlySummary.hoursDblOt) *
                                            extra.ppeValue
                                ) / 100
                            }
                        extraList.add(
                            ExtraAndTotal(extra.ppeName, creditTotal)
                        )
                    }

                    3 -> {
                        val creditTotal =
                            if (extra.ppeIsFixed) {
                                extra.ppeValue
                            } else {
                                round(
                                    hourlySummary.payRate *
                                            (hourlySummary.hoursReg +
                                                    hourlySummary.hoursOt +
                                                    hourlySummary.hoursDblOt +
                                                    hourlySummary.hoursStat) *
                                            extra.ppeValue
                                ) / 100
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

    private fun getRegularCreditsByPayDay(): ArrayList<ExtraAndTotal> {
        val extraList = ArrayList<ExtraAndTotal>()
        val workExtrasByPay = getRegularExtrasPerPayPeriod()
        val extraTypes = getExtraTypes()
        for (i in 0 until workExtrasByPay.size) {
            var notFound = true
            for (extraType in extraTypes) {
                if (workExtrasByPay[i].extraType.wetName ==
                    extraType.wetName
                ) {
                    notFound = false
                }
            }
            if (notFound) {
                if (workExtrasByPay[i].extraType.wetIsCredit &&
                    workExtrasByPay[i].extraType.wetIsDefault &&
                    workExtrasByPay[i].extraType.wetAppliesTo == 3
                ) {
                    if (workExtrasByPay[i].definition.weIsFixed) {
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
                                round(
                                    workExtrasByPay[i].definition.weValue *
                                            hourlySummary.payRate *
                                            (hourlySummary.hoursReg +
                                                    hourlySummary.hoursOt +
                                                    hourlySummary.hoursDblOt)
                                ) / 100

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
                            round(
                                workExtrasByPay[i].definition.weValue *
                                        hourlySummary.payRate *
                                        (hourlySummary.hoursReg +
                                                hourlySummary.hoursOt +
                                                hourlySummary.hoursDblOt)
                            ) /
                                    100
                        )
                    )
                }

            }
        }
        return extraList
    }

    private fun getCustomPayPeriodExtras(): ArrayList<WorkPayPeriodExtras> {
        val customPayPeriodExtras = ArrayList<WorkPayPeriodExtras>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getPayPeriodExtras(
                currentPayPeriod.payPeriodId
            ).observe(lifecycleOwner) { list ->
                list.listIterator().forEach {
                    customPayPeriodExtras.add(it)
                }
            }
        }
        return customPayPeriodExtras
    }

    private fun getRegularExtrasPerPayPeriod(): ArrayList<ExtraDefinitionAndType> {
        val workExtrasByPay = ArrayList<ExtraDefinitionAndType>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workExtraViewModel.getExtraTypesAndDef(
                currentPayPeriod.ppEmployerId,
                currentPayPeriod.ppCutoffDate,
                3
            ).observe(lifecycleOwner) { list ->
                workExtrasByPay.clear()
                list.listIterator().forEach {
                    workExtrasByPay.add(it)
                }
            }
        }
        return workExtrasByPay
    }

    private fun getExtraTypes(): ArrayList<WorkExtraTypes> {
        val extraTypes = ArrayList<WorkExtraTypes>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workExtraViewModel.getWorkExtraTypeList(
                currentPayPeriod.ppEmployerId
            ).observe(
                lifecycleOwner
            ) { list ->
                extraTypes.clear()
                list.listIterator().forEach {
                    extraTypes.add(it)
                }
            }
        }
        return extraTypes
    }
}