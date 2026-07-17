package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformedMerged
import ms.mattschlenkrich.paycalculator.data.repository.WorkPerformedRepository

class WorkPerformedViewModel(
    app: Application,
    private val workPerformedRepository: WorkPerformedRepository
) : AndroidViewModel(app) {
    suspend fun insertWorkPerformed(workPerformed: WorkPerformed) =
        workPerformedRepository.insertWorkPerformed(workPerformed)

    suspend fun deleteWorkPerformed(workPerformedId: Long, updateTime: String) =
        workPerformedRepository.deleteWorkPerformed(workPerformedId, updateTime)

    suspend fun deleteWorkPerformedMerged(workPerformedMergedId: Long, updateTime: String) =
        workPerformedRepository.deleteWorkPerformedMerged(workPerformedMergedId, updateTime)

    fun getWorkPerformedAll() = workPerformedRepository.getWorkPerformedAll()
    suspend fun getWorkPerformedAllSync() = workPerformedRepository.getWorkPerformedAllSync()
    fun getWorkPerformedAndChildList(workPerformedId: Long) =
        workPerformedRepository.getWorkPerformedAndChildList(workPerformedId)

    suspend fun insertWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged) =
        workPerformedRepository.insertWorkPerformedMerged(workPerformedMerged)

    suspend fun updateWorkPerformedMerged(oldWorkPerformedId: Long, newWorkPerformedId: Long) =
        workPerformedRepository.updateWorkPerformedMerged(oldWorkPerformedId, newWorkPerformedId)

    fun searchFromWorkPerformed(query: String) =
        workPerformedRepository.searchFromWorkPerformed(query)

    suspend fun getWorkPerformedSync(description: String) =
        workPerformedRepository.getWorkPerformedSync(description)

    fun getWorkPerformed(workPerformedId: Long) =
        workPerformedRepository.getWorkPerformed(workPerformedId)

    suspend fun updateWorkPerformed(workPerformed: WorkPerformed) =
        workPerformedRepository.updateWorkPerformed(workPerformed)

    suspend fun insertWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed) =
        workPerformedRepository.insertWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed)

    suspend fun updateWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed) =
        workPerformedRepository.updateWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed)

    suspend fun removeAllWorkPerformedFromWorkOrderHistory(historyId: Long, updateTime: String) =
        workPerformedRepository.removeAllWorkPerformedFromWorkOrderHistory(historyId, updateTime)

    suspend fun deleteWorkOrderHistoryWorkPerformed(
        historyWorkPerformedId: Long,
        updateTime: String
    ) = workPerformedRepository.deleteWorkOrderHistoryWorkPerformed(
        historyWorkPerformedId,
        updateTime
    )

    fun getWorkPerformedCombinedByWorkOrderHistory(historyId: Long) =
        workPerformedRepository.getWorkPerformedCombinedByWorkOrderHistory(historyId)

    fun getWorkPerformedHistoryById(historyWorkPerformedId: Long) =
        workPerformedRepository.getWorkPerformedHistoryById(historyWorkPerformedId)

    suspend fun getOrCreateWorkPerformed(description: String): WorkPerformed? {
        if (description.isBlank()) return null
        val existing = getWorkPerformedAllSync().find {
            it.wpDescription.trim().equals(description.trim(), ignoreCase = true)
        }
        if (existing != null) return existing

        val nf = NumberFunctions()
        val df = DateFunctions()
        val newWorkPerformed = WorkPerformed(
            nf.generateRandomIdAsLong(),
            description.trim(),
            false,
            df.getCurrentUTCTimeAsString()
        )
        workPerformedRepository.insertWorkPerformed(newWorkPerformed)
        return newWorkPerformed
    }
}