package ms.mattschlenkrich.paycalculator.ui.employer

import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.PayDayFrequencies
import ms.mattschlenkrich.paycalculator.data.entity.EmployerTaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.viewmodel.WorkTaxViewModel

fun validateEmployer(
    name: String,
    daysBefore: String,
    frequency: String,
    midMonthDate: String
): Int? {
    if (name.isBlank()) {
        return ms.mattschlenkrich.paycalculator.R.string.the_employer_must_have_a_name
    }
    if (daysBefore.isBlank() || daysBefore.toIntOrNull() == null) {
        return ms.mattschlenkrich.paycalculator.R.string.the_number_of_days_before_the_pay_day_is_required
    }
    if (frequency == PayDayFrequencies.SEMI_MONTHLY.toString() &&
        (midMonthDate.isBlank() || midMonthDate.toIntOrNull() == null || midMonthDate.toInt() == 0)
    ) {
        return ms.mattschlenkrich.paycalculator.R.string.for_semi_monthly_pay_days_there_needs_to_be_a_mid_month_pay_day
    }
    return null
}

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
        name.trim(),
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

suspend fun addEmployerTaxRules(
    employerId: Long,
    workTaxViewModel: WorkTaxViewModel,
    df: DateFunctions
) {
    workTaxViewModel.getTaxTypesSync().forEach { type ->
        workTaxViewModel.insertEmployerTaxType(
            EmployerTaxTypes(
                etrEmployerId = employerId,
                etrTaxType = type.taxType,
                etrInclude = true,
                etrIsDeleted = false,
                etrUpdateTime = df.getCurrentUTCTimeAsString()
            )
        )
    }
}