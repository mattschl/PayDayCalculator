package ms.mattschlenkrich.paycalculator.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PayDayDaoTest {

    private lateinit var db: PayDatabase
    private lateinit var payDayDao: PayDayDao
    private val updateTime = "2024-01-01 12:00:00"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PayDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        payDayDao = db.getPayDayDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testGetWorkDateExtrasPerPay_JoinsCorrectly() = runBlocking {
        // 1. Setup Employer and PayPeriod
        db.getEmployerDao().insertEmployer(
            Employers(
                1L,
                "Emp",
                "Weekly",
                "2024-01-01",
                "Fri",
                0,
                7,
                31,
                false,
                updateTime
            )
        )
        db.getPayDayDao().insertPayPeriod(PayPeriods(1L, "2024-01-15", 1L, false, updateTime))

        // 2. Setup WorkDates
        val date1 = WorkDates(
            1L,
            1L,
            1L,
            "2024-01-15",
            "2024-01-10",
            0.0,
            0.0,
            0.0,
            0.0,
            null,
            false,
            updateTime
        )
        db.getPayDayDao().insertWorkDate(date1)

        // 3. Setup Extra Type
        // workExtraTypeId, wetName, wetEmployerId, wetAppliesTo, wetAttachTo, wetIsCredit, wetIsDefault, wetIsDeleted, wetUpdateTime
        val extraType = WorkExtraTypes(1L, "Bonus", 1L, 1, 1, true, true, false, updateTime)
        db.getWorkExtraDao().insertWorkExtraType(extraType)

        // 4. Setup WorkDateExtra
        // id, dateId, typeId, name, appliesTo, attachTo, value, fixed, credit, deleted, updateTime
        val dateExtra =
            WorkDateExtras(1L, 1L, 1L, "Bonus", 1, 1, 50.0, true, true, false, updateTime)
        db.getPayDayDao().insertWorkDateExtra(dateExtra)

        // 5. Verify Join
        val results = payDayDao.getWorkDateExtrasPerPay(1L, "2024-01-15").getOrAwaitValue()

        assertNotNull(results)
        assertEquals(1, results.size)
        assertEquals("Bonus", results[0].extra.wdeName)
        assertEquals("Bonus", results[0].type?.wetName)
    }

    @Test
    fun testGetPayPeriod_FiltersDeleted() = runBlocking {
        db.getEmployerDao().insertEmployer(
            Employers(
                1L,
                "Emp",
                "Weekly",
                "2024-01-01",
                "Fri",
                0,
                7,
                31,
                false,
                updateTime
            )
        )

        val pp1 = PayPeriods(1L, "2024-01-15", 1L, false, updateTime)

        db.getPayDayDao().insertPayPeriod(pp1)
        val result = payDayDao.getPayPeriod("2024-01-15", 1L).getOrAwaitValue()
        assertNotNull(result)

        db.getPayDayDao().updatePayPeriod(pp1.copy(ppIsDeleted = true))
        val syncResult = payDayDao.getPayPeriodSync("2024-01-15", 1L)
        assertEquals(null, syncResult)
    }
}