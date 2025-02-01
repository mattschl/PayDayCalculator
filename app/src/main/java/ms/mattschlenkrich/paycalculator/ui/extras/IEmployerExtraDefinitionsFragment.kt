package ms.mattschlenkrich.paycalculator.ui.extras

import android.annotation.SuppressLint

interface IEmployerExtraDefinitionsFragment {
    @SuppressLint("NotifyDataSetChanged")
    fun populateExtrasList()
    fun gotoEmployerExtraDefinitionUpdateFragment()
}