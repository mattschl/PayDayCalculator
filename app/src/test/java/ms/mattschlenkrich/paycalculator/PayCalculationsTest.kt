package ms.mattschlenkrich.paycalculator

import kotlinx.coroutines.test.runTest
import ms.mattschlenkrich.paycalculator.data.entity.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDetailViewModel
import ms.mattschlenkrich.paycalculator.logic.PayCalculationsAsync
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

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
    fun getPayRateFromDb_returnsCorrectRate() = runTest {
        whenever(
            payCalculationsViewModel.getPayRate(
                employerMock.employerId,
                payPeriodMock.ppCutoffDate
            )
        ).thenReturn(
            EmployerPayRates(
                0L,
                employerMock.employerId,
                "2025-01-01",
                ms.mattschlenkrich.paycalculator.common.PayRateBasedOn.HOURLY.value,
                31.93,
                false,
                ""
            )
        )

        val payCalculationsAsync = PayCalculationsAsync(
            payCalculationsViewModel,
            payDetailViewModel,
            employerMock,
            payPeriodMock
        )

        assertEquals(31.93, payCalculationsAsync.calculatePayRateFromDb(), 0.01)
    }
}