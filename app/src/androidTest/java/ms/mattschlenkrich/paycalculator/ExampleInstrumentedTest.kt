package ms.mattschlenkrich.paycalculator

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext_returnsCorrectPackageNameAndResources() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("ms.mattschlenkrich.paycalculator", appContext.packageName)
        assertNotNull(appContext.getString(R.string.app_name))
    }

    @Test
    fun databaseInstance_initializesSuccessfully() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = PayDatabase(appContext)
        assertNotNull(db.getEmployerDao())
        assertNotNull(db.getPayDayDao())
        assertNotNull(db.getWorkOrderDao())
    }
}