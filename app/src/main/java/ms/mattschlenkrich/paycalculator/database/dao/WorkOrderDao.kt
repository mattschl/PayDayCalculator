package ms.mattschlenkrich.paycalculator.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.database.model.workorder.Areas
import ms.mattschlenkrich.paycalculator.database.model.workorder.JobSpec
import ms.mattschlenkrich.paycalculator.database.model.workorder.Material
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrder
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryMaterial
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryMaterialCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWithDates
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderHistoryWorkPerformedCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkOrderJobSpecCombined
import ms.mattschlenkrich.paycalculator.database.model.workorder.WorkPerformed

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
    fun searchWorkOrders(employerId: Long, query: String):
            LiveData<List<WorkOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrderHistory(history: WorkOrderHistory)


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
                "WHERE woHistoryWorkOrderId = :workOrderId " +
                "AND woHistoryDeleted = 0 "
    )
    fun getWorkOrderHistoriesById(workOrderId: Long): LiveData<List<WorkOrderHistoryWithDates>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryWorkOrderId = :workOrderNumber " +
                "AND woHistoryDeleted = 0 " +
                "Order BY woHistoryUpdateTime"
    )
    fun getWorkOrderHistoriesByNumber(workOrderNumber: String): LiveData<List<WorkOrderHistoryWithDates>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistory " +
                "WHERE woHistoryId = :historyID"
    )
    fun getWorkOrderHistory(historyID: Long): LiveData<WorkOrderHistoryWithDates>

    @Query(
        "UPDATE workOrderHistory " +
                "SET woHistoryDeleted = 1, " +
                "woHistoryUpdateTime = :updateTime " +
                "WHERE woHistoryWorkDateId = :workDateId"
    )
    suspend fun deleteWorkOrderHistoryByWorkDateId(
        workDateId: Long, updateTime: String
    )

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec)

    @Query(
        "UPDATE workOrderJobSpecs " +
                "SET wojsIsDeleted = 1, " +
                "wojsUpdateTime = :updateTime " +
                "WHERE workOrderJobSpecId = :workOrderJobSpecId"
    )
    suspend fun deleteWorkOrderJobSpec(
        workOrderJobSpecId: Long, updateTime: String
    )

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderJobSpecs " +
                "WHERE wojsIsDeleted = 0 " +
                "AND wojsWorkOrderId = :workOrderId " +
                "ORDER BY wojsSequence, " +
                "wojsUpdateTime"
    )
    fun getWorkOrderJobSpecs(workOrderId: Long):
            LiveData<List<WorkOrderJobSpecCombined>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrderHistoryWorkPerformed(
        workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed
    )

    @Query(
        "DELETE FROM workOrderHistoryWorkPerformed  " +
                "WHERE workOrderHistoryWorkPerformedId = :workPerformedHistoryId"
    )
    suspend fun removeWorkPerformedFromWorkOrderHistory(
        workPerformedHistoryId: Long
    )

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryWorkPerformed " +
                "WHERE wowpIsDeleted = 0 " +
                "AND wowpHistoryId = :historyId " +
                "ORDER BY wowpSequence, " +
                "wowpUpdateTime"
    )
    fun getWorkPerformedByWorkOrderHistory(historyId: Long):
            LiveData<List<WorkOrderHistoryWorkPerformedCombined>>

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
        "UPDATE materials " +
                "SET mIsDeleted = 1, " +
                "mUpdateTime = :updateTime " +
                "WHERE materialId = :materialId"
    )
    suspend fun deleteMaterial(materialId: Long, updateTime: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrderHistoryMaterial(
        workOrderHistoryMaterial: WorkOrderHistoryMaterial
    )

    @Query(
        "DELETE FROM workOrderHistoryMaterials " +
                "WHERE workOrderHistoryMaterialId = " +
                ":workOrderHistoryMaterialId"
    )
    suspend fun removeWorkOrderHistoryMaterial(
        workOrderHistoryMaterialId: Long
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
    suspend fun deleteWorkOrderHistoryMaterial(
        historyMaterialId: Long, updateTime: String
    )

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM workOrderHistoryMaterials " +
                "WHERE wohmHistoryId = :historyId " +
                "AND wohmIsDeleted = 0 " +
                "ORDER BY wohmUpdateTime"
    )
    fun getMaterialsByHistory(historyId: Long):
            LiveData<List<WorkOrderHistoryMaterialCombined>>

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