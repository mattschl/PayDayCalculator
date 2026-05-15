package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.Areas
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec
import ms.mattschlenkrich.paycalculator.data.entity.JobSpecMerged
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.MaterialMerged
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformedMerged
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryMaterialCombined

class WorkOrderRepository(private val db: PayDatabase) {
    suspend fun insertWorkOrder(workOrder: WorkOrder) =
        db.getWorkOrderDao().insertWorkOrder(workOrder)

    suspend fun updateWorkOrder(
        workOrderId: Long,
        workOrderNumber: String,
        employerId: Long,
        address: String,
        description: String,
        isDeleted: Boolean,
        updateTime: String,
    ) = db.getWorkOrderDao().updateWorkOrder(
        workOrderId, workOrderNumber, employerId, address, description, isDeleted, updateTime
    )

    suspend fun findWorkOrder(workOrderNum: String, employerId: Long) =
        db.getWorkOrderDao().findWorkOrder(workOrderNum, employerId)

    fun getWorkOrdersByEmployerId(employerId: Long) =
        db.getWorkOrderDao().getWorkOrdersByEmployerId(employerId)

    fun searchWorkOrders(employerId: Long, query: String) =
        db.getWorkOrderDao().searchWorkOrders(employerId, query)

    suspend fun insertWorkOrderHistory(history: WorkOrderHistory) {
        db.getWorkOrderDao().insertWorkOrderHistory(history)
        synchronizeWorkDate(history.woHistoryWorkDateId)
    }

    suspend fun updateWorkOrderHistory(history: WorkOrderHistory) {
        db.getWorkOrderDao().updateWorkOrderHistory(history)
        synchronizeWorkDate(history.woHistoryWorkDateId)
    }

    suspend fun getWorkOrderHistory(workOrderId: Long, workDateId: Long) =
        db.getWorkOrderDao().getWorkOrderHistory(workOrderId, workDateId)

    suspend fun deleteWorkOrderHistory(historyId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkOrderHistory(historyId, updateTime)

    fun getWorkOrderHistoriesByDate(workDateId: Long) =
        db.getWorkOrderDao().getWorkOrderHistoriesByDate(workDateId)

    fun getWorkOrderHistoriesById(historyId: Long) =
        db.getWorkOrderDao().getWorkOrderHistoriesById(historyId)

    fun getWorkOrderHistoryCombined(historyId: Long) =
        db.getWorkOrderDao().getWorkOrderHistoryCombined(historyId)

    suspend fun updateWorkOrderHistory(
        historyId: Long,
        regHours: Double,
        otHours: Double,
        dblOtHours: Double,
        updateTime: String
    ) {
        val history = db.getWorkOrderDao().getWorkOrderHistorySync(historyId)
        if (history != null) {
            db.getWorkOrderDao().updateWorkOrderHistory(
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
        val workDate = db.getPayDayDao().getWorkDateSync(workDateId)
        if (workDate != null) {
            db.getPayDayDao().updateWorkDates(
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
        val histories = db.getWorkOrderDao().getWorkOrderHistoriesByDateSync(dateId)
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
        val times = db.getWorkOrderDao().getTimeWorkedForWorkOrderHistorySync(historyId)
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

        val history = db.getWorkOrderDao().getWorkOrderHistorySync(historyId)
        if (history != null) {
            synchronizeWorkDate(history.woHistoryWorkDateId)
        }
    }

    suspend fun insertTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) {
        db.getWorkOrderDao().insertTimeWorked(timeWorked)
        synchronizeHours(timeWorked.wohtHistoryId)
    }

    suspend fun updateTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) {
        db.getWorkOrderDao().updateTimeWorked(timeWorked)
        synchronizeHours(timeWorked.wohtHistoryId)
    }

    suspend fun deleteTimeWorked(timeWorkedId: Long, updateTime: String) {
        val time = db.getWorkOrderDao().getTimeWorkedSync(timeWorkedId)
        db.getWorkOrderDao().deleteTimeWorked(timeWorkedId, updateTime)
        if (time != null) {
            synchronizeHours(time.wohtHistoryId)
        }
    }

    fun getTimeWorkedPerDay(workDateId: Long) =
        db.getWorkOrderDao().getTimeWorkedPerDay(workDateId)

    fun getTimeWorkedForWorkOrderHistory(historyId: Long) =
        db.getWorkOrderDao().getTimeWorkedForWorkOrderHistory(historyId)

    fun getWorkOrderHistoriesByWorkOrder(workOrderId: Long) =
        db.getWorkOrderDao().getWorkOrderHistoriesByWorkOrder(workOrderId)

    fun getWorkPerformedHistoryById(historyWorkPerformedId: Long) =
        db.getWorkOrderDao().getWorkPerformedHistoryById(historyWorkPerformedId)

    fun getWorkOrderHistory(historyID: Long) = db.getWorkOrderDao().getWorkOrderHistory(historyID)

    fun getJobSpec(jobSpecId: Long) = db.getWorkOrderDao().getJobSpec(jobSpecId)

    fun getJobSpecAndChildList(jobSpecId: Long) =
        db.getWorkOrderDao().getJobSpecAndChildList(jobSpecId)

    suspend fun insertJobSpecMerged(jobSpecMerged: JobSpecMerged) =
        db.getWorkOrderDao().insertJobSpecMerged(jobSpecMerged)

    suspend fun deleteJobSpecMerged(jobSpecMergedId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteJobSpecMerged(jobSpecMergedId, updateTime)

    suspend fun updateJobSpecMerged(oldJobSpecId: Long, newJobSpecId: Long) =
        db.getWorkOrderDao().updateJobSpecMerged(oldJobSpecId, newJobSpecId)

    suspend fun insertJobSpec(jobSpec: JobSpec) = db.getWorkOrderDao().insertJobSpec(jobSpec)

    suspend fun updateJobSpec(jobSpec: JobSpec) = db.getWorkOrderDao().updateJobSpec(jobSpec)

    suspend fun deleteJobSpec(jobSpecId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteJobSpec(jobSpecId, updateTime)

    fun getJobSpecs() = db.getWorkOrderDao().getJobSpecsAll()

    suspend fun getJobSpecsAllSync() = db.getWorkOrderDao().getJobSpecsAllSync()

    fun searchJobSpecs(query: String) = db.getWorkOrderDao().searchJobSpecs(query)

    suspend fun insertWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) =
        db.getWorkOrderDao().insertWorkOrderJobSpec(workOrderJobSpec)

    suspend fun updateWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) =
        db.getWorkOrderDao().updateWorkOrderJobSpec(workOrderJobSpec)

    fun getWorkOrderJobSpecs(workOrderId: Long) =
        db.getWorkOrderDao().getWorkOrderJobSpecs(workOrderId)

    fun getWorkOrderJobSpec(workOrderJobSpecId: Long) =
        db.getWorkOrderDao().getWorkOrderJobSpec(workOrderJobSpecId)

    suspend fun insertWorkPerformed(workPerformed: WorkPerformed) =
        db.getWorkOrderDao().insertWorkPerformed(workPerformed)

    suspend fun deleteWorkPerformed(workPerformedId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkPerformed(workPerformedId, updateTime)

    suspend fun deleteWorkPerformedMerged(workPerformedMergedId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkPerformedMerged(workPerformedMergedId, updateTime)

    fun getWorkPerformedAll() = db.getWorkOrderDao().getWorkPerformedAll()

    suspend fun getWorkPerformedAllSync() = db.getWorkOrderDao().getWorkPerformedAllSync()

    fun getWorkPerformedAndChildList(workPerformedId: Long) =
        db.getWorkOrderDao().getWorkPerformedAndChildList(workPerformedId)

    suspend fun insertWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged) =
        db.getWorkOrderDao().insertWorkPerformedMerged(workPerformedMerged)

    suspend fun updateWorkPerformedMerged(oldWorkPerformedId: Long, newWorkPerformedId: Long) =
        db.getWorkOrderDao().updateWorkPerformedMerged(oldWorkPerformedId, newWorkPerformedId)

    fun searchFromWorkPerformed(query: String) = db.getWorkOrderDao().searchFromWorkPerformed(query)

    suspend fun getWorkPerformedSync(description: String) =
        db.getWorkOrderDao().getWorkPerformedSync(description)

    fun getWorkPerformed(workPerformedId: Long) =
        db.getWorkOrderDao().getWorkPerformed(workPerformedId)

    suspend fun updateWorkPerformed(workPerformed: WorkPerformed) =
        db.getWorkOrderDao().updateWorkPerformed(workPerformed)

    suspend fun insertWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    ) = db.getWorkOrderDao().insertWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed)

    suspend fun updateWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    ) = db.getWorkOrderDao().updateWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed)

    suspend fun removeAllWorkPerformedFromWorkOrderHistory(historyId: Long, updateTime: String) =
        db.getWorkOrderDao().removeAllWorkPerformedFromWorkOrderHistory(historyId, updateTime)

    suspend fun deleteWorkOrderHistoryWorkPerformed(
        historyWorkPerformedId: Long,
        updateTime: String
    ) =
        db.getWorkOrderDao().deleteWorkOrderHistoryWorkPerformed(historyWorkPerformedId, updateTime)

    fun getWorkPerformedCombinedByWorkOrderHistory(historyId: Long) =
        db.getWorkOrderDao().getWorkPerformedByWorkOrderHistory(historyId)

    suspend fun insertMaterial(material: Material) = db.getWorkOrderDao().insertMaterial(material)

    suspend fun updateMaterial(material: Material) = db.getWorkOrderDao().updateMaterial(material)

    fun getMaterialsList() = db.getWorkOrderDao().getMaterialsList()

    suspend fun getMaterialsListSync() = db.getWorkOrderDao().getMaterialsListSync()

    fun getMaterialAndChildList(materialId: Long) =
        db.getWorkOrderDao().getMaterialAndChildList(materialId)

    fun searchMaterials(query: String) = db.getWorkOrderDao().searchMaterials(query)

    fun getMaterial(materialId: Long) = db.getWorkOrderDao().getMaterial(materialId)

    suspend fun getMaterialSync(materialId: Long) = db.getWorkOrderDao().getMaterialSync(materialId)

    suspend fun getMaterialSync(mName: String) = db.getWorkOrderDao().getMaterialSync(mName)

    suspend fun updateMaterialMerged(oldMaterialID: Long, newMaterialID: Long, updateTime: String) =
        db.getWorkOrderDao().updateMaterialMerged(oldMaterialID, newMaterialID, updateTime)

    suspend fun deleteMaterialMerged(materialMergedId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteMaterialMerged(materialMergedId, updateTime)

    suspend fun insertMaterialMerged(materialMerged: MaterialMerged) =
        db.getWorkOrderDao().insertMaterialMerged(materialMerged)

    suspend fun deleteMaterial(materialId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteMaterial(materialId, updateTime)

    suspend fun insertWorkOrderHistoryMaterial(
        workOrderHistoryMaterial: WorkOrderHistoryMaterial
    ) = db.getWorkOrderDao().insertWorkOrderHistoryMaterial(workOrderHistoryMaterial)

    suspend fun updateWorkOrderHistoryMaterial(
        workOrderHistoryMaterial: WorkOrderHistoryMaterial
    ) = db.getWorkOrderDao().updateWorkOrderHistoryMaterial(workOrderHistoryMaterial)

    suspend fun deleteWorkOrderHistoryMaterial(
        historyMaterialId: Long, updateTime: String
    ) = db.getWorkOrderDao().deleteWorkOrderHistoryMaterial(historyMaterialId, updateTime)

    fun getMaterialsByHistory(historyId: Long) =
        db.getWorkOrderDao().getMaterialsByHistory(historyId)

    suspend fun getWorkOrderHistoryMaterialCombined(woHistoryMaterialId: Long): WorkOrderHistoryMaterialCombined =
        db.getWorkOrderDao().getWorkOrderHistoryMaterialCombined(woHistoryMaterialId)

    suspend fun removeAllMaterialsFromWorkOrderHistory(historyId: Long, updateTime: String) =
        db.getWorkOrderDao().removeAllMaterialsFromWorkOrderHistory(historyId, updateTime)

    suspend fun insertArea(area: Areas) = db.getWorkOrderDao().insertArea(area)

    suspend fun updateArea(area: Areas) = db.getWorkOrderDao().updateArea(area)

    fun getAreasList() = db.getWorkOrderDao().getAreasList()

    suspend fun getAreasListSync() = db.getWorkOrderDao().getAreasListSync()

    fun getArea(areaId: Long) = db.getWorkOrderDao().getArea(areaId)

    fun searchAreas(query: String) = db.getWorkOrderDao().searchAreas(query)
}