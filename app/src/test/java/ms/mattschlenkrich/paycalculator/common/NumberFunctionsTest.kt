package ms.mattschlenkrich.paycalculator.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberFunctionsTest {

    private val nf = NumberFunctions()

    @Test
    fun getDoubleFromDollars_handlesVariousInputs() {
        assertEquals(123.45, nf.getDoubleFromDollars("$123.45"), 0.001)
        assertEquals(1234.56, nf.getDoubleFromDollars("1,234.56"), 0.001)
        assertEquals(10.0, nf.getDoubleFromDollars(" 10 "), 0.001)
        assertEquals(0.0, nf.getDoubleFromDollars(""), 0.001)
    }

    @Test
    fun displayDollars_formatsCorrectly() {
        // Locale.CANADA is used, which should be "$1,234.56"
        val result = nf.displayDollars(1234.56)
        assertTrue(result.contains("$"))
        assertTrue(result.contains("1,234.56"))
    }

    @Test
    fun displayNumberFromDouble_formatsCorrectly() {
        // Locale.CANADA number instance
        assertEquals("1,234.56", nf.displayNumberFromDouble(1234.56))
        assertEquals("0.5", nf.displayNumberFromDouble(0.5))
    }

    @Test
    fun generateRandomIdAsLong_returnsNonZero() {
        val id1 = nf.generateRandomIdAsLong()
        val id2 = nf.generateRandomIdAsLong()
        assertNotEquals(0L, id1)
        assertNotEquals(id1, id2)
    }

    @Test
    fun getDoubleFromPercentString_convertsCorrectly() {
        assertEquals(0.05, nf.getDoubleFromPercentString("5%"), 0.001)
        assertEquals(0.125, nf.getDoubleFromPercentString("12.5 %"), 0.001)
        assertEquals(1.0, nf.getDoubleFromPercentString("100"), 0.001)
    }

    @Test
    fun getPercentStringFromDouble_convertsCorrectly() {
        assertEquals("5.0%", nf.getPercentStringFromDouble(0.05))
        assertEquals("12.5%", nf.getPercentStringFromDouble(0.125))
        assertEquals("100.0%", nf.getPercentStringFromDouble(1.0))
    }

    @Test
    fun getDoubleFromDollarOrPercentString_handlesMixedInputs() {
        assertEquals(0.05, nf.getDoubleFromDollarOrPercentString("5%"), 0.001)
        assertEquals(123.45, nf.getDoubleFromDollarOrPercentString("$123.45"), 0.001)
        assertEquals(50.0, nf.getDoubleFromDollarOrPercentString("50"), 0.001)
        assertEquals(0.0, nf.getDoubleFromDollarOrPercentString(null), 0.001)
    }

    @Test
    fun roundTo2Decimals_isCorrect() {
        assertEquals(8.41, nf.roundTo2Decimals(8.41099), 0.001)
        assertEquals(8.41, nf.roundTo2Decimals(8.414), 0.001)
        assertEquals(8.42, nf.roundTo2Decimals(8.415), 0.001)
    }
}