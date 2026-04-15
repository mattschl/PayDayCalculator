package ms.mattschlenkrich.paycalculator

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed class Screen(val route: String, @StringRes val resourceId: Int, @DrawableRes val icon: Int) {
    object TimeSheet : Screen("timeSheet", R.string.time_sheet, R.drawable.ic_time_sheet)
    object PayDetails : Screen("payDetails", R.string.pay_details, R.drawable.ic_check_foreground)
    object Employers : Screen("employers", R.string.employers, R.drawable.ic_employer_foreground)
    object Taxes : Screen("taxes", R.string.taxes, R.drawable.ic_tax)
    object Extras : Screen("extras", R.string.pay_extras, R.drawable.ic_extras)

    object EmployerAdd :
        Screen("employerAdd", R.string.add_an_employer, R.drawable.ic_employer_foreground)

    object EmployerUpdate :
        Screen("employerUpdate", R.string.update_employer, R.drawable.ic_employer_foreground)

    object TaxTypeAdd : Screen("taxTypeAdd", R.string.add_a_new_tax_type, R.drawable.ic_tax)
    object TaxTypeUpdate : Screen("taxTypeUpdate", R.string.update_tax_type, R.drawable.ic_tax)
    object TaxRuleAdd : Screen("taxRuleAdd", R.string.add_tax_rule, R.drawable.ic_tax)
    object TaxRuleUpdate :
        Screen("taxRuleUpdate", R.string.view_or_update_tax_rule, R.drawable.ic_tax)

    object EmployerPayRates :
        Screen("employerPayRates", R.string.view_or_edit_wages, R.drawable.ic_employer_foreground)

    object EmployerPayRateAdd :
        Screen("employerPayRateAdd", R.string.add_a_pay_rate, R.drawable.ic_employer_foreground)

    object EmployerPayRateUpdate :
        Screen("employerPayRateUpdate", R.string.update, R.drawable.ic_employer_foreground)

    object EmployerExtraDefinitionsAdd :
        Screen("employerExtraDefinitionsAdd", R.string.add_new_extra, R.drawable.ic_extras)

    object EmployerExtraDefinitionUpdate :
        Screen("employerExtraDefinitionUpdate", R.string.update_extra_type, R.drawable.ic_extras)

    object WorkExtraTypeAdd :
        Screen("workExtraTypeAdd", R.string.add_extra_type, R.drawable.ic_extras)

    object WorkExtraTypeUpdate :
        Screen("workExtraTypeUpdate", R.string.update_extra_type, R.drawable.ic_extras)

    object PayPeriodExtraAdd :
        Screen("payPeriodExtraAdd", R.string.add_new_extra, R.drawable.ic_extras)

    object PayPeriodExtraUpdate :
        Screen("payPeriodExtraUpdate", R.string.update_extra_type, R.drawable.ic_extras)

    object WorkDateAdd :
        Screen("workDateAdd", R.string.add_a_new_work_date, R.drawable.ic_time_sheet)

    object WorkDateUpdate :
        Screen("workDateUpdate", R.string.update_this_work_date, R.drawable.ic_time_sheet)

    object WorkDateTimes : Screen("workDateTimes", R.string.time_sheet, R.drawable.ic_time_sheet)
    object WorkDateExtraAdd :
        Screen("workDateExtraAdd", R.string.add_new_extra, R.drawable.ic_extras)

    object WorkDateExtraUpdate :
        Screen("workDateExtraUpdate", R.string.update_extra_type, R.drawable.ic_extras)

    object WorkOrderHistoryAdd :
        Screen("workOrderHistoryAdd", R.string.add_new_work_order, R.drawable.ic_time_sheet)

    object WorkOrderHistoryUpdate :
        Screen("workOrderHistoryUpdate", R.string.update_work_order, R.drawable.ic_time_sheet)
}

val bottomNavItems = listOf(
    Screen.TimeSheet,
    Screen.PayDetails,
    Screen.Employers,
    Screen.Taxes,
    Screen.Extras
)