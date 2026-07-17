package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryWorkPerformed

class WorkOrderRepository(private val db: PayDatabase) {
    private val workOrderDao = db.getWorkOrderDao()
    private val payDayDao = db.getPayDayDao()
    private val workOrderTimeDao = db.getWorkOrderTimeDao()
    private val workPerformedDao = db.getWorkPerformedDao()
    private val materialDao = db.getMaterialDao()

    suspend fun insertWorkOrder(workOrder: WorkOrder) = workOrderDao.insertWorkOrder(workOrder)

    suspend fun updateWorkOrder(
        workOrderId: Long,
        workOrderNumber: String,
        employerId: Long,
        address: String,
        description: String,
        isDeleted: Boolean,
        updateTime: String,
    ) = workOrderDao.updateWorkOrder(
        workOrderId, workOrderNumber, employerId, address, description, isDeleted, updateTime
    )

    suspend fun findWorkOrder(workOrderNum: String, employerId: Long) =
        workOrderDao.findWorkOrder(workOrderNum, employerId)

    fun getWorkOrdersByEmployerId(employerId: Long) =
        workOrderDao.getWorkOrdersByEmployerId(employerId)

    fun getUniqueAddresses(employerId: Long) =
        workOrderDao.getUniqueAddresses(employerId)

    fun searchWorkOrders(employerId: Long, query: String) =
        workOrderDao.searchWorkOrders(employerId, query)

    suspend fun insertWorkOrderHistory(history: WorkOrderHistory) {
        workOrderDao.insertWorkOrderHistory(history)
        synchronizeWorkDate(history.woHistoryWorkDateId)
    }

    suspend fun updateWorkOrderHistory(history: WorkOrderHistory) {
        workOrderDao.updateWorkOrderHistory(history)
        synchronizeWorkDate(history.woHistoryWorkDateId)
    }

    suspend fun getWorkOrderHistory(workOrderId: Long, workDateId: Long) =
        workOrderDao.getWorkOrderHistorySync(workOrderId, workDateId)

    suspend fun deleteWorkOrderHistory(historyId: Long, updateTime: String) =
        workOrderDao.deleteWorkOrderHistory(historyId, updateTime)

    fun getWorkOrderHistoriesByDate(workDateId: Long) =
        workOrderDao.getWorkOrderHistoriesByDate(workDateId)

    fun getWorkOrderHistory(historyId: Long) =
        workOrderDao.getWorkOrderHistory(historyId)

    fun getWorkOrderHistoryCombined(historyId: Long) =
        workOrderDao.getWorkOrderHistoryCombined(historyId)

    fun getWorkOrderSummary(workOrderId: Long) =
        workOrderDao.getWorkOrderSummary(workOrderId)

    fun getWorkOrderMaterialsSummary(workOrderId: Long) =
        workOrderDao.getWorkOrderMaterialsSummary(workOrderId)

    fun getWorkOrderWorkPerformedSummary(workOrderId: Long) =
        workOrderDao.getWorkOrderWorkPerformedSummary(workOrderId)

    suspend fun updateWorkOrderHistory(
        historyId: Long,
        regHours: Double,
        otHours: Double,
        dblOtHours: Double,
        updateTime: String
    ) {
        val history = workOrderDao.getWorkOrderHistorySync(historyId)
        if (history != null) {
            workOrderDao.updateWorkOrderHistory(
                historyId,
                history.woHistoryWorkOrderId,
                history.woHistoryWorkDateId,
                regHours,
                otHours,
                dblOtHours,
                history.woHistoryNote,
                history.woHistoryDeleted,
                updateTime
            )
        }
    }

    suspend fun updateWorkDate(
        workDateId: Long,
        regHours: Double,
        otHours: Double,
        dblOtHours: Double,
        updateTime: String
    ) {
        val workDate = payDayDao.getWorkDateSync(workDateId)
        if (workDate != null) {
            payDayDao.updateWorkDates(
                workDateId,
                workDate.wdPayPeriodId,
                workDate.wdEmployerId,
                workDate.wdCutoffDate,
                workDate.wdDate,
                regHours,
                otHours,
                dblOtHours,
                workDate.wdStatHours,
                workDate.wdIsDeleted,
                updateTime
            )
        }
    }

    suspend fun synchronizeWorkDate(dateId: Long) {
        val updateTime = DateFunctions().getCurrentUTCTimeAsString()
        val histories = workOrderDao.getWorkOrderHistoriesByDateSync(dateId)
        var dateReg = 0.0
        var dateOt = 0.0
        var dateDbl = 0.0
        for (h in histories) {
            dateReg += h.woHistoryRegHours
            dateOt += h.woHistoryOtHours
            dateDbl += h.woHistoryDblOtHours
        }
        updateWorkDate(dateId, dateReg, dateOt, dateDbl, updateTime)
    }

    suspend fun synchronizeHours(historyId: Long) {
        val updateTime = DateFunctions().getCurrentUTCTimeAsString()
        val times = workOrderTimeDao.getTimeWorkedForWorkOrderHistorySync(historyId)
        var totalReg = 0.0
        var totalOt = 0.0
        var totalDbl = 0.0

        for (time in times) {
            val hours = DateFunctions().getTimeWorked(time.wohtStartTime, time.wohtEndTime)
            when (time.wohtTimeType) {
                ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes.REG_HOURS.value -> totalReg += hours
                ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes.OT_HOURS.value -> totalOt += hours
                ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes.DBL_OT_HOURS.value -> totalDbl += hours
            }
        }

        updateWorkOrderHistory(historyId, totalReg, totalOt, totalDbl, updateTime)

        val history = workOrderDao.getWorkOrderHistorySync(historyId)
        if (history != null) {
            synchronizeWorkDate(history.woHistoryWorkDateId)
        }
    }

    suspend fun insertTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) {
        workOrderTimeDao.insertTimeWorked(timeWorked)
        synchronizeHours(timeWorked.wohtHistoryId)
    }

    suspend fun updateTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) {
        workOrderTimeDao.updateTimeWorked(timeWorked)
        synchronizeHours(timeWorked.wohtHistoryId)
    }

    suspend fun deleteTimeWorked(timeWorkedId: Long, updateTime: String) {
        val time = workOrderTimeDao.getTimeWorkedSync(timeWorkedId)
        workOrderTimeDao.deleteTimeWorked(timeWorkedId, updateTime)
        if (time != null) {
            synchronizeHours(time.wohtHistoryId)
        }
    }

    fun getTimeWorkedPerDay(workDateId: Long) =
        workOrderTimeDao.getTimeWorkedPerDay(workDateId)

    fun getTimeWorkedForWorkOrderHistory(historyId: Long) =
        workOrderTimeDao.getTimeWorkedForWorkOrderHistory(historyId)

    fun getWorkOrderHistoriesByWorkOrder(workOrderId: Long) =
        workOrderDao.getWorkOrderHistoriesByWorkOrder(workOrderId)

    fun getWorkPerformedHistoryById(historyWorkPerformedId: Long) =
        workPerformedDao.getWorkPerformedHistoryById(historyWorkPerformedId)

    suspend fun insertWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    ) = workPerformedDao.insertWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed)

    suspend fun updateWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    ) = workPerformedDao.updateWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed)

    suspend fun removeAllWorkPerformedFromWorkOrderHistory(historyId: Long, updateTime: String) =
        workPerformedDao.removeAllWorkPerformedFromWorkOrderHistory(historyId, updateTime)

    suspend fun deleteWorkOrderHistoryWorkPerformed(
        historyWorkPerformedId: Long,
        updateTime: String
    ) =
        workPerformedDao.deleteWorkOrderHistoryWorkPerformed(historyWorkPerformedId, updateTime)

    fun getWorkPerformedCombinedByWorkOrderHistory(historyId: Long) =
        workPerformedDao.getWorkPerformedByWorkOrderHistory(historyId)

    suspend fun updateMaterialMerged(oldMaterialID: Long, newMaterialID: Long, updateTime: String) =
        materialDao.updateMaterialMerged(oldMaterialID, newMaterialID, updateTime)

    suspend fun insertWorkOrderHistoryMaterial(
        workOrderHistoryMaterial: WorkOrderHistoryMaterial
    ) = materialDao.insertWorkOrderHistoryMaterial(workOrderHistoryMaterial)

    suspend fun updateWorkOrderHistoryMaterial(
        workOrderHistoryMaterial: WorkOrderHistoryMaterial
    ) = materialDao.updateWorkOrderHistoryMaterial(workOrderHistoryMaterial)

    suspend fun deleteWorkOrderHistoryMaterial(
        historyMaterialId: Long, updateTime: String
    ) = materialDao.deleteWorkOrderHistoryMaterial(historyMaterialId, updateTime)

    fun getMaterialsByHistory(historyId: Long) =
        materialDao.getMaterialsByHistory(historyId)

    suspend fun getWorkOrderHistoryMaterialCombined(woHistoryMaterialId: Long) =
        materialDao.getWorkOrderHistoryMaterialCombined(woHistoryMaterialId)

    suspend fun removeAllMaterialsFromWorkOrderHistory(historyId: Long, updateTime: String) =
        materialDao.removeAllMaterialsFromWorkOrderHistory(historyId, updateTime)
}