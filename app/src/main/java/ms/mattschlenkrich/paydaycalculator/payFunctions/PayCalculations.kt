package ms.mattschlenkrich.paydaycalculator.payFunctions

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
    private val workExtrasPerDate = ArrayList<WorkDateExtraAndTypeFull>()
    private val workExtrasByPay = ArrayList<ExtraDefinitionAndType>()
    private val extraTypes = ArrayList<WorkExtraTypes>()
    var rate = 0.0
    val hours = Hours()
    val pay = Pay()
    val extras = Extras()

    init {
        findWorkDates()
        findExtrasPerDay()
        findExtrasPerPay()
        findRate()
        findExtraTypes()
    }

    inner class Extras {

        fun getCreditExtraAndTotalsByDate(): ArrayList<ExtraAndTotal> {
            val extraList = ArrayList<ExtraAndTotal>()
            var total = 0.0
            for (i in 0 until workExtrasPerDate.size) {
                if (workExtrasPerDate[i].extra.wdeIsCredit) {
                    if (workExtrasPerDate[i].extra.wdeAppliesTo == 0 &&
                        workExtrasPerDate[i].extra.wdeIsFixed
                    ) {
                        for (date in workDates) {
                            if (date.workDateId == workExtrasPerDate[i].extra.wdeWorkDateId) {
                                total += workExtrasPerDate[i].extra.wdeValue * (
                                        date.wdRegHours + date.wdOtHours + date.wdDblOtHours
                                        )
                            }
                        }
                    } else if (workExtrasPerDate[i].extra.wdeAppliesTo == 0 &&
                        !workExtrasPerDate[i].extra.wdeIsFixed
                    ) {
                        for (date in workDates) {
                            if (date.workDateId == workExtrasPerDate[i].extra.wdeWorkDateId) {
                                total += workExtrasPerDate[i].extra.wdeValue * rate * (
                                        date.wdRegHours + date.wdOtHours + date.wdDblOtHours
                                        )
                            }
                        }
                    } else if (workExtrasPerDate[i].extra.wdeAppliesTo == 1 &&
                        workExtrasPerDate[i].extra.wdeIsFixed
                    ) {
                        for (date in workDates) {
                            if (date.workDateId == workExtrasPerDate[i].extra.wdeWorkDateId) {
                                total += workExtrasPerDate[i].extra.wdeValue
                            }
                        }
                    } else if (workExtrasPerDate[i].extra.wdeAppliesTo == 1 &&
                        !workExtrasPerDate[i].extra.wdeIsFixed
                    ) {
                        for (date in workDates) {
                            if (date.workDateId == workExtrasPerDate[i].extra.wdeWorkDateId) {
                                total += workExtrasPerDate[i].extra.wdeValue * rate * (
                                        date.wdRegHours + date.wdOtHours + date.wdDblOtHours
                                        )
                            }
                        }
                    }
                }
                if (workExtrasPerDate.size == 1) {
                    extraList.add(ExtraAndTotal(workExtrasPerDate[i].extra.wdeName, total))
                    total = 0.0
                } else if (i < workExtrasPerDate.size - 1 &&
                    (workExtrasPerDate[i].extra.wdeName != workExtrasPerDate[i + 1].extra.wdeName)
                ) {
                    extraList.add(ExtraAndTotal(workExtrasPerDate[i].extra.wdeName, total))
                    total = 0.0
                } else if (i == workExtrasPerDate.size - 1) {
                    extraList.add(ExtraAndTotal(workExtrasPerDate[i].extra.wdeName, total))
                    total = 0.0
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
            return getPayHourly() //TODO: add the other values
        }

        fun getPayTimeWorked(): Double {
            return getPayReg() + getPayOt() + getPayDblOt()
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
                workExtrasPerDate.clear()
                list.listIterator().forEach {
                    workExtrasPerDate.add(it)
                }
            }
        }
    }

    private fun findExtrasPerPay() {
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.workExtraViewModel.getExtraTypesAndDefByPay(
                employer.employerId, cutOff
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