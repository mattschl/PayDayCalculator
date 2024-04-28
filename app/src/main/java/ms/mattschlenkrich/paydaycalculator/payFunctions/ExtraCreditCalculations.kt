package ms.mattschlenkrich.paydaycalculator.payFunctions

import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDateExtraAndTypeFull
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates

class ExtraCreditCalculations(
    private val hourlyCalculations: HourlyCalculations,
    private val payRate: Double,
    private val workDates: ArrayList<WorkDates>,
    private val workDateExtrasFull: ArrayList<WorkDateExtraAndTypeFull>,
    private val workExtrasByPay: ArrayList<ExtraDefinitionAndType>,
) {
    private var creditExtraAndTotalsByDate: ArrayList<ExtraAndTotal>
    private var creditExtrasAndTotalsByPay: ArrayList<ExtraAndTotal>

    init {
        creditExtraAndTotalsByDate = findCreditExtraAndTotalsByDate()
        creditExtrasAndTotalsByPay = findCreditExtrasAndTotalsByPay()
    }

    fun getCreditExtraAndTotalsByDate(): ArrayList<ExtraAndTotal> {
        return creditExtraAndTotalsByDate
    }

    fun getCreditExtrasAndTotalsByPay(): ArrayList<ExtraAndTotal> {
        return creditExtrasAndTotalsByPay
    }

    private fun findCreditExtraAndTotalsByDate(): ArrayList<ExtraAndTotal> {
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
                            total += workDateExtrasFull[i].extra.wdeValue * payRate * (
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
                            total += workDateExtrasFull[i].extra.wdeValue * payRate * (
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

    private fun findCreditExtrasAndTotalsByPay(): ArrayList<ExtraAndTotal> {
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
                                    hourlyCalculations.getHoursWorked() * payRate / 100
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
                                hourlyCalculations.getHoursWorked() * payRate / 100

                    )
                )
            }
        }
        return extraList
    }
}