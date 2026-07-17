package ms.mattschlenkrich.paycalculator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec
import ms.mattschlenkrich.paycalculator.data.entity.JobSpecMerged
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.data.model.JobSpecAndChild
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderJobSpecCombined

@Dao
interface JobSpecDao {
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
}