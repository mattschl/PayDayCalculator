package ms.mattschlenkrich.paycalculator.ui.tax

import ms.mattschlenkrich.paycalculator.R
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.TaxTypes

fun validateTaxType(
    name: String,
    list: List<TaxTypes>
): Int? {
    if (name.isBlank()) {
        return R.string.the_tax_type_must_have_a_name
    }
    if (list.any { it.taxType.equals(name.trim(), ignoreCase = true) }) {
        return R.string.this_tax_type_already_exists
    }
    return null
}

fun validateTaxTypeUpdate(
    name: String,
    cur: TaxTypes,
    list: List<TaxTypes>
): Int? {
    if (name.isBlank()) {
        return R.string.the_tax_type_must_have_a_name
    }
    if (list.any { it.taxType == name.trim() && it.taxType != cur.taxType }) {
        return R.string.this_tax_type_already_exists
    }
    return null
}

fun validateTaxRule(
    nf: NumberFunctions,
    percentage: String,
    hasExemption: Boolean,
    exemptionAmount: String,
    hasUpperLimit: Boolean,
    upperLimit: String
): Int? {
    if (percentage.isBlank() || nf.getDoubleFromDollarOrPercentString(percentage) == 0.0) {
        return R.string.there_should_be_a_percentage_here
    }
    if (hasExemption && (exemptionAmount.isBlank() || nf.getDoubleFromDollarOrPercentString(
            exemptionAmount
        ) == 0.0)
    ) {
        return R.string.an_exemption_is_indicated_but_no_amount_was_entered
    }
    if (hasUpperLimit && (upperLimit.isBlank() || nf.getDoubleFromDollarOrPercentString(
            upperLimit
        ) == 0.0)
    ) {
        return R.string.an_upper_limit_is_indicated_but_no_amount_was_entered
    }
    return null
}