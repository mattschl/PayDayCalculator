package ms.mattschlenkrich.paycalculator

import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.paycalculator.data.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.logic.PayCalculationsAsync
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PayCalculationsTest {

    @Mock
    private lateinit var payCalculationsViewModel: PayCalculationsViewModel

    @Mock
    private lateinit var payDetailViewModel: PayDetailViewModel

    private val employerMock = Employers(
        -1550413509,
        "Cornerstone",
        "Bi-Weekly",
        "2024-10-04",
        "Friday",
        6,
        15,
        31,
        false,
        "2024-10-15 20:49:52"
    )

    private val payPeriodMock = PayPeriods(
        -493210422,
        "2025-01-04",
        -1550413509,
        false,
        "2024-12-22 09:30:31"
    )

    @Test
    fun getPayRateFromDb_answerIs_31_93() = runBlocking {
        val payCalculationsAsync = PayCalculationsAsync(
            payCalculationsViewModel,
            payDetailViewModel,
            employerMock,
            payPeriodMock
        )

        assert(payCalculationsAsync.calculatePayRateFromDb() == 31.93)
    }
}