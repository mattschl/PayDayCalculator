package ms.mattschlenkrich.paycalculator.common

import ms.mattschlenkrich.paycalculator.data.model.MaterialAndQuantity

object WorkOrderCalculations {

    fun calculateMaterialAmount(item: MaterialAndQuantity, factor: Double): Double {
        return if (item.price > 0.0) {
            item.price * item.quantity
        } else if (item.cost > 0.0) {
            if (factor > 0.0) (item.cost * item.quantity) / factor else (item.cost * item.quantity)
        } else {
            item.totalAmount
        }
    }
}