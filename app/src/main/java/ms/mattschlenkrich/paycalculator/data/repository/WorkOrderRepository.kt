package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryExpense
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderPictures

class WorkOrderRepository(db: PayDatabase) {
    private val workOrderDao = db.getWorkOrderDao()
    private val payDayDao = db.getPayDayDao()
    private val workOrderTimeDao = db.getWorkOrderTimeDao()
    private val workPerformedDao = db.getWorkPerformedDao()
    private val materialDao = db.getMaterialDao()
    private val workOrderPictureDao = db.getWorkOrderPictureDao()

    suspend fun insertWorkOrder(workOrder: WorkOrder) {
        val existing = workOrderDao.findWorkOrderAnySync(
            workOrder.woNumber,
            workOrder.woEmployerId,
        )
        if (existing != null) {
            val updated = workOrder.copy(
                workOrderId = existing.workOrderId,
                woDeleted = false,
            )
            updateWorkOrder(
                updated.workOrderId,
                updated.woNumber,
                updated.woEmployerId,
                updated.woAddress,
                updated.woDescription,
                updated.woDeleted,
                updated.woUpdateTime,
            )
        } else {
            workOrderDao.insertWorkOrder(workOrder)
        }
    }

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

    fun getWorkOrder(workOrderId: Long) = workOrderDao.getWorkOrder(workOrderId)

    suspend fun findWorkOrder(workOrderNum: String, employerId: Long) =
        workOrderDao.findWorkOrder(workOrderNum, employerId)

    fun getWorkOrdersByEmployerId(employerId: Long) =
        workOrderDao.getWorkOrdersByEmployerId(employerId)

    fun getUniqueAddresses(employerId: Long) =
        workOrderDao.getUniqueAddresses(employerId)

    fun searchWorkOrders(employerId: Long, query: String) =
        workOrderDao.searchWorkOrders(employerId, query)

    suspend fun insertWorkOrderHistory(history: WorkOrderHistory) {
        val existing = workOrderDao.getWorkOrderHistoryAnySync(
            history.woHistoryWorkOrderId,
            history.woHistoryWorkDateId
        )
        if (existing != null) {
            val updated = history.copy(
                woHistoryId = existing.woHistoryId,
                woHistoryDeleted = false
            )
            workOrderDao.updateWorkOrderHistory(updated)
            val updateTime = DateFunctions().getCurrentUTCTimeAsString()
            workPerformedDao.removeAllWorkPerformedFromWorkOrderHistory(
                existing.woHistoryId,
                updateTime
            )
            materialDao.removeAllMaterialsFromWorkOrderHistory(
                existing.woHistoryId,
                updateTime
            )
            workOrderTimeDao.removeAllTimeWorkedFromWorkOrderHistory(
                existing.woHistoryId,
                updateTime
            )
        } else {
            workOrderDao.insertWorkOrderHistory(history)
        }
        synchronizeWorkDate(history.woHistoryWorkDateId)
    }

    suspend fun updateWorkOrderHistory(history: WorkOrderHistory) {
        val existing = workOrderDao.getWorkOrderHistoryByIdAnySync(history.woHistoryId)
        if (existing != null && ((existing.woHistoryDeleted && !history.woHistoryDeleted))) {
            val updateTime = DateFunctions().getCurrentUTCTimeAsString()
            workPerformedDao.removeAllWorkPerformedFromWorkOrderHistory(
                history.woHistoryId,
                updateTime
            )
            materialDao.removeAllMaterialsFromWorkOrderHistory(
                history.woHistoryId,
                updateTime
            )
            workOrderTimeDao.removeAllTimeWorkedFromWorkOrderHistory(
                history.woHistoryId,
                updateTime
            )
        }
        val times = workOrderTimeDao.getTimeWorkedForWorkOrderHistorySync(history.woHistoryId)
        val finalHistory = if (times.isNotEmpty()) {
            var totalReg = 0.0
            var totalOt = 0.0
            var totalDbl = 0.0
            for (time in times) {
                val hours = DateFunctions().getTimeWorked(time.wohtStartTime, time.wohtEndTime)
                when (time.wohtTimeType) {
                    TimeWorkedTypes.REG_HOURS.value -> totalReg += hours
                    TimeWorkedTypes.OT_HOURS.value -> totalOt += hours
                    TimeWorkedTypes.DBL_OT_HOURS.value -> totalDbl += hours
                }
            }
            history.copy(
                woHistoryRegHours = totalReg,
                woHistoryOtHours = totalOt,
                woHistoryDblOtHours = totalDbl
            )
        } else {
            history
        }
        workOrderDao.updateWorkOrderHistory(finalHistory)
        synchronizeWorkDate(finalHistory.woHistoryWorkDateId)
    }

    suspend fun getWorkOrderHistory(workOrderId: Long, workDateId: Long) =
        workOrderDao.getWorkOrderHistorySync(workOrderId, workDateId)

    suspend fun deleteWorkOrderHistory(historyId: Long, updateTime: String) {
        val history = workOrderDao.getWorkOrderHistorySync(historyId)
        workOrderDao.deleteWorkOrderHistory(historyId, updateTime)
        workPerformedDao.removeAllWorkPerformedFromWorkOrderHistory(historyId, updateTime)
        materialDao.removeAllMaterialsFromWorkOrderHistory(historyId, updateTime)
        workOrderTimeDao.removeAllTimeWorkedFromWorkOrderHistory(historyId, updateTime)
        history?.let {
            synchronizeWorkDate(it.woHistoryWorkDateId)
        }
    }

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

    fun getWorkOrderJobSpecsSummary(workOrderId: Long) =
        workOrderDao.getWorkOrderJobSpecsSummary(workOrderId)

    fun getWorkOrderExpensesSummary(workOrderId: Long) =
        workOrderDao.getWorkOrderExpensesSummary(workOrderId)

    fun getWorkOrderExpensesAll(workOrderId: Long) =
        workOrderDao.getWorkOrderExpensesAll(workOrderId)

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

        if (times.isNotEmpty()) {
            for (time in times) {
                val hours = DateFunctions().getTimeWorked(time.wohtStartTime, time.wohtEndTime)
                when (time.wohtTimeType) {
                    TimeWorkedTypes.REG_HOURS.value -> totalReg += hours
                    TimeWorkedTypes.OT_HOURS.value -> totalOt += hours
                    TimeWorkedTypes.DBL_OT_HOURS.value -> totalDbl += hours
                }
            }
        }

        updateWorkOrderHistory(historyId, totalReg, totalOt, totalDbl, updateTime)

        val history = workOrderDao.getWorkOrderHistorySync(historyId)
        history?.let {
            synchronizeWorkDate(it.woHistoryWorkDateId)
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

    suspend fun deleteWorkDate(workDateId: Long, updateTime: String) {
        val histories = workOrderDao.getWorkOrderHistoriesByDateSync(workDateId)
        histories.forEach { history ->
            deleteWorkOrderHistory(history.woHistoryId, updateTime)
        }
        payDayDao.removeAllWorkDateExtras(workDateId, updateTime)
        payDayDao.deleteWorkDate(workDateId, updateTime)
    }

    suspend fun insertWorkOrderHistoryExpense(expense: WorkOrderHistoryExpense) =
        workOrderDao.insertWorkOrderHistoryExpense(expense)

    suspend fun updateWorkOrderHistoryExpense(expense: WorkOrderHistoryExpense) =
        workOrderDao.updateWorkOrderHistoryExpense(expense)

    suspend fun deleteWorkOrderHistoryExpense(expenseId: Long, updateTime: String) =
        workOrderDao.deleteWorkOrderHistoryExpense(expenseId, updateTime)

    fun getExpensesByHistory(historyId: Long) =
        workOrderDao.getExpensesByHistory(historyId)

    fun getPicturesByWorkOrderId(workOrderId: Long) =
        workOrderPictureDao.getPicturesByWorkOrderId(workOrderId)

    fun getPicturesForHistory(historyId: Long) =
        workOrderPictureDao.getPicturesForHistory(historyId)

    fun getPicturesForExpense(expenseId: Long) =
        workOrderPictureDao.getPicturesForExpense(expenseId)

    suspend fun insertPicture(picture: WorkOrderPictures) =
        workOrderPictureDao.insertPicture(picture)

    suspend fun updatePicture(picture: WorkOrderPictures) =
        workOrderPictureDao.updatePicture(picture)

    suspend fun deletePictureById(pictureId: Long) =
        workOrderPictureDao.deletePictureById(pictureId)
}