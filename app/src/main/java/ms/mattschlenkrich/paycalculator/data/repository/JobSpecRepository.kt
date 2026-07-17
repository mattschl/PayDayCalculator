package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec
import ms.mattschlenkrich.paycalculator.data.entity.JobSpecMerged
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderJobSpec

class JobSpecRepository(private val db: PayDatabase) {
    fun getJobSpec(jobSpecId: Long) = db.getJobSpecDao().getJobSpec(jobSpecId)
    fun getJobSpecAndChildList(jobSpecId: Long) =
        db.getJobSpecDao().getJobSpecAndChildList(jobSpecId)

    suspend fun insertJobSpecMerged(jobSpecMerged: JobSpecMerged) =
        db.getJobSpecDao().insertJobSpecMerged(jobSpecMerged)

    suspend fun deleteJobSpecMerged(jobSpecMergedId: Long, updateTime: String) =
        db.getJobSpecDao().deleteJobSpecMerged(jobSpecMergedId, updateTime)

    suspend fun updateJobSpecMerged(oldJobSpecId: Long, newJobSpecId: Long) =
        db.getJobSpecDao().updateJobSpecMerged(oldJobSpecId, newJobSpecId)

    suspend fun insertJobSpec(jobSpec: JobSpec) = db.getJobSpecDao().insertJobSpec(jobSpec)
    suspend fun updateJobSpec(jobSpec: JobSpec) = db.getJobSpecDao().updateJobSpec(jobSpec)
    suspend fun deleteJobSpec(jobSpecId: Long, updateTime: String) =
        db.getJobSpecDao().deleteJobSpec(jobSpecId, updateTime)

    fun getJobSpecs() = db.getJobSpecDao().getJobSpecsAll()
    suspend fun getJobSpecsAllSync() = db.getJobSpecDao().getJobSpecsAllSync()
    fun searchJobSpecs(query: String) = db.getJobSpecDao().searchJobSpecs(query)
    suspend fun insertWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) =
        db.getJobSpecDao().insertWorkOrderJobSpec(workOrderJobSpec)

    suspend fun updateWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) =
        db.getJobSpecDao().updateWorkOrderJobSpec(workOrderJobSpec)

    fun getWorkOrderJobSpecs(workOrderId: Long) =
        db.getJobSpecDao().getWorkOrderJobSpecs(workOrderId)

    fun getWorkOrderJobSpec(workOrderJobSpecId: Long) =
        db.getJobSpecDao().getWorkOrderJobSpec(workOrderJobSpecId)
}