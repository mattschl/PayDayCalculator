package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.model.Employers
import ms.mattschlenkrich.paydaycalculator.model.ExtraDefinitionAndType
import ms.mattschlenkrich.paydaycalculator.model.WorkDateExtraAndTypeFull
import ms.mattschlenkrich.paydaycalculator.model.WorkDates

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
    var rate = 0.0
    val hours = Hours()
    val pay = Pay()
    val extras = Extras()

    init {
        findWorkDates()
        findExtrasPerDay()
        findExtrasPerPay()
        findRate()
    }

    inner class Extras {

        fun getExtraFixedTotalByDaily(): Double {
            var extraTotal = 0.0
            for (extra in workExtrasPerDate) {
                if (extra.extra.wdeIsFixed) {
                    if (extra.extra.wdeAppliesTo == 0) {
                        for (date in workDates) {
                            if (extra.extra.wdeWorkDateId == date.workDateId &&
                                !extra.extra.wdeIsDeleted
                            ) {
                                extraTotal += extra.extra.wdeValue *
                                        (date.wdRegHours + date.wdOtHours + date.wdDblOtHours)
                            }
                        }
                    } else if (extra.extra.wdeAppliesTo == 1) {
                        extraTotal += extra.extra.wdeValue
                    }
                }
            }
            return extraTotal
        }


        fun getExtraPercentTotalByDaily(): Double {
            var extraTotal = 0.0
            for (extra in workExtrasPerDate) {
                if (!extra.extra.wdeIsFixed) {
                    for (date in workDates) {
                        if (extra.extra.wdeWorkDateId == date.workDateId &&
                            !extra.extra.wdeIsDeleted
                        ) {
                            extraTotal += extra.extra.wdeValue * (
                                    date.wdRegHours + date.wdOtHours + date.wdDblOtHours)
                        }
                    }
                }
            }
            return extraTotal
        }

        fun getExtraPercentTotalByPay(): Double {
            var extraTotal = 0.0
            for (extra in workExtrasByPay) {
                if (!extra.definition.weIsFixed &&
                    extra.extraType.wetAppliesTo == 3 &&
                    !extra.extraType.wetIsDeleted &&
                    !extra.definition.weIsDeleted
                ) {
                    extraTotal += (pay.getPayReg() +
                            pay.getPayOt() +
                            pay.getPayDblOt()) *
                            extra.definition.weValue
                }
            }
//        Log.d(TAG, "extraTotal is $extraTotal")
            return extraTotal
        }

        fun getExtraCreditTotalsByPay(): Double {
            var extraTotal = 0.0
            for (extra in workExtrasByPay) {
                if (extra.extraType.wetIsCredit) {
                    extraTotal += if (extra.definition.weIsFixed) {
                        extra.definition.weValue
                    } else {
                        extra.definition.weValue * pay.getPayHourly() / 100
                    }
                }
            }
            return extraTotal
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
            return getPayHourly() +
                    extras.getExtraFixedTotalByDaily() +
                    extras.getExtraPercentTotalByDaily() +
                    extras.getExtraCreditTotalsByPay() +
                    extras.getExtraPercentTotalByPay()
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

}