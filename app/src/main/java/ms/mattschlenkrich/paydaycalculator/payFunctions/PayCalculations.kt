package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.util.Log
import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.ExtraAndTotal
import ms.mattschlenkrich.paydaycalculator.model.ExtraDefinitionAndType
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtraAndTypeFull
import ms.mattschlenkrich.paydaycalculator.model.WorkDates
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraTypes

private const val TAG = "PayCalculations"

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
    var rate = 0.0
    val hours = Hours()
    val pay = Pay()
    val extras = Extras()
    val deductions = Deductions()

    init {
        findWorkDates()
        findExtrasPerDay()
        findExtrasPerPay()
        findRate()
        findExtraTypes()
    }

    inner class Deductions {
        fun getDebitExtraAndTotalByPay(): ArrayList<ExtraAndTotal> {
            val debitLiat = ArrayList<ExtraAndTotal>()
            for (i in 0 until workExtrasByPay.size) {
                if (!workExtrasByPay[i].extraType.wetIsCredit &&
                    workExtrasByPay[i].extraType.wetIsDefault &&
                    workExtrasByPay[i].extraType.wetAppliesTo == 3
                ) {
                    if (workExtrasByPay[i].definition.weIsFixed
                    ) {
                        debitLiat.add(
                            ExtraAndTotal(
                                workExtrasByPay[i].extraType.wetName,
                                workExtrasByPay[i].definition.weValue
                            )
                        )
                    } else {
                        debitLiat.add(
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
                    debitLiat.add(
                        ExtraAndTotal(
                            workExtrasByPay[i].extraType.wetName,
                            workExtrasByPay[i].definition.weValue *
                                    pay.getPayTimeWorked() / 100
                        )
                    )
                }
            }
            return debitLiat
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
        fun getHoursAll(): Double {
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

        fun getPayGross(): Double {
            return getPayHourly() + getCreditTotalByDate() +
                    getCreditTotalsByPay()
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
            return total
        }

        fun getCreditTotalsByPay(): Double {
            var total = 0.0
            for (extra in extras.getCreditExtrasAndTotalsByPay()) {
                Log.d(TAG, "extra is ${extra.extraName} and amount is ${extra.amount}")
                total += extra.amount
            }
            return total
        }

        fun getDebitTotalsByPay(): Double {
            var total = 0.0
            for (extra in deductions.getDebitExtraAndTotalByPay()) {
                Log.d(TAG, "extra is ${extra.extraName} and amount is ${extra.amount}")
                total += extra.amount
            }
            return total
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
}