package ms.mattschlenkrich.paycalculator.payfunctions

import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import ms.mattschlenkrich.paycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class EmployerPayRate(
    private val mainActivity: MainActivity,
    private val mView: View,
    private val currentPayPeriod: PayPeriods
) {
    fun getPayRate(): Double {
        var payRate = 0.0
        mView.findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            mainActivity.employerViewModel.getEmployerPayRates(
                currentPayPeriod.ppEmployerId
            ).observe(lifecycleOwner) { rates ->
                for (rate in rates) {
                    if (rate.eprEffectiveDate <=
                        currentPayPeriod.ppCutoffDate
                    ) {
                        payRate = rate.eprPayRate
                        break
                    }
                }
            }
        }
        return payRate
    }
}