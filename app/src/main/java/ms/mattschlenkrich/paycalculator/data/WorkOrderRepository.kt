package ms.mattschlenkrich.paycalculator.data

import ms.mattschlenkrich.paycalculator.common.DateFunctions

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

//    suspend fun deleteWorkOrder(workOrderId: Long, updateTime: String) =
//        db.getWorkOrderDao().deleteWorkOrder(workOrderId, updateTime)
//
//    suspend fun deleteWorkOrder(workOrderNumber: String, updateTime: String) =
//        db.getWorkOrderDao().deleteWorkOrder(workOrderNumber, updateTime)

    fun getWorkOrder(workOrderId: Long) = db.getWorkOrderDao().getWorkOrder(workOrderId)

    suspend fun findWorkOrder(workOrderNum: String, employerId: Long) =
        db.getWorkOrderDao().findWorkOrder(workOrderNum, employerId)


    fun getWorkOrder(workOrderNum: String) = db.getWorkOrderDao().getWorkOrder(workOrderNum)

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

    suspend fun updateWorkOrderHistory(
        historyID: Long,
        workOrderId: Long,
        workDateId: Long,
        regHours: Double,
        otHours: Double,
        dblOtHours: Double,
        note: String?,
        isDeleted: Boolean,
        updateTime: String
    ) = db.getWorkOrderDao().updateWorkOrderHistory(
        historyID,
        workOrderId,
        workDateId,
        regHours,
        otHours,
        dblOtHours,
        note,
        isDeleted,
        updateTime
    )

    suspend fun deleteWorkOrderHistory(historyId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkOrderHistory(historyId, updateTime)

    suspend fun deleteWorkOrderHistory(historyId: Long) =
        db.getWorkOrderDao().deleteWorkOrderHistory(
            historyId,
            DateFunctions().getCurrentUTCTimeAsString()
        )

    fun getWorkOrderHistoriesByDate(workDateId: Long) =
        db.getWorkOrderDao().getWorkOrderHistoriesByDate(workDateId)

    fun getWorkOrderHistoriesById(historyId: Long) =
        db.getWorkOrderDao().getWorkOrderHistoriesById(historyId)

    suspend fun getWorkOrderHistoryWithDatedById(historyID: Long) =
        db.getWorkOrderDao().getWorkOrderHistoryWithDateById(historyID)

    fun getWorkOrderHistoryCombined(historyId: Long) =
        db.getWorkOrderDao().getWorkOrderHistoryCombined(historyId)

    suspend fun getTimeWorkedSync(timeWorkedId: Long) =
        db.getWorkOrderDao().getTimeWorkedSync(timeWorkedId)

    suspend fun updateWorkOrderHistory(
        historyId: Long,
        regHours: Double,
        otHours: Double,
        dblOtHours: Double,
        updateTime: String
    ) {
        val history = db.getWorkOrderDao().getWorkOrderHistorySync(historyId)
        if (history != null) {
            if (regHours > history.woHistoryRegHours ||
                otHours > history.woHistoryOtHours ||
                dblOtHours > history.woHistoryDblOtHours
            ) {
                db.getWorkOrderDao().updateWorkOrderHistory(
                    historyId,
                    history.woHistoryWorkOrderId,
                    history.woHistoryWorkDateId,
                    maxOf(regHours, history.woHistoryRegHours),
                    maxOf(otHours, history.woHistoryOtHours),
                    maxOf(dblOtHours, history.woHistoryDblOtHours),
                    history.woHistoryNote,
                    history.woHistoryDeleted,
                    updateTime
                )
            }
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
            if (regHours > workDate.wdRegHours ||
                otHours > workDate.wdOtHours ||
                dblOtHours > workDate.wdDblOtHours
            ) {
                db.getPayDayDao().updateWorkDates(
                    workDateId,
                    workDate.wdPayPeriodId,
                    workDate.wdEmployerId,
                    workDate.wdCutoffDate,
                    workDate.wdDate,
                    maxOf(regHours, workDate.wdRegHours),
                    maxOf(otHours, workDate.wdOtHours),
                    maxOf(dblOtHours, workDate.wdDblOtHours),
                    workDate.wdStatHours,
                    workDate.wdIsDeleted,
                    updateTime
                )
            }
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

//    suspend fun deleteTimeWorked(timeWorked: WorkOrderHistoryTimeWorked) =
//        db.getWorkOrderDao().deleteTimeWorked(timeWorked)

    fun getTimeWorkedPerDay(workDateId: Long) =
        db.getWorkOrderDao().getTimeWorkedPerDay(workDateId)

    fun getTimeWorkedForWorkOrderHistory(historyId: Long) =
        db.getWorkOrderDao().getTimeWorkedForWorkOrderHistory(historyId)

    fun getWorkOrderHistoriesByWorkOrder(workOrderId: Long) =
        db.getWorkOrderDao().getWorkOrderHistoriesByWorkOrder(workOrderId)

    fun getWorkPerformedHistoryById(historyWorkPerformedId: Long) =
        db.getWorkOrderDao().getWorkPerformedHistoryById(historyWorkPerformedId)

    suspend fun getWorkPerformedHistoryByIdSync(historyWorkPerformedId: Long) =
        db.getWorkOrderDao().getWorkPerformedHistoryByIdSync(historyWorkPerformedId)

    fun getWorkOrderHistory(historyID: Long) = db.getWorkOrderDao().getWorkOrderHistory(historyID)

    suspend fun deleteWorkOrderHistoryByWorkDateId(workDateId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkOrderHistoryByWorkDateId(workDateId, updateTime)

    fun getJobSpec(jobSpecId: Long) = db.getWorkOrderDao().getJobSpec(jobSpecId)

    fun getJobSpecAndChildList(jobSpecId: Long) =
        db.getWorkOrderDao().getJobSpecAndChildList(jobSpecId)

    suspend fun insertJobSpecMerged(jobSpecMerged: JobSpecMerged) =
        db.getWorkOrderDao().insertJobSpecMerged(jobSpecMerged)

    suspend fun deleteJobSpecMerged(jobSpecMergedId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteJobSpecMerged(jobSpecMergedId, updateTime)

    suspend fun getJobSpecSync(jsName: String) = db.getWorkOrderDao().getJobSpecSync(jsName)

    suspend fun insertJobSpec(jobSpec: JobSpec) = db.getWorkOrderDao().insertJobSpec(jobSpec)

    suspend fun updateJobSpec(jobSpec: JobSpec) = db.getWorkOrderDao().updateJobSpec(jobSpec)

    fun getJobSpecs() = db.getWorkOrderDao().getJobSpecsAll()

    suspend fun getJobSpecsAllSync() = db.getWorkOrderDao().getJobSpecsAllSync()

    fun searchJobSpecs(query: String) = db.getWorkOrderDao().searchJobSpecs(query)

    suspend fun insertWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) =
        db.getWorkOrderDao().insertWorkOrderJobSpec(workOrderJobSpec)

    suspend fun updateWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) =
        db.getWorkOrderDao().updateWorkOrderJobSpec(workOrderJobSpec)

    suspend fun deleteWorkOrderJobSpec(workOrderJobSpecId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkOrderJobSpec(workOrderJobSpecId, updateTime)

//    suspend fun deleteWorkOrderJobSpec(workOrderJobSpecId: Long) =
//        db.getWorkOrderDao().deleteWorkOrderJobSpec(workOrderJobSpecId)

    fun getWorkOrderJobSpecs(workOrderId: Long) =
        db.getWorkOrderDao().getWorkOrderJobSpecs(workOrderId)

    fun getWorkOrderJobSpec(workOrderJobSpecId: Long) =
        db.getWorkOrderDao().getWorkOrderJobSpec(workOrderJobSpecId)

    suspend fun getWorkOrderJobSpecSync(workOrderJobSpecId: Long) =
        db.getWorkOrderDao().getWorkOrderJobSpecSync(workOrderJobSpecId)

    suspend fun insertWorkPerformed(workPerformed: WorkPerformed) =
        db.getWorkOrderDao().insertWorkPerformed(workPerformed)

    suspend fun deleteWorkPerformed(workPerformedId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkPerformed(workPerformedId, updateTime)

    suspend fun deleteWorkPerformedMerged(workPerformedMergedId: Long, updateTime: String) =
        db.getWorkOrderDao().deleteWorkPerformedMerged(workPerformedMergedId, updateTime)


    fun getWorkPerformedAll() = db.getWorkOrderDao().getWorkPerformedAll()

    suspend fun getWorkPerformedAllSync() = db.getWorkOrderDao().getWorkPerformedAllSync()

    fun getWorkPerformedChildren(workPerformedId: Long) =
        db.getWorkOrderDao().getWorkPerformedChildren(workPerformedId)

    fun getWorkPerformedAndChildList(workPerformedId: Long) =
        db.getWorkOrderDao().getWorkPerformedAndChildList(workPerformedId)

    suspend fun insertWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged) =
        db.getWorkOrderDao().insertWorkPerformedMerged(workPerformedMerged)

    suspend fun updateWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged) =
        db.getWorkOrderDao().updateWorkPerformedMerged(workPerformedMerged)

    suspend fun updateWorkPerformedMerged(oldWorkPerformedId: Long, newWorkPerformedId: Long) =
        db.getWorkOrderDao().updateWorkPerformedMerged(oldWorkPerformedId, newWorkPerformedId)

    fun searchFromWorkPerformed(query: String) = db.getWorkOrderDao().searchFromWorkPerformed(query)

    fun getWorkPerformed(description: String) = db.getWorkOrderDao().getWorkPerformed(description)

    suspend fun getWorkPerformedSync(description: String) =
        db.getWorkOrderDao().getWorkPerformedSync(description)

    fun getWorkPerformed(workPerformedId: Long) =
        db.getWorkOrderDao().getWorkPerformed(workPerformedId)

    suspend fun getWorkPerformedSync(workPerformedId: Long) =
        db.getWorkOrderDao().getWorkPerformedSync(workPerformedId)

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

    suspend fun getWorkPerformedByWorkOrderHistorySync(historyId: Long) =
        db.getWorkOrderDao().getWorkPerformedByWorkOrderHistorySync(historyId)

    suspend fun insertMaterial(material: Material) = db.getWorkOrderDao().insertMaterial(material)

    suspend fun updateMaterial(material: Material) = db.getWorkOrderDao().updateMaterial(material)

    fun getMaterialsList() = db.getWorkOrderDao().getMaterialsList()

    fun getMaterialsChildren(materialId: Long) =
        db.getWorkOrderDao().getMaterialsChildren(materialId)

    fun getMaterialAndChildList(materialId: Long) =
        db.getWorkOrderDao().getMaterialAndChildList(materialId)

    fun searchMaterials(query: String) = db.getWorkOrderDao().searchMaterials(query)

    fun getMaterial(materialId: Long) = db.getWorkOrderDao().getMaterial(materialId)

    suspend fun getMaterialSync(materialId: Long) = db.getWorkOrderDao().getMaterialSync(materialId)

    fun getMaterial(mName: String) = db.getWorkOrderDao().getMaterial(mName)

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

    suspend fun removeWorkOrderHistoryMaterial(
        workOrderHistoryMaterialId: Long, updateTime: String
    ) = db.getWorkOrderDao().removeWorkOrderHistoryMaterial(workOrderHistoryMaterialId, updateTime)

    suspend fun updateWorkOrderHistoryMaterial(
        workOrderHistoryMaterial: WorkOrderHistoryMaterial
    ) = db.getWorkOrderDao().updateWorkOrderHistoryMaterial(workOrderHistoryMaterial)

    suspend fun deleteWorkOrderHistoryMaterial(
        historyMaterialId: Long, updateTime: String
    ) = db.getWorkOrderDao().deleteWorkOrderHistoryMaterial(historyMaterialId, updateTime)

    fun getMaterialsByHistory(historyId: Long) =
        db.getWorkOrderDao().getMaterialsByHistory(historyId)

    suspend fun getMaterialsFromHistoryId(historyId: Long) =
        db.getWorkOrderDao().getMaterialsFromHistoryId(historyId)

    fun getMaterialsHistoryByWorkOrderId(workOrderId: Long) =
        db.getWorkOrderDao().getMaterialsHistoryByWorkOrderId(workOrderId)

    suspend fun getWorkOrderHistoryMaterialCombined(woHistoryMaterialId: Long): WorkOrderHistoryMaterialCombined =
        db.getWorkOrderDao().getWorkOrderHistoryMaterialCombined(woHistoryMaterialId)

    suspend fun removeAllMaterialsFromWorkOrderHistory(historyId: Long, updateTime: String) =
        db.getWorkOrderDao().removeAllMaterialsFromWorkOrderHistory(historyId, updateTime)

    suspend fun insertArea(area: Areas) = db.getWorkOrderDao().insertArea(area)

    suspend fun updateArea(area: Areas) = db.getWorkOrderDao().updateArea(area)

    fun getAreasList() = db.getWorkOrderDao().getAreasList()

    suspend fun getAreasListSync() = db.getWorkOrderDao().getAreasListSync()

    fun getArea(areaId: Long) = db.getWorkOrderDao().getArea(areaId)

    suspend fun getAreaSync(areaId: Long) = db.getWorkOrderDao().getAreaSync(areaId)

    fun getArea(areaName: String) = db.getWorkOrderDao().getArea(areaName)

    suspend fun getAreaSync(areaName: String) = db.getWorkOrderDao().getAreaSync(areaName)

    fun searchAreas(query: String) = db.getWorkOrderDao().searchAreas(query)
}