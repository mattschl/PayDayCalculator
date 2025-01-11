package ms.mattschlenkrich.paycalculator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.database.model.employer.Employers
import ms.mattschlenkrich.paycalculator.database.model.payperiod.PayPeriods
import ms.mattschlenkrich.paycalculator.payfunctions.PayCalculationsAsync
import ms.mattschlenkrich.paycalculator.ui.MainActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PayCalculationsTest {

    @Mock
    private val scope = CoroutineScope(Dispatchers.Default)

    @Mock
    private val mainActivity = MainActivity()

    @Mock
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

    @Mock
    private val payPeriodMock = PayPeriods(
        -493210422,
        "2025-01-04",
        -1550413509,
        false,
        "2024-12-22 09:30:31"
    )


    private val payCalculationsAsync = PayCalculationsAsync(
        mainActivity,
        employerMock,
        payPeriodMock
    )


    @Test
    fun getPayRateFromDb_answerIs_31_93() {
        scope.launch {
            assert(payCalculationsAsync.calculatePayRateFromDb() == 31.93)
        }
    }
}