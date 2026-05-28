package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
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
import ms.mattschlenkrich.paycalculator.data.model.JobSpecAndChild
import ms.mattschlenkrich.paycalculator.data.model.MaterialAndChild
import ms.mattschlenkrich.paycalculator.data.model.MaterialAndQuantity
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryMaterialCombined
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryWorkPerformedCombined
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderJobSpecCombined
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderSummary
import ms.mattschlenkrich.paycalculator.data.model.WorkPerformedAndChild
import ms.mattschlenkrich.paycalculator.data.model.WorkPerformedAndQuantity

@Dao
interface WorkOrderDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkOrder(workOrder: WorkOrder)

    @Query(
        "Update workOrders " +
                "SET woNumber = :workOrderNumber, " +
                "woEmployerId = :employerId, " +
                "woAddress = :address, " +
                "woDescription = :description," +
                "woDeleted = :isDeleted," +
                "woUpdateTime = :updateTime " +
                "WHERE workOrderId = :workOrderId"
    )
    suspend fun updateWorkOrder(
        workOrderId: Long,
        workOrderNumber: String,
        employerId: Long,
        address: String,
        description: String,
        isDeleted: Boolean,
        updateTime: String,
    )

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woNumber = :workOrderNum " +
                "AND woEmployerId = :employerId " +
                "AND woDeleted = 0"
    )
    suspend fun findWorkOrder(workOrderNum: String, employerId: Long): WorkOrder?

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId " +
                "AND woDeleted = 0 " +
                "ORDER BY woNumber"
    )
    fun getWorkOrdersByEmployerId(employerId: Long): LiveData<List<WorkOrder>>

    @Query(
        "SELECT DISTINCT woAddress FROM workOrders " +
                "WHERE woEmployerId = :employerId " +
                "AND woDeleted = 0 " +
                "ORDER BY woAddress COLLATE NOCASE"
    )
    fun getUniqueAddresses(employerId: Long): LiveData<List<String>>

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId " +
                "AND (woNumber LIKE :query " +
                "OR woAddress LIKE :query) " +
                "AND woDeleted = 0 " +
                "ORDER BY woNumber"
    )
    fun searchWorkOrders(employerId: Long, query: String): LiveData<List<WorkOrder>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkOrderHistory(history: WorkOrderHistory)

    @Update
    suspend fun updateWorkOrderHistory(history: WorkOrderHistory)

    @Query(
        "Update workOrderHistory " +
                "SET woHistoryWorkOrderId = :workOrderId, " +
                "woHistoryWorkDateId = :workDateId, " +
                "woHistoryRegHours = :regHours, " +
                "woHistoryOtHours = :otHours, " +
                "woHistoryDblOtHours = :dblOtHours, " +
                "woHistoryNote = :note, " +
                "woHistoryDeleted = :isDeleted, " +
                "woHistoryUpdateTime = :updateTime " +
                "WHERE woHistoryId = :historyID"
    )
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
    )

    @Query(
        "UPDATE workOrderHistory " +
                "SET woHistoryDeleted = 1, " +
                "woHistoryUpdateTime = :updateTime " +
                "WHERE woHistoryId = :historyId"
    )
    suspend fun deleteWorkOrderHistory(historyId: Long, updateTime: String)

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkDateId = :workDateId " +
                "AND woHistoryDeleted = 0 " +
                "Order by woHistoryUpdateTime"
    )
    fun getWorkOrderHistoriesByDate(workDateId: Long): LiveData<List<WorkOrderHistoryWithDates>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryId = :historyId " +
                "AND woHistoryDeleted = 0 "
    )
    fun getWorkOrderHistory(historyId: Long): LiveData<WorkOrderHistoryWithDates>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkOrderId = :workOrderId " +
                "AND woHistoryDeleted = 0 " +
                "Order BY woHistoryUpdateTime"
    )
    fun getWorkOrderHistoriesByWorkOrder(workOrderId: Long):
            LiveData<List<WorkOrderHistoryWithDates>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryId = :historyId " +
                "AND woHistoryDeleted = 0"
    )
    fun getWorkOrderHistoryCombined(historyId: Long): LiveData<WorkOrderHistoryCombined>

    @Query(
        "SELECT SUM(woHistoryRegHours) as totalRegHours, " +
                "SUM(woHistoryOtHours) as totalOtHours, " +
                "SUM(woHistoryDblOtHours) as totalDblOtHours " +
                "FROM workOrderHistory " +
                "WHERE woHistoryWorkOrderId = :workOrderId " +
                "AND woHistoryDeleted = 0"
    )
    fun getWorkOrderSummary(workOrderId: Long): LiveData<WorkOrderSummary>

    @Query(
        "SELECT mName as name, SUM(wohmQuantity) as quantity " +
                "FROM workOrderHistoryMaterials " +
                "INNER JOIN materials ON wohmMaterialId = materialId " +
                "WHERE wohmHistoryId IN (SELECT woHistoryId FROM workOrderHistory WHERE woHistoryWorkOrderId = :workOrderId AND woHistoryDeleted = 0) " +
                "AND wohmIsDeleted = 0 " +
                "GROUP BY mName " +
                "ORDER BY mName"
    )
    fun getWorkOrderMaterialsSummary(workOrderId: Long): LiveData<List<MaterialAndQuantity>>

    @Query(
        "SELECT wpDescription as description, null as area, COUNT(*) as quantity " +
                "FROM workOrderHistoryWorkPerformed " +
                "INNER JOIN workPerformed ON wowpWorkPerformedId = workPerformedId " +
                "WHERE wowpHistoryId IN (SELECT woHistoryId FROM workOrderHistory WHERE woHistoryWorkOrderId = :workOrderId AND woHistoryDeleted = 0) " +
                "AND wowpIsDeleted = 0 " +
                "GROUP BY wpDescription " +
                "ORDER BY wpDescription"
    )
    fun getWorkOrderWorkPerformedSummary(workOrderId: Long): LiveData<List<WorkPerformedAndQuantity>>

    @Insert
    suspend fun insertTimeWorked(timeWorked: WorkOrderHistoryTimeWorked)

    @Update
    suspend fun updateTimeWorked(timeWorked: WorkOrderHistoryTimeWorked)

    @Query(
        "UPDATE workOrderHistoryTimeWorked " +
                "SET wohtIsDeleted = 1, " +
                "wohtUpdateTime = :updateTime " +
                "WHERE woHistoryTimeWorkedId = :timeWorkedId"
    )
    suspend fun deleteTimeWorked(timeWorkedId: Long, updateTime: String)

    @Query(
        "SELECT * FROM workOrderHistoryTimeWorked " +
                "WHERE wohtHistoryId = :historyId " +
                "AND wohtIsDeleted = 0"
    )
    suspend fun getTimeWorkedForWorkOrderHistorySync(historyId: Long): List<WorkOrderHistoryTimeWorked>

    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkDateId = :workDateId " +
                "AND woHistoryDeleted = 0"
    )
    suspend fun getWorkOrderHistoriesByDateSync(workDateId: Long): List<WorkOrderHistory>

    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryId = :historyId " +
                "AND woHistoryDeleted = 0"
    )
    suspend fun getWorkOrderHistorySync(historyId: Long): WorkOrderHistory?

    @Query(
        "SELECT * FROM workOrderHistoryTimeWorked " +
                "WHERE woHistoryTimeWorkedId = :timeWorkedId"
    )
    suspend fun getTimeWorkedSync(timeWorkedId: Long): WorkOrderHistoryTimeWorked?

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryTimeWorked " +
                "WHERE wohtDateId = :workDateId  " +
                "AND wohtIsDeleted = 0 " +
                "order BY wohtStartTime"
    )
    fun getTimeWorkedPerDay(workDateId: Long): LiveData<List<WorkOrderHistoryTimeWorkedCombined>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryTimeWorked " +
                "WHERE wohtHistoryId = :historyId " +
                "AND wohtIsDeleted = 0 " +
                "order BY wohtStartTime"
    )
    fun getTimeWorkedForWorkOrderHistory(historyId: Long): LiveData<List<WorkOrderHistoryTimeWorkedCombined>>

    @Query(
        "SELECT * FROM jobSpecs " +
                "WHERE jobSpecId = :jobSpecId " +
                "AND jsIsDeleted = 0"
    )
    fun getJobSpec(jobSpecId: Long): LiveData<JobSpec>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM jobSpecMerged " +
                "WHERE jsmMasterId = :jobSpecId " +
                "AND jsmIsDeleted = 0"
    )
    fun getJobSpecAndChildList(jobSpecId: Long): LiveData<List<JobSpecAndChild>>

    @Insert
    suspend fun insertJobSpecMerged(jobSpecMerged: JobSpecMerged)

    @Query(
        "UPDATE jobSpecMerged " +
                "SET jsmIsDeleted = 1, " +
                "jsmUpdateTime = :updateTime " +
                "WHERE jobSpecMergedId = :jobSpecMergedId"
    )
    suspend fun deleteJobSpecMerged(jobSpecMergedId: Long, updateTime: String)

    @Query(
        "UPDATE workOrderJobSpecs " +
                "SET wojsJobSpecId = :newJobSpecId " +
                "WHERE wojsJobSpecId = :oldJobSpecId"
    )
    suspend fun updateJobSpecMerged(oldJobSpecId: Long, newJobSpecId: Long)

    @Insert
    suspend fun insertJobSpec(jobSpec: JobSpec)

    @Update
    suspend fun updateJobSpec(jobSpec: JobSpec)

    @Query(
        "UPDATE jobSpecs " +
                "SET jsIsDeleted = 1," +
                "jsUpdateTime = :updateTime " +
                "WHERE jobSpecId = :jobSpecId"
    )
    suspend fun deleteJobSpec(jobSpecId: Long, updateTime: String)

    @Query(
        "SELECT * FROM jobSpecs " +
                "WHERE jsIsDeleted = 0 " +
                "ORDER BY jsName"
    )
    fun getJobSpecsAll(): LiveData<List<JobSpec>>

    @Query(
        "SELECT * FROM jobSpecs " +
                "WHERE jsIsDeleted = 0 " +
                "ORDER BY jsName"
    )
    suspend fun getJobSpecsAllSync(): List<JobSpec>

    @Query(
        "SELECT * FROM jobSpecs " +
                "WHERE jsName LIKE :query " +
                "AND jsIsDeleted = 0 " +
                "ORDER BY jsName"
    )
    fun searchJobSpecs(query: String): LiveData<List<JobSpec>>

    @Insert
    suspend fun insertWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec)

    @Update
    suspend fun updateWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec)

    @Query(
        "UPDATE workOrderJobSpecs " +
                "SET wojsIsDeleted = 1, " +
                "wojsUpdateTime = :updateTime " +
                "WHERE workOrderJobSpecId = :workOrderJobSpecId"
    )
    suspend fun deleteWorkOrderJobSpec(workOrderJobSpecId: Long, updateTime: String)

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderJobSpecs " +
                "WHERE wojsIsDeleted = 0 " +
                "AND wojsWorkOrderId = :workOrderId " +
                "ORDER BY wojsSequence, " +
                "wojsUpdateTime"
    )
    fun getWorkOrderJobSpecs(workOrderId: Long): LiveData<List<WorkOrderJobSpecCombined>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderJobSpecs " +
                "WHERE workOrderJobSpecId = :workOrderJobSpecId " +
                "AND wojsIsDeleted = 0"
    )
    fun getWorkOrderJobSpec(workOrderJobSpecId: Long): LiveData<WorkOrderJobSpecCombined>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkPerformed(workPerformed: WorkPerformed)

    @Query(
        "UPDATE workPerformed " +
                "SET wpIsDeleted = 1," +
                "wpUpdateTime = :updateTime " +
                "WHERE workPerformedId = :workPerformedId"
    )
    suspend fun deleteWorkPerformed(workPerformedId: Long, updateTime: String)


    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE wpIsDeleted = 0 " +
                "ORDER BY wpDescription"
    )
    fun getWorkPerformedAll(): LiveData<List<WorkPerformed>>

    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE wpIsDeleted = 0 " +
                "ORDER BY wpDescription"
    )
    suspend fun getWorkPerformedAllSync(): List<WorkPerformed>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM workPerformedMerged " +
                "WHERE wpmMasterId = :workPerformedId " +
                "AND wpmIsDeleted = 0"
    )
    fun getWorkPerformedAndChildList(workPerformedId: Long): LiveData<List<WorkPerformedAndChild>>


    @Insert
    suspend fun insertWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged)

    @Query(
        "UPDATE workPerformedMerged " +
                "SET wpmIsDeleted = 1, " +
                "wpmUpdateTime = :updateTime " +
                "WHERE workPerformedMergeId = :workPerformedMergedId"
    )
    suspend fun deleteWorkPerformedMerged(workPerformedMergedId: Long, updateTime: String)


    @Query(
        "Update workOrderHistoryWorkPerformed " +
                "SET wowpWorkPerformedId = :newWorkPerformedId " +
                "WHERE wowpWorkPerformedId = :oldWorkPerformedId"
    )
    suspend fun updateWorkPerformedMerged(oldWorkPerformedId: Long, newWorkPerformedId: Long)

    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE wpDescription LIKE :query " +
                "AND wpIsDeleted = 0 " +
                "ORDER BY wpDescription"
    )
    fun searchFromWorkPerformed(query: String): LiveData<List<WorkPerformed>>

    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE wpDescription = :description " +
                "AND wpIsDeleted = 0"
    )
    suspend fun getWorkPerformedSync(description: String): WorkPerformed?

    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE workPerformedId = :workPerformedId " +
                "AND wpIsDeleted = 0"
    )
    fun getWorkPerformed(workPerformedId: Long): LiveData<WorkPerformed>

    @Update
    suspend fun updateWorkPerformed(workPerformed: WorkPerformed)

    @Insert
    suspend fun insertWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    )

    @Update
    suspend fun updateWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    )

    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkOrderId = :workOrderId " +
                "AND woHistoryWorkDateId = :workDateId " +
                "AND woHistoryDeleted = 0"
    )
    suspend fun getWorkOrderHistorySync(workOrderId: Long, workDateId: Long): WorkOrderHistory?


    @Query(
        "UPDATE workOrderHistoryWorkPerformed  " +
                "SET wowpIsDeleted = 1, " +
                "wowpUpdateTime = :updateTime " +
                "WHERE wowpHistoryId = :historyId"
    )
    suspend fun removeAllWorkPerformedFromWorkOrderHistory(historyId: Long, updateTime: String)

    @Query(
        "UPDATE workOrderHistoryWorkPerformed " +
                "SET wowpIsDeleted = 1, " +
                "wowpUpdateTime = :updateTime " +
                "WHERE workOrderHistoryWorkPerformedId = :historyWorkPerformedId"
    )
    suspend fun deleteWorkOrderHistoryWorkPerformed(
        historyWorkPerformedId: Long,
        updateTime: String
    )

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryWorkPerformed " +
                "WHERE wowpIsDeleted = 0 " +
                "AND wowpHistoryId = :historyId " +
                "ORDER BY wowpAreaId, wowpSequence, " +
                "wowpUpdateTime"
    )
    fun getWorkPerformedByWorkOrderHistory(historyId: Long):
            LiveData<List<WorkOrderHistoryWorkPerformedCombined>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryWorkPerformed " +
                "WHERE workOrderHistoryWorkPerformedId = :historyWorkPerformedId " +
                "AND wowpIsDeleted = 0"
    )
    fun getWorkPerformedHistoryById(historyWorkPerformedId: Long):
            LiveData<WorkOrderHistoryWorkPerformedCombined>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMaterial(material: Material)

    @Update
    suspend fun updateMaterial(material: Material)

    @Query(
        "SELECT * FROM materials " +
                "WHERE mIsDeleted = 0 " +
                "ORDER BY mName"
    )
    fun getMaterialsList(): LiveData<List<Material>>

    @Query(
        "SELECT * FROM materials " +
                "WHERE mIsDeleted = 0 " +
                "ORDER BY mName"
    )
    suspend fun getMaterialsListSync(): List<Material>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM materialMerged " +
                "WHERE mmMasterId = :materialId " +
                "AND mmIsDeleted = 0"
    )
    fun getMaterialAndChildList(materialId: Long): LiveData<List<MaterialAndChild>>

    @Query(
        "SELECT * FROM materials " +
                "WHERE mName LIKE :query " +
                "AND mIsDeleted = 0 " +
                "ORDER BY mName"
    )
    fun searchMaterials(query: String): LiveData<List<Material>>

    @Query(
        "SELECT * FROM materials " +
                "WHERE materialId = :materialId " +
                "AND mIsDeleted = 0"
    )
    fun getMaterial(materialId: Long): LiveData<Material>

    @Query(
        "SELECT * FROM materials " +
                "WHERE materialId = :materialId " +
                "AND mIsDeleted = 0"
    )
    suspend fun getMaterialSync(materialId: Long): Material?

    @Query(
        "SELECT * FROM materials " +
                "WHERE mName = :mName " +
                "AND mIsDeleted = 0"
    )
    suspend fun getMaterialSync(mName: String): Material?

    @Query(
        "UPDATE workOrderHistoryMaterials " +
                "SET wohmMaterialId = :newMaterialID, " +
                "wohmUpdateTime = :updateTime " +
                "WHERE wohmMaterialId = :oldMaterialID "
    )
    suspend fun updateMaterialMerged(oldMaterialID: Long, newMaterialID: Long, updateTime: String)

    @Query(
        "UPDATE materialMerged " +
                "SET mmIsDeleted = 1, " +
                "mmUpdateTime = :updateTime " +
                "WHERE materialMergeId = :materialMergedId"
    )
    suspend fun deleteMaterialMerged(materialMergedId: Long, updateTime: String)

    @Query(
        "UPDATE materials " +
                "SET mIsDeleted = 1, " +
                "mUpdateTime = :updateTime " +
                "WHERE materialId = :materialId"
    )
    suspend fun deleteMaterial(materialId: Long, updateTime: String)

    @Insert
    suspend fun insertMaterialMerged(materialMerged: MaterialMerged)


    @Query(
        "UPDATE workOrderHistoryMaterials " +
                "SET wohmIsDeleted = 1, " +
                "wohmUpdateTime = :updateTime " +
                "WHERE wohmHistoryId = :historyId"
    )
    suspend fun removeAllMaterialsFromWorkOrderHistory(historyId: Long, updateTime: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkOrderHistoryMaterial(
        workOrderHistoryMaterial: WorkOrderHistoryMaterial
    )

    @Update
    suspend fun updateWorkOrderHistoryMaterial(
        workOrderHistoryMaterial: WorkOrderHistoryMaterial
    )

    @Query(
        "UPDATE workOrderHistoryMaterials " +
                "SET wohmIsDeleted = 1," +
                "wohmUpdateTime = :updateTime " +
                "WHERE workOrderHistoryMaterialId = :historyMaterialId"
    )
    suspend fun deleteWorkOrderHistoryMaterial(historyMaterialId: Long, updateTime: String)

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryMaterials " +
                "WHERE wohmHistoryId = :historyId " +
                "AND wohmIsDeleted = 0 " +
                "ORDER BY wohmUpdateTime"
    )
    fun getMaterialsByHistory(historyId: Long): LiveData<List<WorkOrderHistoryMaterialCombined>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "INNER JOIN " +
                "(SELECT * FROM workOrderHistoryMaterials " +
                "WHERE workOrderHistoryMaterialId = :woHistoryMaterialId " +
                "AND wohmIsDeleted = 0) " +
                "ON woHistoryId = wohmHistoryId " +
                "WHERE woHistoryDeleted = 0"
    )
    suspend fun getWorkOrderHistoryMaterialCombined(woHistoryMaterialId: Long):
            WorkOrderHistoryMaterialCombined?

    @Insert
    suspend fun insertArea(area: Areas)

    @Update
    suspend fun updateArea(area: Areas)

    @Query(
        "SELECT * FROM areas " +
                "WHERE areaIsDeleted = 0 " +
                "ORDER BY areaName"
    )
    fun getAreasList(): LiveData<List<Areas>>

    @Query(
        "SELECT * FROM areas " +
                "WHERE areaIsDeleted = 0 " +
                "ORDER BY areaName"
    )
    suspend fun getAreasListSync(): List<Areas>

    @Query(
        "SELECT * FROM areas " +
                "WHERE areaId = :areaId " +
                "AND areaIsDeleted = 0"
    )
    fun getArea(areaId: Long): LiveData<Areas>

    @Query(
        "SELECT * FROM areas " +
                "WHERE areaName LIKE :query " +
                "AND areaIsDeleted = 0"
    )
    fun searchAreas(query: String): LiveData<List<Areas>>
}