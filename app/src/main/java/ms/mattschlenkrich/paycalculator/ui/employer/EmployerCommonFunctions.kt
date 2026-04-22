package ms.mattschlenkrich.paycalculator.ui.employer

import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.data.Employers
import ms.mattschlenkrich.paycalculator.data.WorkTaxViewModel

fun getCurrentEmployer(
    id: Long,
    name: String,
    frequency: String,
    startDate: String,
    dayOfWeek: String,
    daysBefore: String,
    midMonthDate: String,
    mainMonthDate: String,
    df: DateFunctions
): Employers {
    return Employers(
        id,
        name,
        frequency,
        startDate,
        dayOfWeek,
        daysBefore.toIntOrNull() ?: 0,
        midMonthDate.toIntOrNull() ?: 0,
        mainMonthDate.toIntOrNull() ?: 0,
        false,
        df.getCurrentUTCTimeAsString()
    )
}

fun validateEmployer(
    name: String,
    daysBefore: String,
    frequency: String,
    midMonthDate: String
): Int? {
    if (name.isBlank()) {
        return R.string.the_employer_must_have_a_name
    }
    if (daysBefore.isBlank()) {
        return R.string.the_number_of_days_before_the_pay_day_is_required
    }
    if (frequency == ms.mattschlenkrich.paycalculator.common.INTERVAL_SEMI_MONTHLY && midMonthDate.isBlank()) {
        return R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day
    }
    return null
}

fun addEmployerTaxRules(
    employerId: Long,
    workTaxViewModel: WorkTaxViewModel,
    df: DateFunctions
) {
    workTaxViewModel.getTaxTypes().observeForever { type ->
        type.forEach {
            workTaxViewModel.insertEmployerTaxType(
                EmployerTaxTypes(
                    etrEmployerId = employerId,
                    etrTaxType = it.taxType,
                    etrInclude = true,
                    etrIsDeleted = false,
                    etrUpdateTime = df.getCurrentUTCTimeAsString()
                )
            )
        }
    }
}