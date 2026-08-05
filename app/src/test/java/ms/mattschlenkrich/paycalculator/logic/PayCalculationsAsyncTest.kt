package ms.mattschlenkrich.paycalculator.logic

import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.paycalculator.common.PayRateBasedOn
import ms.mattschlenkrich.paycalculator.common.TaxBasedOn
import ms.mattschlenkrich.paycalculator.data.entity.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.entity.TaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkTaxRules
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayCalculationsViewModel
import ms.mattschlenkrich.paycalculator.data.viewmodel.PayDetailViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PayCalculationsAsyncTest {

    @Mock
    private lateinit var payCalculationsViewModel: PayCalculationsViewModel

    @Mock
    private lateinit var payDetailViewModel: PayDetailViewModel

    private val employerId = 1L
    private val cutoffDate = "2024-01-15"
    private val payPeriodId = 10L

    private val employer = Employers(
        employerId, "Test Corp", "Bi-Weekly", "2024-01-01",
        "Friday", 0, 15, 31, false, ""
    )

    private val payPeriod = PayPeriods(
        payPeriodId, cutoffDate, employerId, false, ""
    )

    @Before
    fun setup() {
        // Default mocks to avoid NullPointerExceptions during init
        `when`(payCalculationsViewModel.getPayRate(employerId, cutoffDate))
            .thenReturn(
                EmployerPayRates(
                    1L,
                    employerId,
                    "2024-01-01",
                    PayRateBasedOn.HOURLY.value,
                    20.0,
                    false,
                    ""
                )
            )
        `when`(payCalculationsViewModel.getWorkDateList(employerId, cutoffDate))
            .thenReturn(emptyList())
        `when`(payDetailViewModel.getDaysWorked(employerId, cutoffDate))
            .thenReturn(0)
        `when`(payDetailViewModel.getHoursReg(employerId, cutoffDate))
            .thenReturn(0.0)
        `when`(payDetailViewModel.getHoursOt(employerId, cutoffDate))
            .thenReturn(0.0)
        `when`(payDetailViewModel.getHoursDblOt(employerId, cutoffDate))
            .thenReturn(0.0)
        `when`(payDetailViewModel.getHoursStat(employerId, cutoffDate))
            .thenReturn(0.0)
        `when`(payCalculationsViewModel.getWorkDateExtrasPerPay(employerId, cutoffDate))
            .thenReturn(emptyList())
        `when`(payCalculationsViewModel.getCustomPayPeriodExtras(payPeriodId))
            .thenReturn(emptyList())
        `when`(payCalculationsViewModel.getDefaultExtraTypesAndCurrentDef(employerId, cutoffDate))
            .thenReturn(emptyList())
        `when`(payCalculationsViewModel.getExtraTypes(employerId))
            .thenReturn(emptyList())
        `when`(payCalculationsViewModel.getTaxTypes(employerId))
            .thenReturn(emptyList())
        `when`(payCalculationsViewModel.getCurrentEffectiveDate(cutoffDate))
            .thenReturn(null)
    }

    @Test
    fun calculateBasicPay_80Hours_at_20Rate() = runBlocking {
        `when`(payDetailViewModel.getHoursReg(employerId, cutoffDate))
            .thenReturn(80.0)
        `when`(payDetailViewModel.getDaysWorked(employerId, cutoffDate))
            .thenReturn(10)

        val calculator = PayCalculationsAsync(
            payCalculationsViewModel,
            payDetailViewModel,
            employer,
            payPeriod
        )
        calculator.waitForCalculations()

        assertEquals(20.0, calculator.getPayRate(), 0.01)
        assertEquals(80.0, calculator.getHoursReg(), 0.01)
        assertEquals(1600.0, calculator.getPayReg(), 0.01)
        assertEquals(1600.0, calculator.getPayGross(), 0.01)
    }

    @Test
    fun calculateOvertimePay_10HoursOT_at_20Rate() = runBlocking {
        `when`(payDetailViewModel.getHoursReg(employerId, cutoffDate))
            .thenReturn(80.0)
        `when`(payDetailViewModel.getHoursOt(employerId, cutoffDate))
            .thenReturn(10.0)
        `when`(payDetailViewModel.getDaysWorked(employerId, cutoffDate))
            .thenReturn(10)

        val calculator = PayCalculationsAsync(
            payCalculationsViewModel,
            payDetailViewModel,
            employer,
            payPeriod
        )
        calculator.waitForCalculations()

        // 80 * 20 = 1600
        // 10 * 20 * 1.5 = 300
        // Total = 1900
        assertEquals(1600.0, calculator.getPayReg(), 0.01)
        assertEquals(300.0, calculator.getPayOt(), 0.01)
        assertEquals(1900.0, calculator.getPayGross(), 0.01)
    }

    @Test
    fun calculateDoubleOvertimePay_5HoursDblOT_at_20Rate() = runBlocking {
        `when`(payDetailViewModel.getHoursReg(employerId, cutoffDate))
            .thenReturn(80.0)
        `when`(payDetailViewModel.getHoursDblOt(employerId, cutoffDate))
            .thenReturn(5.0)

        val calculator = PayCalculationsAsync(
            payCalculationsViewModel,
            payDetailViewModel,
            employer,
            payPeriod
        )
        calculator.waitForCalculations()

        // 80 * 20 = 1600
        // 5 * 20 * 2.0 = 200
        // Total = 1800
        assertEquals(200.0, calculator.getPayDblOt(), 0.01)
        assertEquals(1800.0, calculator.getPayGross(), 0.01)
    }

    @Test
    fun calculateStatPay_8HoursStat_at_20Rate() = runBlocking {
        `when`(payDetailViewModel.getHoursReg(employerId, cutoffDate))
            .thenReturn(72.0)
        `when`(payDetailViewModel.getHoursStat(employerId, cutoffDate))
            .thenReturn(8.0)

        val calculator = PayCalculationsAsync(
            payCalculationsViewModel,
            payDetailViewModel,
            employer,
            payPeriod
        )
        calculator.waitForCalculations()

        // 72 * 20 = 1440
        // 8 * 20 = 160
        // Total = 1600
        assertEquals(160.0, calculator.getPayStat(), 0.01)
        assertEquals(1600.0, calculator.getPayGross(), 0.01)
    }

    @Test
    fun calculateExtras_fixedCreditAndDebit() = runBlocking {
        val bonus = ms.mattschlenkrich.paycalculator.data.entity.WorkPayPeriodExtras(
            1L, payPeriodId, null, "Bonus",
            ms.mattschlenkrich.paycalculator.common.ExtraAttachToFrequencies.PER_PAY.value,
            0, 100.0, true, true, false, ""
        )
        val deduction = ms.mattschlenkrich.paycalculator.data.entity.WorkPayPeriodExtras(
            2L, payPeriodId, null, "Parking",
            ms.mattschlenkrich.paycalculator.common.ExtraAttachToFrequencies.PER_PAY.value,
            0, 25.0, true, false, false, ""
        )

        `when`(payCalculationsViewModel.getCustomPayPeriodExtras(payPeriodId))
            .thenReturn(listOf(bonus, deduction))
        `when`(payDetailViewModel.getHoursReg(employerId, cutoffDate))
            .thenReturn(80.0)

        val calculator = PayCalculationsAsync(
            payCalculationsViewModel,
            payDetailViewModel,
            employer,
            payPeriod
        )
        calculator.waitForCalculations()

        // Reg = 1600
        // Credits = 100
        // Debits = 25
        // Gross = 1600 + 100 = 1700
        assertEquals(100.0, calculator.getCreditTotalAll(), 0.01)
        assertEquals(25.0, calculator.getDebitTotalsByPay(), 0.01)
        assertEquals(1700.0, calculator.getPayGross(), 0.01)
    }

    @Test
    fun calculateTaxes_simple10PercentNoExemption() = runBlocking {
        val taxType = TaxTypes(1L, "Income Tax", TaxBasedOn.TIME_WORKED_ONLY.value, false, "")
        val taxRule = WorkTaxRules(
            1L, "Income Tax", 1, "2024-01-01", 0.10,
            false, 0.0, false, 0.0, false, ""
        )

        `when`(payCalculationsViewModel.getTaxTypes(employerId)).thenReturn(listOf(taxType))
        `when`(payCalculationsViewModel.getCurrentEffectiveDate(cutoffDate)).thenReturn("2024-01-01")
        `when`(payCalculationsViewModel.getTaxRules("2024-01-01")).thenReturn(listOf(taxRule))

        `when`(payDetailViewModel.getHoursReg(employerId, cutoffDate)).thenReturn(80.0)

        val calculator = PayCalculationsAsync(
            payCalculationsViewModel,
            payDetailViewModel,
            employer,
            payPeriod
        )
        calculator.waitForCalculations()

        // Pay = 1600. Tax = 1600 * 0.10 = 160.
        assertEquals(160.0, calculator.getAllTaxDeductions(), 0.01)
    }
}