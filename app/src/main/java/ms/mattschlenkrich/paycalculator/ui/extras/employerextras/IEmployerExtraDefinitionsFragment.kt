package ms.mattschlenkrich.paycalculator.ui.extras.employerextras

import android.annotation.SuppressLint

interface IEmployerExtraDefinitionsFragment {
    @SuppressLint("NotifyDataSetChanged")
    fun populateExtrasList()
    fun gotoEmployerExtraDefinitionUpdateFragment()
}