package ms.mattschlenkrich.paycalculator.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkOrderRepositoryTest {

    private lateinit var db: PayDatabase
    private lateinit var repository: WorkOrderRepository
    private val updateTime = "2024-01-01 12:00:00"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PayDatabase::class.java).build()
        repository = WorkOrderRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertTimeWorked_RecalculatesHistoryAndDateHours() = runBlocking {
        // 1. Setup prerequisite data
        val employer = Employers(
            1L,
            "Employer A",
            "Bi-Weekly",
            "2024-01-01",
            "Friday",
            0,
            7,
            31,
            false,
            updateTime
        )
        db.getEmployerDao().insertEmployer(employer)

        val payPeriod = PayPeriods(1L, "2024-01-15", 1L, false, updateTime)
        db.getPayDayDao().insertPayPeriod(payPeriod)

        // workDateId, wdPayPeriodId, wdEmployerId, wdCutoffDate, wdDate, wdRegHours, wdOtHours, wdDblOtHours, wdStatHours, wdNote, wdIsDeleted, wdUpdateTime
        val workDate = WorkDates(
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
        db.getPayDayDao().insertWorkDate(workDate)

        val workOrder = WorkOrder(1L, "WO-101", 1L, "Address", "Desc", false, updateTime)
        db.getWorkOrderDao().insertWorkOrder(workOrder)

        val history = WorkOrderHistory(1L, 1L, 1L, 0.0, 0.0, 0.0, "Note", false, updateTime)
        db.getWorkOrderDao().insertWorkOrderHistory(history)

        // 2. Insert time worked (2 hours Reg)
        val timeWorked = WorkOrderHistoryTimeWorked(
            1L,
            1L,
            1L,
            "08:00",
            "10:00",
            TimeWorkedTypes.REG_HOURS.value,
            false,
            updateTime
        )
        repository.insertTimeWorked(timeWorked)

        // 3. Verify history hours re-calculated
        val updatedHistory = db.getWorkOrderDao().getWorkOrderHistorySync(1L)
        assertEquals(2.0, updatedHistory?.woHistoryRegHours ?: 0.0, 0.01)

        // 4. Verify work date hours re-calculated
        val updatedWorkDate = db.getPayDayDao().getWorkDateSync(1L)
        assertEquals(2.0, updatedWorkDate?.wdRegHours ?: 0.0, 0.01)
    }

    @Test
    fun testDeleteWorkOrderHistory_CascadesToAssociatedData() = runBlocking {
        // 1. Setup data
        val employer = Employers(
            1L,
            "Employer A",
            "Bi-Weekly",
            "2024-01-01",
            "Friday",
            0,
            7,
            31,
            false,
            updateTime
        )
        db.getEmployerDao().insertEmployer(employer)
        val payPeriod = PayPeriods(1L, "2024-01-15", 1L, false, updateTime)
        db.getPayDayDao().insertPayPeriod(payPeriod)
        val workDate = WorkDates(
            1L,
            1L,
            1L,
            "2024-01-15",
            "2024-01-10",
            2.0,
            0.0,
            0.0,
            0.0,
            null,
            false,
            updateTime
        )
        db.getPayDayDao().insertWorkDate(workDate)
        val workOrder = WorkOrder(1L, "WO-101", 1L, "Address", "Desc", false, updateTime)
        db.getWorkOrderDao().insertWorkOrder(workOrder)
        val history = WorkOrderHistory(1L, 1L, 1L, 2.0, 0.0, 0.0, "Note", false, updateTime)
        db.getWorkOrderDao().insertWorkOrderHistory(history)

        // Add associated data
        db.getWorkOrderTimeDao().insertTimeWorked(
            WorkOrderHistoryTimeWorked(
                1L,
                1L,
                1L,
                "08:00",
                "10:00",
                TimeWorkedTypes.REG_HOURS.value,
                false,
                updateTime
            )
        )

        // 2. Delete history
        repository.deleteWorkOrderHistory(1L, updateTime)

        // 3. Verify everything is deleted (or marked deleted)
        val deletedHistory = db.getWorkOrderDao().getWorkOrderHistorySync(1L)
        assertTrue(deletedHistory == null || deletedHistory.woHistoryDeleted)

        val times = db.getWorkOrderTimeDao().getTimeWorkedForWorkOrderHistorySync(1L)
        assertTrue(times.isEmpty() || times.all { it.wohtIsDeleted })

        // 4. Verify WorkDate hours are zeroed out
        val updatedWorkDate = db.getPayDayDao().getWorkDateSync(1L)
        assertEquals(0.0, updatedWorkDate?.wdRegHours ?: 0.0, 0.01)
    }
}