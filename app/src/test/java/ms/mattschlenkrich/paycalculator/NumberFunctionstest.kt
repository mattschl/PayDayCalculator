package ms.mattschlenkrich.paycalculator

import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import org.junit.Test

class NumberFunctionsTest {
    @Test
    fun roundTo2DecimalsIsCorrect()    {
        val nf = NumberFunctions()
        val num = 8.41099
        val answer = 8.41
        assert(
            nf.roundTo2Decimals(num) == answer
            )
    }
}