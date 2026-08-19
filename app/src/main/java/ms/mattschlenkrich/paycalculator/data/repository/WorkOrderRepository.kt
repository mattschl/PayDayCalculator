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
    private val jobSpecDao = db.getJobSpecDao()
    private val areaDao = db.getAreaDao()

    suspend fun insertWorkOrder(workOrder: WorkOrder) {
        val existing = workOrderDao.findWorkOrderAnySync(
            workOrder.woNumber,
            workOrder.woEmployerId
        )
        if (existing != null) {
            val updated = workOrder.copy(
                workOrderId = existing.workOrderId,
                woDeleted = false
            )
            updateWorkOrder(
                updated.workOrderId,
                updated.woNumber,
                updated.woEmployerId,
                updated.woAddress,
                updated.woDescription,
                updated.woDeleted,
                updated.woUpdateTime
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
        if (existing != null && existing.woHistoryDeleted && !history.woHistoryDeleted) {
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
                    ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes.REG_HOURS.value -> totalReg += hours
                    ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes.OT_HOURS.value -> totalOt += hours
                    ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes.DBL_OT_HOURS.value -> totalDbl += hours
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
        if (history != null) {
            synchronizeWorkDate(history.woHistoryWorkDateId)
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
                    ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes.REG_HOURS.value -> totalReg += hours
                    ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes.OT_HOURS.value -> totalOt += hours
                    ms.mattschlenkrich.paycalculator.common.TimeWorkedTypes.DBL_OT_HOURS.value -> totalDbl += hours
                }
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
    ) {
        // Since there is a unique index, we check if one already exists (including deleted)
        // However, the DAO doesn't have a lookup by unique keys yet.
        // I'll use REPLACE strategy in DAO or check here.
        workPerformedDao.insertWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed)
    }

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

    suspend fun removeAllTimeWorkedFromWorkOrderHistory(historyId: Long, updateTime: String) =
        workOrderTimeDao.removeAllTimeWorkedFromWorkOrderHistory(historyId, updateTime)

    suspend fun deleteWorkDate(workDateId: Long, updateTime: String) {
        val histories = workOrderDao.getWorkOrderHistoriesByDateSync(workDateId)
        histories.forEach { history ->
            deleteWorkOrderHistory(history.woHistoryId, updateTime)
        }
        payDayDao.removeAllWorkDateExtras(workDateId, updateTime)
        payDayDao.deleteWorkDate(workDateId, updateTime)
    }

    // Specialized JobSpec delegations
    fun getJobSpecs() = jobSpecDao.getJobSpecsAll()
    suspend fun getJobSpecsAllSync() = jobSpecDao.getJobSpecsAllSync()
    fun searchJobSpecs(query: String) = jobSpecDao.searchJobSpecs(query)
    fun getJobSpec(jobSpecId: Long) = jobSpecDao.getJobSpec(jobSpecId)
    fun getJobSpecAndChildList(jobSpecId: Long) = jobSpecDao.getJobSpecAndChildList(jobSpecId)
    suspend fun insertJobSpec(jobSpec: ms.mattschlenkrich.paycalculator.data.entity.JobSpec) =
        jobSpecDao.insertJobSpec(jobSpec)

    suspend fun updateJobSpec(jobSpec: ms.mattschlenkrich.paycalculator.data.entity.JobSpec) =
        jobSpecDao.updateJobSpec(jobSpec)

    suspend fun deleteJobSpec(jobSpecId: Long, updateTime: String) =
        jobSpecDao.deleteJobSpec(jobSpecId, updateTime)

    suspend fun insertJobSpecMerged(jobSpecMerged: ms.mattschlenkrich.paycalculator.data.entity.JobSpecMerged) =
        jobSpecDao.insertJobSpecMerged(jobSpecMerged)

    suspend fun deleteJobSpecMerged(jobSpecMergedId: Long, updateTime: String) =
        jobSpecDao.deleteJobSpecMerged(jobSpecMergedId, updateTime)

    suspend fun updateJobSpecMerged(oldJobSpecId: Long, newJobSpecId: Long) =
        jobSpecDao.updateJobSpecMerged(oldJobSpecId, newJobSpecId)

    suspend fun insertWorkOrderJobSpec(workOrderJobSpec: ms.mattschlenkrich.paycalculator.data.entity.WorkOrderJobSpec) =
        jobSpecDao.insertWorkOrderJobSpec(workOrderJobSpec)

    suspend fun updateWorkOrderJobSpec(workOrderJobSpec: ms.mattschlenkrich.paycalculator.data.entity.WorkOrderJobSpec) =
        jobSpecDao.updateWorkOrderJobSpec(workOrderJobSpec)

    fun getWorkOrderJobSpecs(workOrderId: Long) = jobSpecDao.getWorkOrderJobSpecs(workOrderId)
    fun getWorkOrderJobSpec(workOrderJobSpecId: Long) =
        jobSpecDao.getWorkOrderJobSpec(workOrderJobSpecId)

    // Specialized Material delegations
    suspend fun insertMaterial(material: ms.mattschlenkrich.paycalculator.data.entity.Material) =
        materialDao.insertMaterial(material)

    suspend fun updateMaterial(material: ms.mattschlenkrich.paycalculator.data.entity.Material) =
        materialDao.updateMaterial(material)

    fun getMaterialsList() = materialDao.getMaterialsList()
    suspend fun getMaterialsListSync() = materialDao.getMaterialsListSync()
    fun getMaterialAndChildList(materialId: Long) = materialDao.getMaterialAndChildList(materialId)
    fun searchMaterials(query: String) = materialDao.searchMaterials(query)
    fun getMaterial(materialId: Long) = materialDao.getMaterial(materialId)
    suspend fun getMaterialSync(materialId: Long) = materialDao.getMaterialSync(materialId)
    suspend fun getMaterialSync(mName: String) = materialDao.getMaterialSync(mName)
    suspend fun insertMaterialMerged(materialMerged: ms.mattschlenkrich.paycalculator.data.entity.MaterialMerged) =
        materialDao.insertMaterialMerged(materialMerged)

    suspend fun deleteMaterialMerged(materialMergeId: Long, updateTime: String) =
        materialDao.deleteMaterialMerged(materialMergeId, updateTime)

    suspend fun deleteMaterial(materialId: Long, updateTime: String) =
        materialDao.deleteMaterial(materialId, updateTime)

    // Specialized WorkPerformed delegations
    suspend fun insertWorkPerformed(workPerformed: ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed) =
        workPerformedDao.insertWorkPerformed(workPerformed)

    suspend fun deleteWorkPerformed(workPerformedId: Long, updateTime: String) =
        workPerformedDao.deleteWorkPerformed(workPerformedId, updateTime)

    suspend fun deleteWorkPerformedMerged(workPerformedMergedId: Long, updateTime: String) =
        workPerformedDao.deleteWorkPerformedMerged(workPerformedMergedId, updateTime)

    fun getWorkPerformedAll() = workPerformedDao.getWorkPerformedAll()
    suspend fun getWorkPerformedAllSync() = workPerformedDao.getWorkPerformedAllSync()
    fun getWorkPerformedAndChildList(workPerformedId: Long) =
        workPerformedDao.getWorkPerformedAndChildList(workPerformedId)

    suspend fun insertWorkPerformedMerged(workPerformedMerged: ms.mattschlenkrich.paycalculator.data.entity.WorkPerformedMerged) =
        workPerformedDao.insertWorkPerformedMerged(workPerformedMerged)

    suspend fun updateWorkPerformedMerged(oldWorkPerformedId: Long, newWorkPerformedId: Long) =
        workPerformedDao.updateWorkPerformedMerged(oldWorkPerformedId, newWorkPerformedId)

    fun searchFromWorkPerformed(query: String) = workPerformedDao.searchFromWorkPerformed(query)
    suspend fun getWorkPerformedSync(description: String) =
        workPerformedDao.getWorkPerformedSync(description)

    fun getWorkPerformed(workPerformedId: Long) = workPerformedDao.getWorkPerformed(workPerformedId)
    suspend fun updateWorkPerformed(workPerformed: ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed) =
        workPerformedDao.updateWorkPerformed(workPerformed)

    // Specialized Area delegations
    suspend fun insertArea(area: ms.mattschlenkrich.paycalculator.data.entity.Areas) =
        areaDao.insertArea(area)

    suspend fun updateArea(area: ms.mattschlenkrich.paycalculator.data.entity.Areas) =
        areaDao.updateArea(area)

    fun getAreasList() = areaDao.getAreasList()
    suspend fun getAreasListSync() = areaDao.getAreasListSync()
    fun getArea(areaId: Long) = areaDao.getArea(areaId)
    fun searchAreas(query: String) = areaDao.searchAreas(query)
}