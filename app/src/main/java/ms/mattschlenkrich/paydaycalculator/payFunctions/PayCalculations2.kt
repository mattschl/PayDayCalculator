package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.util.Log
import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.paydaycalculator.common.WAIT_250
import ms.mattschlenkrich.paydaycalculator.model.employer.EmployerPayRates
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.model.payperiod.WorkDates
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

private const val TAG = "PayCalculations2"

class PayCalculations2(
    private val mainActivity: MainActivity,
    private val employer: Employers,
//    private val cutOff: String,
    private val mView: View,
    private val curPayPeriod: PayPeriods,
) {
//    private lateinit var workDates: ArrayList<WorkDates>

    //    private lateinit var workDateExtrasFull: ArrayList<WorkDateExtraAndTypeFull>
//    private lateinit var workExtrasByPay: ArrayList<ExtraDefinitionAndType>
//    private lateinit var extraTypes: ArrayList<WorkExtraTypes>
//    private lateinit var taxRules: ArrayList<WorkTaxRules>
//    private lateinit var taxTypes: ArrayList<TaxTypes>
//    private lateinit var payPeriodExtras: ArrayList<WorkPayPeriodExtras>
    val extras = Extras()
    val tax = Tax()
    val pay = Pay()
    val hours = Hours()
    var payRate = 0.0
    private var daysWorked = 0
    private var hoursReg = 0.0
    private var hoursOt = 0.0
    private var hoursDblOt = 0.0
    private var hoursStat = 0.0
    private var hoursWorked = 0.0
    private var hoursAll = 0.0
    private var payReg = 0.0
    private var payOt = 0.0
    private var payDblOt = 0.0
    private var payHourly = 0.0
    private var payStat = 0.0
    private var payTimeWorked = 0.0

    init {
        runBlocking {
            val payRateDefer = async { findPayRate() }
            val workDatesDefer =
                async { findWorkDates() }
            delay(WAIT_250)
            val daysWorkedDefer =
                async { findDaysWorked(workDatesDefer.await()) }
            val hoursRegDefer =
                async { findHoursReg(workDatesDefer.await()) }
            val hoursOtDefer =
                async { findHoursOt(workDatesDefer.await()) }
            val hoursDblOtDefer =
                async { findHoursDblOt(workDatesDefer.await()) }
            val hoursStatDefer =
                async { findHoursStat(workDatesDefer.await()) }
            val hoursWorkedDefer =
                async {
                    findHoursWorked(
                        hoursRegDefer.await(),
                        hoursOtDefer.await(),
                        hoursDblOtDefer.await(),
                    )
                }
            val hoursAllDefer =
                async {
                    findHoursAll(
                        hoursWorkedDefer.await(),
                        hoursStatDefer.await()
                    )
                }
            val payRegDefer =
                async {
                    findPayReg(
                        hoursRegDefer.await(),
                        payRateDefer.await()
                    )
                }
            val payOtDefer =
                async {
                    findPayOt(
                        hoursOtDefer.await(),
                        payRateDefer.await()
                    )
                }
            val payDblOtDefer =
                async {
                    findPayDblOt(
                        hoursDblOtDefer.await(),
                        payRateDefer.await()
                    )
                }
            val payTimeWorkedDefer =
                async {
                    findPayTimeWorked(
                        payRegDefer.await(),
                        payOtDefer.await(),
                        payDblOtDefer.await()
                    )
                }
            val payStatDefer =
                async {
                    findPayStat(
                        hoursStatDefer.await(),
                        payRateDefer.await()
                    )
                }
            val payHourlyDefer =
                async {
                    findPayHourly(
                        payTimeWorkedDefer.await(),
                        payStatDefer.await()
                    )
                }


            payRate = payRateDefer.await()
            daysWorked = daysWorkedDefer.await()
            hoursReg = hoursRegDefer.await()
            hoursDblOt = hoursOtDefer.await()
            hoursDblOt = hoursDblOtDefer.await()
            hoursStat = hoursStatDefer.await()
            hoursWorked = hoursWorkedDefer.await()
            hoursAll = hoursAllDefer.await()
            payReg = payRegDefer.await()
            payOt = payOtDefer.await()
            payDblOt = payDblOtDefer.await()
            payHourly = payHourlyDefer.await()
            payStat = payStatDefer.await()
            payTimeWorked = payTimeWorkedDefer.await()
            payHourly = payHourlyDefer.await()
        }
    }

    private fun findPayTimeWorked(reg: Double, ot: Double, dblOt: Double): Double {
        return reg + ot + dblOt
    }

    private fun findPayStat(hours: Double, rate: Double): Double {
        return hours * rate

    }

    private fun findPayHourly(timeWorked: Double, stat: Double): Double {
        return timeWorked + stat
    }

    private fun findPayDblOt(hours: Double, rate: Double): Double {
        return hours * rate * 2
    }

    private fun findPayOt(hours: Double, rate: Double): Double {
        return hours * rate * 1.5

    }

    private fun findPayReg(hours: Double, rate: Double): Double {
        return hours * rate
    }

    private fun findHoursAll(worked: Double, stat: Double): Double {
        return worked + stat
    }

    private fun findHoursWorked(reg: Double, ot: Double, dblOt: Double): Double {
        return reg + ot + dblOt
    }

    private fun findHoursStat(workDates: List<WorkDates>): Double {
        var hours = 0.0
        for (day in workDates) {
            if (!day.wdIsDeleted) hours += day.wdStatHours
        }
        return hours
    }

    private fun findHoursDblOt(workDates: List<WorkDates>): Double {
        var hours = 0.0
        for (day in workDates) {
            if (!day.wdIsDeleted) hours += day.wdDblOtHours
        }
        return hours
    }

    private fun findHoursOt(workDates: List<WorkDates>): Double {
        var hours = 0.0
        for (day in workDates) {
            if (!day.wdIsDeleted) hours += day.wdOtHours
        }
        return hours
    }

    private fun findHoursReg(workDates: List<WorkDates>): Double {
        var hours = 0.0
        for (day in workDates) {
            if (!day.wdIsDeleted) hours += day.wdRegHours
        }
        Log.d(
            TAG,
            "in findHoursReg and there are $hours hours and the size of workDates is ${workDates.size}"
        )
        return hours
    }

    private fun findDaysWorked(workDates: List<WorkDates>): Int {
        var days = 0
        for (day in workDates) {
            days++
        }
        return days
    }

    private fun findPayRate(): Double {
        var rate = 0.0
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
        return rate
    }

    private suspend fun findWorkDates(): List<WorkDates> {
        val innerWorkDates = ArrayList<WorkDates>()
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.payDayViewModel.getWorkDateList(
                employer.employerId, curPayPeriod.ppCutoffDate
            ).observe(lifecycleOwner) { list ->
                list.listIterator().forEach {
                    innerWorkDates.add(it)
                    Log.d(TAG, "in findWorkDates and ${it.wdDate}")
                }
            }
        }
        return innerWorkDates
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

    inner class Extras {
        fun getCredits() {}
        fun getCreditTotal() {}
        fun getCreditsCustom() {}
        fun getDeductions() {}
        fun getDeductionsCustom() {}
        fun getDeductionsTotal() {}
    }

    inner class Tax {
        fun getTaxList() {}
        fun getTaxTotal() {}
    }

    inner class Pay {
        fun getRate(): Double {
            return payRate
        }

        fun getPayReg(): Double {
            return payReg
        }

        fun getPayOt(): Double {
            return payOt
        }

        fun getPayDblOt(): Double {
            return payDblOt
        }

        fun getPayHourly(): Double {
            return payHourly
        }

        fun getPayStat(): Double {
            return payStat
        }

        fun getPayGross() {}
        fun getPayTimeWorked(): Double {
            return payTimeWorked
        }
    }

    inner class Hours {
        fun getHoursWorked(): Double {
            return hoursWorked
        }

        fun getHoursAll(): Double {
            return hoursAll
        }

        fun getHoursReg(): Double {
            return hoursReg
        }

        fun getHoursOt(): Double {
            return hoursOt
        }

        fun getHoursDblOt(): Double {
            return hoursDblOt
        }

        fun getHoursStat(): Double {
            return hoursStat
        }

        fun getDaysWorked(): Int {
            return daysWorked
        }

    }
}