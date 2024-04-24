package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import ms.mattschlenkrich.paydaycalculator.model.employer.Employers
import ms.mattschlenkrich.paydaycalculator.model.payperiod.PayPeriods
import ms.mattschlenkrich.paydaycalculator.ui.MainActivity

class PayCalculations2(
    private val mainActivity: MainActivity,
    private val employer: Employers,
    private val cutOff: String,
    private val mView: View,
    private val curPayPeriod: PayPeriods,
) {

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
        fun getRate() {}
        fun getPayReg() {}
        fun getPayOt() {}
        fun getPayDblOt() {}
        fun getPayHourly() {}
        fun getPayStat() {}
        fun getPayGross() {}
        fun getPayTimeWorked() {}
    }

    inner class Hours {
        fun getHoursWorked() {}
        fun getHoursAll() {}
        fun getHoursReg() {}
        fun getHoursOt() {}
        fun getHoursDblOt() {}
        fun getHoursStat() {}
        fun getDaysWorked() {}

    }
}