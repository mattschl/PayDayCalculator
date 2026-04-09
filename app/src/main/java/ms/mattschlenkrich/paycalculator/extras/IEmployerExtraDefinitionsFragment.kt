package ms.mattschlenkrich.paycalculator.extras

import android.annotation.SuppressLint

interface IEmployerExtraDefinitionsFragment {
    @SuppressLint("NotifyDataSetChanged")
    fun populateExtrasList()
    fun gotoEmployerExtraDefinitionUpdateFragment()
}