package ms.mattschlenkrich.paycalculator.common

import ms.mattschlenkrich.paycalculator.data.model.MaterialAndQuantity
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkOrderCalculationsTest {

    @Test
    fun testCalculateMaterialAmount_UsesPrice_WhenPriceSet() {
        val item =
            MaterialAndQuantity(1L, "Material", 2.0, totalAmount = 0.0, cost = 10.0, price = 25.0)
        val factor = 0.8 // 20% markup
        val result = WorkOrderCalculations.calculateMaterialAmount(item, factor)
        // Price (25.0) * Quantity (2.0) = 50.0
        assertEquals(50.0, result, 0.01)
    }

    @Test
    fun testCalculateMaterialAmount_UsesCostWithMarkup_WhenPriceNotSet() {
        val item =
            MaterialAndQuantity(1L, "Material", 2.0, totalAmount = 0.0, cost = 10.0, price = 0.0)
        val factor = 0.8 // 20% markup
        val result = WorkOrderCalculations.calculateMaterialAmount(item, factor)
        // (Cost (10.0) * Quantity (2.0)) / Factor (0.8) = 25.0
        assertEquals(25.0, result, 0.01)
    }

    @Test
    fun testCalculateMaterialAmount_UsesTotalAmount_WhenNeitherCostNorPriceSet() {
        val item =
            MaterialAndQuantity(1L, "Material", 2.0, totalAmount = 30.0, cost = 0.0, price = 0.0)
        val factor = 0.8
        val result = WorkOrderCalculations.calculateMaterialAmount(item, factor)
        assertEquals(30.0, result, 0.01)
    }
}