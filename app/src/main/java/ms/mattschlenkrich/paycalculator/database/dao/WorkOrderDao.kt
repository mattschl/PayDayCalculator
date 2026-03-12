package ms.mattschlenkrich.paycalculator.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.RoomWarnings
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.database.model.workorder.Areas
import ms.mattschlenkrich.paycalculator.database.model.workorder.JobSpec
import ms.mattschlenkrich.paycalculator.database.model.workorder.Material
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryMaterialCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorked
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryTimeWorkedCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWorkPerformedCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderJobSpecCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformed
import ms.mattschlenkrich.paycalculator.database.model.workorder.merged.MaterialAndChild
import ms.mattschlenkrich.paycalculator.database.model.workorder.merged.MaterialMerged
import ms.mattschlenkrich.paycalculator.database.model.workorder.merged.WorkPerformedAndChild
import ms.mattschlenkrich.paycalculator.database.model.workorder.merged.WorkPerformedMerged

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


    /**
     * Sets the value of isDeleted to *true*
     */
    @Query(
        "UPDATE workOrders " +
                "SET woDeleted = 1, " +
                "woUpdateTime = :updateTime " +
                "WHERE workOrderId = :workOrderId"
    )
    suspend fun deleteWorkOrder(workOrderId: Long, updateTime: String)

    @Query(
        "UPDATE workOrders " +
                "SET woDeleted = 1, " +
                "woUpdateTime = :updateTime " +
                "WHERE workOrderId = :workOrderNumber"
    )
    suspend fun deleteWorkOrder(workOrderNumber: String, updateTime: String)

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE workOrderId = :workOrderId " +
                "AND woDeleted = 0"
    )
    fun getWorkOrder(workOrderId: Long): LiveData<WorkOrder>

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woNumber = :workOrderNum " +
                "AND woDeleted = 0"
    )
    fun getWorkOrder(workOrderNum: String): LiveData<WorkOrder>

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woNumber = :workOrderNum " +
                "AND woDeleted = :employerId"
    )
    fun findWorkOrder(workOrderNum: String, employerId: Long): WorkOrder?

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId " +
                "AND woDeleted = 0 " +
                "ORDER BY woNumber"
    )
    fun getWorkOrdersByEmployerId(employerId: Long): LiveData<List<WorkOrder>>

    @Query(
        "SELECT * FROM workOrders " +
                "WHERE woEmployerId = :employerId " +
                "AND (woNumber LIKE :query " +
                "OR woAddress LIKE :query) " +
                "ORDER BY woNumber"
    )
    fun searchWorkOrders(employerId: Long, query: String): LiveData<List<WorkOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
                "WHERE woHistoryId = :historyID"
    )
    suspend fun deleteWorkOrderHistory(historyID: Long, updateTime: String)

    /**
     * Deletes the entry in the database
     */
    @Query(
        "DELETE FROM workOrderHistory " +
                "WHERE woHistoryId = :historyId"
    )
    suspend fun deleteWorkOrderHistory(historyId: Long)

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
                "WHERE woHistoryId = :historyId "
    )
    fun getWorkOrderHistoriesById(historyId: Long): LiveData<WorkOrderHistoryWithDates>

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
                "WHERE woHistoryId = :historyID"
    )
    fun getWorkOrderHistory(historyID: Long): LiveData<WorkOrderHistoryWithDates>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryId = :historyID"
    )
    fun getWorkOrderHistoryWithDateById(historyID: Long): WorkOrderHistoryWithDates

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryId = :historyId"
    )
    fun getWorkOrderHistoryCombined(historyId: Long): LiveData<WorkOrderHistoryCombined>

    @Query(
        "UPDATE workOrderHistory " +
                "SET woHistoryDeleted = 1, " +
                "woHistoryUpdateTime = :updateTime " +
                "WHERE woHistoryWorkDateId = :workDateId"
    )
    suspend fun deleteWorkOrderHistoryByWorkDateId(workDateId: Long, updateTime: String)


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

    @Delete
    suspend fun deleteTimeWorked(timeWorked: WorkOrderHistoryTimeWorked)

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

    @Insert
    suspend fun insertJobSpec(jobSpec: JobSpec)

    @Update
    suspend fun updateJobSpec(jobSpec: JobSpec)

    @Query(
        "SELECT * FROM jobSpecs " +
                "WHERE jsIsDeleted = 0 " +
                "ORDER BY jsName"
    )
    fun getJobSpecsAll(): LiveData<List<JobSpec>>

    @Query(
        "SELECT * FROM jobSpecs " +
                "WHERE jsName LIKE :query " +
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

    @Query(
        "DELETE FROM workOrderJobSpecs " +
                "WHERE workOrderJobSpecId = :workOrderJobSpecId"
    )
    suspend fun deleteWorkOrderJobSpec(workOrderJobSpecId: Long)

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
                "WHERE workOrderJobSpecId = :workOrderJobSpecId"
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

    @Delete
    suspend fun deleteWorkPerformed(workPerformed: WorkPerformed)


    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE wpIsDeleted = 0 " +
                "ORDER BY wpDescription"
    )
    fun getWorkPerformedAll(): LiveData<List<WorkPerformed>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM workPerformed " +
                "INNER JOIN  " +
                "(SELECT wpmChildId FROM workPerformedMerged " +
                "WHERE wpmMasterId = :workPerformedId) " +
                "ON workPerformed.workPerformedId = " +
                "wpmChildId "
    )
    fun getWorkPerformedChildren(workPerformedId: Long): LiveData<List<WorkPerformed>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM workPerformedMerged " +
                "WHERE wpmMasterId = :workPerformedId"
    )
    fun getWorkPerformedAndChildList(workPerformedId: Long): LiveData<List<WorkPerformedAndChild>>


    @Insert
    suspend fun insertWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged)

    @Update
    suspend fun updateWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged)

    @Delete
    suspend fun deleteWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged)

    @Query(
        "DELETE FROM workPerformedMerged " +
                "WHERE workPerformedMergeId = :workPerformedMergedId"
    )
    suspend fun deleteWorkPerformedMerged(workPerformedMergedId: Long)


    @Query(
        "Update workOrderHistoryWorkPerformed " +
                "SET wowpWorkPerformedId = :newWorkPerformedId " +
                "WHERE wowpWorkPerformedId = :oldWorkPerformedId"
    )
    suspend fun updateWorkPerformedMerged(oldWorkPerformedId: Long, newWorkPerformedId: Long)

    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE wpDescription LIKE :query " +
                "ORDER BY wpDescription"
    )
    fun searchFromWorkPerformed(query: String): LiveData<List<WorkPerformed>>

    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE wpDescription = :description"
    )
    fun getWorkPerformed(description: String): LiveData<WorkPerformed>

    @Query(
        "SELECT * FROM workPerformed " +
                "WHERE workPerformedId = :workPerformedId"
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
    fun getWorkOrderHistory(workOrderId: Long, workDateId: Long): WorkOrderHistory?


    @Query(
        "DELETE FROM workOrderHistoryWorkPerformed  " +
                "WHERE wowpHistoryId = :historyId"
    )
    suspend fun removeAllWorkPerformedFromWorkOrderHistory(historyId: Long)

    @Query(
        "DELETE FROM workOrderHistoryWorkPerformed " +
                "WHERE workOrderHistoryWorkPerformedId = :historyWorkPerformedId"
    )
    suspend fun deleteWorkOrderHistoryWorkPerformed(historyWorkPerformedId: Long)

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
    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryWorkPerformed as wp, areas as ar " +
                "INNER JOIN " +
                "(SELECT * FROM areas )" +
                "ON ar.areaId = wp.wowpAreaId " +
                "WHERE wp.wowpIsDeleted = 0 " +
                "AND wp.wowpHistoryId = :historyId " +
                "ORDER BY ar.areaName, wp.wowpSequence, " +
                "wp.wowpUpdateTime"
    )
    fun getWorkPerformedByWorkOrderHistory2(historyId: Long):
            LiveData<List<WorkOrderHistoryWorkPerformedCombined>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryWorkPerformed " +
                "WHERE workOrderHistoryWorkPerformedId = :historyWorkPerformedId"
    )
    fun getWorkPerformedHistoryById(historyWorkPerformedId: Long):
            LiveData<WorkOrderHistoryWorkPerformedCombined>

    @Insert
    suspend fun insertMaterial(material: Material)

    @Update
    suspend fun updateMaterial(material: Material)

    @Query(
        "SELECT * FROM materials " +
                "WHERE mIsDeleted = 0 " +
                "ORDER BY mName"
    )
    fun getMaterialsList(): LiveData<List<Material>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM materials " +
                "INNER JOIN " +
                "(SELECT mmChildId FROM materialMerged " +
                "WHERE mmMasterId = :materialId)" +
                "ON materials.materialId = mmChildId " +
                "ORDER BY mName"
    )
    fun getMaterialsChildren(materialId: Long): LiveData<List<Material>>


    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM materialMerged " +
                "WHERE mmMasterId = :materialId"
    )
    fun getMaterialAndChildList(materialId: Long): LiveData<List<MaterialAndChild>>

    @Query(
        "SELECT * FROM materials " +
                "WHERE mName LIKE :query " +
                "ORDER BY mName"
    )
    fun searchMaterials(query: String): LiveData<List<Material>>

    @Query(
        "SELECT * FROM materials " +
                "WHERE materialId = :materialId"
    )
    fun getMaterial(materialId: Long): LiveData<Material>

    @Query(
        "SELECT * FROM materials " +
                "WHERE mName = :mName"
    )
    fun getMaterial(mName: String): LiveData<Material>

    @Query(
        "UPDATE workOrderHistoryMaterials " +
                "SET wohmMaterialId = :newMaterialID " +
                "AND wohmUpdateTime = :updateTime " +
                "WHERE wohmMaterialId = :oldMaterialID "
    )
    suspend fun updateMaterialMerged(oldMaterialID: Long, newMaterialID: Long, updateTime: String)

    @Query(
        "DELETE FROM materialMerged " +
                "WHERE materialMergeId = :materialMergedId"
    )
    suspend fun deleteMaterialMerged(materialMergedId: Long)

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
        "DELETE FROM workOrderHistoryMaterials " +
                "WHERE wohmHistoryId = :historyId"
    )
    suspend fun removeAllMaterialsFromWorkOrderHistory(historyId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrderHistoryMaterial(
        workOrderHistoryMaterial: WorkOrderHistoryMaterial
    )

    @Query(
        "DELETE FROM workOrderHistoryMaterials " +
                "WHERE workOrderHistoryMaterialId = " +
                ":workOrderHistoryMaterialId"
    )
    suspend fun removeWorkOrderHistoryMaterial(workOrderHistoryMaterialId: Long)

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
        "SELECT * FROM Materials " +
                "INNER JOIN " +
                "(SELECT wohmMaterialId FROM workOrderHistoryMaterials " +
                "WHERE wohmHistoryId = :historyId " +
                "AND wohmIsDeleted = 0 ) " +
                "ON wohmMaterialId = materialId"
    )
    fun getMaterialsFromHistoryId(historyId: Long): List<Material>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryMaterials " +
                "INNER JOIN " +
                "(SELECT woHistoryId FROM workOrderHistory " +
                "WHERE woHistoryWorkOrderId = :workOrderId " +
                "AND woHistoryDeleted = 0 " +
                "ORDER BY woHistoryUpdateTime) " +
                "ON woHistoryId = wohmHistoryId " +
                "WHERE wohmIsDeleted = 0 " +
                "ORDER By wohmMaterialId"
    )
    fun getMaterialsHistoryByWorkOrderId(workOrderId: Long):
            LiveData<List<WorkOrderHistoryMaterialCombined>>

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
    fun getWorkOrderHistoryMaterialCombined(woHistoryMaterialId: Long):
            WorkOrderHistoryMaterialCombined

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
                "WHERE areaId = :areaId"
    )
    fun getArea(areaId: Long): LiveData<Areas>

    @Query(
        "SELECT * FROM areas " +
                "WHERE areaName = :areaName"
    )
    fun getArea(areaName: String): LiveData<Areas>

    @Query(
        "SELECT * FROM areas " +
                "WHERE areaName LIKE :query"
    )
    fun searchAreas(query: String): LiveData<List<Areas>>
}