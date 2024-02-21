package ms.mattschlenkrich.paydaycalculator.payFunctions

import android.view.View
import ms.mattschlenkrich.paydaycalculator.MainActivity
import ms.mattschlenkrich.paydaycalculator.model.Employers

class Deductions(
    private val mainActivity: MainActivity,
    private val employer: Employers,
    private val cutOff: String,
    private val mView: View,
) {
    init {
        findDeductions()
    }

    private fun findDeductions() {
//        mView.findViewTreeLifecycleOwner()?.let {
//            mainActivity
//        }
    }
}