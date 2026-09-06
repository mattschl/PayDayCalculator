package ms.mattschlenkrich.paycalculator.data.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryMaterial
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
class WorkOrderDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: PayDatabase
    private lateinit var workOrderDao: WorkOrderDao
    private val updateTime = "2024-01-01 12:00:00"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PayDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        workOrderDao = db.getWorkOrderDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun setupPrerequisites() {
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
        db.getPayDayDao().insertWorkDate(
            WorkDates(
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
        )
        db.getPayDayDao().insertWorkDate(
            WorkDates(
                2L,
                1L,
                1L,
                "2024-01-15",
                "2024-01-11",
                0.0,
                0.0,
                0.0,
                0.0,
                null,
                false,
                updateTime
            )
        )
    }

    @Test
    fun testGetWorkOrderMaterialsSummary_HandlesMergedMaterials() = runBlocking {
        setupPrerequisites()

        // 1. Setup Master and Child Materials
        val masterMaterial = Material(1L, "Master Material", 8.0, 10.0, false, updateTime)
        val childMaterial = Material(2L, "Child Material", 4.0, 5.0, false, updateTime)
        db.getMaterialDao().insertMaterial(masterMaterial)
        db.getMaterialDao().insertMaterial(childMaterial)

        // 2. Setup Merge Record (Child -> Master)
        val merge = MaterialMerged(1L, 1L, 2L, false, updateTime)
        db.getMaterialDao().insertMaterialMerged(merge)

        // 4. Setup WorkOrder and History
        db.getWorkOrderDao()
            .insertWorkOrder(WorkOrder(1L, "WO-1", 1L, "Addr", "Desc", false, updateTime))
        db.getWorkOrderDao().insertWorkOrderHistory(
            WorkOrderHistory(
                1L,
                1L,
                1L,
                0.0,
                0.0,
                0.0,
                null,
                false,
                updateTime
            )
        )

        // 5. Use BOTH materials in History
        db.getMaterialDao().insertWorkOrderHistoryMaterial(
            WorkOrderHistoryMaterial(
                1L,
                1L,
                1L,
                2.0,
                0,
                false,
                updateTime
            )
        )
        db.getMaterialDao().insertWorkOrderHistoryMaterial(
            WorkOrderHistoryMaterial(
                2L,
                1L,
                2L,
                3.0,
                1,
                false,
                updateTime
            )
        )

        // 6. Verify Summary
        val summary = workOrderDao.getWorkOrderMaterialsSummary(1L).getOrAwaitValue()

        assertEquals(1, summary.size)
        assertEquals("Master Material", summary[0].name)
        assertEquals(5.0, summary[0].quantity, 0.01)
        assertEquals(50.0, summary[0].totalAmount, 0.01)
        assertEquals(8.0, summary[0].cost, 0.01)
        assertEquals(10.0, summary[0].price, 0.01)
    }

    @Test
    fun testGetWorkOrderSummary_CalculatesTotalHours() = runBlocking {
        setupPrerequisites()
        db.getWorkOrderDao()
            .insertWorkOrder(WorkOrder(1L, "WO-1", 1L, "Addr", "Desc", false, updateTime))

        // History 1: 2 Reg, 1 OT
        db.getWorkOrderDao().insertWorkOrderHistory(
            WorkOrderHistory(
                1L,
                1L,
                1L,
                2.0,
                1.0,
                0.0,
                null,
                false,
                updateTime
            )
        )
        // History 2: 3 Reg, 0.5 Dbl
        db.getWorkOrderDao().insertWorkOrderHistory(
            WorkOrderHistory(
                2L,
                1L,
                2L,
                3.0,
                0.0,
                0.5,
                null,
                false,
                updateTime
            )
        )

        val summary = workOrderDao.getWorkOrderSummary(1L).getOrAwaitValue()

        assertEquals(5.0, summary.totalRegHours, 0.01)
        assertEquals(1.0, summary.totalOtHours, 0.01)
        assertEquals(0.5, summary.totalDblOtHours, 0.01)
    }
}

// Utility to observe LiveData in tests
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 5,
    timeUnit: TimeUnit = TimeUnit.SECONDS
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            data = value
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }

    androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync {
        this.observeForever(observer)
    }

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}