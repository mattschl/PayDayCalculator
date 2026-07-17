package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.JobSpec
import ms.mattschlenkrich.paycalculator.data.entity.JobSpecMerged
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderJobSpec
import ms.mattschlenkrich.paycalculator.data.repository.JobSpecRepository

class JobSpecViewModel(
    app: Application,
    private val jobSpecRepository: JobSpecRepository
) : AndroidViewModel(app) {
    fun getJobSpec(jobSpecId: Long) = jobSpecRepository.getJobSpec(jobSpecId)
    fun getJobSpecAndChildList(jobSpecId: Long) =
        jobSpecRepository.getJobSpecAndChildList(jobSpecId)

    suspend fun insertJobSpecMerged(jobSpecMerged: JobSpecMerged) =
        jobSpecRepository.insertJobSpecMerged(jobSpecMerged)

    suspend fun deleteJobSpecMerged(jobSpecMergedId: Long, updateTime: String) =
        jobSpecRepository.deleteJobSpecMerged(jobSpecMergedId, updateTime)

    suspend fun updateJobSpecMerged(oldJobSpecId: Long, newJobSpecId: Long) =
        jobSpecRepository.updateJobSpecMerged(oldJobSpecId, newJobSpecId)

    suspend fun updateJobSpec(jobSpec: JobSpec) = jobSpecRepository.updateJobSpec(jobSpec)
    suspend fun deleteJobSpec(jobSpecId: Long, updateTime: String) =
        jobSpecRepository.deleteJobSpec(jobSpecId, updateTime)

    suspend fun getJobSpecsAllSync() = jobSpecRepository.getJobSpecsAllSync()
    fun searchJobSpecs(query: String) = jobSpecRepository.searchJobSpecs(query)
    suspend fun insertWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) =
        jobSpecRepository.insertWorkOrderJobSpec(workOrderJobSpec)

    suspend fun updateWorkOrderJobSpec(workOrderJobSpec: WorkOrderJobSpec) =
        jobSpecRepository.updateWorkOrderJobSpec(workOrderJobSpec)

    fun getWorkOrderJobSpecs(workOrderId: Long) =
        jobSpecRepository.getWorkOrderJobSpecs(workOrderId)

    fun getWorkOrderJobSpec(workOrderJobSpecId: Long) =
        jobSpecRepository.getWorkOrderJobSpec(workOrderJobSpecId)

    suspend fun getOrCreateJobSpec(name: String): JobSpec {
        val existing = getJobSpecsAllSync().find {
            it.jsName.trim().equals(name.trim(), ignoreCase = true)
        }
        if (existing != null) return existing

        val nf = NumberFunctions()
        val df = DateFunctions()
        val newJobSpec = JobSpec(
            nf.generateRandomIdAsLong(),
            name.trim(),
            false,
            df.getCurrentUTCTimeAsString()
        )
        jobSpecRepository.insertJobSpec(newJobSpec)
        return newJobSpec
    }
}