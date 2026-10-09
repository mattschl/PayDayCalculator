package ms.mattschlenkrich.paycalculator.ui.calculator

import android.app.Application
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import ms.mattschlenkrich.paycalculator.common.CalculatorLogic
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CalculatorTransferTest {

    private lateinit var mainViewModel: MainViewModel
    private val prefs: SharedPreferences = mockk(relaxed = true)
    private val application: Application = mockk {
        every { getSharedPreferences(any(), any()) } returns prefs
        every { filesDir } returns java.io.File("/tmp")
    }

    @Before
    fun setup() {
        mainViewModel = MainViewModel(application)
    }

    @Test
    fun testCalculatorTransfer_storesAndClearsState() {
        assertNull(mainViewModel.transferNumState.value)
        assertEquals(0.0, mainViewModel.getTransferNum(), 0.001)

        mainViewModel.setTransferNum(42.5)
        assertEquals(42.5, mainViewModel.transferNumState.value)
        assertEquals(42.5, mainViewModel.getTransferNum(), 0.001)

        mainViewModel.clearTransferNum()
        assertNull(mainViewModel.transferNumState.value)
        assertEquals(0.0, mainViewModel.getTransferNum(), 0.001)
    }

    @Test
    fun testCalculatorTransfer_handlesZeroValue() {
        mainViewModel.setTransferNum(0.0)
        assertEquals(0.0, mainViewModel.transferNumState.value)
        assertEquals(0.0, mainViewModel.getTransferNum(), 0.001)

        mainViewModel.clearTransferNum()
        assertNull(mainViewModel.transferNumState.value)
    }

    @Test
    fun testCalculatorLogic_calculations() {
        assertEquals(15.0, CalculatorLogic.calculate(10.0, 5.0, "+"), 0.001)
        assertEquals(5.0, CalculatorLogic.calculate(10.0, 5.0, "-"), 0.001)
        assertEquals(50.0, CalculatorLogic.calculate(10.0, 5.0, "X"), 0.001)
        assertEquals(2.0, CalculatorLogic.calculate(10.0, 5.0, "/"), 0.001)
    }
}