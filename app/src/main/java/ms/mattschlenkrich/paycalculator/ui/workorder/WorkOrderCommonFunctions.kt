package ms.mattschlenkrich.paycalculator.ui.workorder

import ms.mattschlenkrich.paycalculator.R

fun validateWorkOrder(
    woNumber: String,
    address: String,
    description: String
): Int? {
    if (woNumber.isBlank()) {
        return R.string.the_work_order_must_have_a_number
    }
    if (address.isBlank()) {
        return R.string.the_work_order_must_have_an_address
    }
    if (description.isBlank()) {
        return R.string.the_work_order_must_have_a_description
    }
    return null
}