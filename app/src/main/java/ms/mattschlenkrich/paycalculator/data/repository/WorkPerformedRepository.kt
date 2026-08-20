package ms.mattschlenkrich.paycalculator.data.repository

import ms.mattschlenkrich.paycalculator.data.PayDatabase
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistoryWorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformed
import ms.mattschlenkrich.paycalculator.data.entity.WorkPerformedMerged

class WorkPerformedRepository(private val db: PayDatabase) {
    suspend fun insertWorkPerformed(workPerformed: WorkPerformed) {
        val existing = db.getWorkPerformedDao().getWorkPerformedAnySync(workPerformed.wpDescription)
        if (existing != null) {
            val updated = workPerformed.copy(
                workPerformedId = existing.workPerformedId,
                wpIsDeleted = false
            )
            db.getWorkPerformedDao().updateWorkPerformed(updated)
        } else {
            db.getWorkPerformedDao().insertWorkPerformed(workPerformed)
        }
    }

    suspend fun deleteWorkPerformed(workPerformedId: Long, updateTime: String) =
        db.getWorkPerformedDao().deleteWorkPerformed(workPerformedId, updateTime)

    suspend fun deleteWorkPerformedMerged(workPerformedMergedId: Long, updateTime: String) =
        db.getWorkPerformedDao().deleteWorkPerformedMerged(workPerformedMergedId, updateTime)

    fun getWorkPerformedAll() = db.getWorkPerformedDao().getWorkPerformedAll()
    suspend fun getWorkPerformedAllSync() = db.getWorkPerformedDao().getWorkPerformedAllSync()
    fun getWorkPerformedAndChildList(workPerformedId: Long) =
        db.getWorkPerformedDao().getWorkPerformedAndChildList(workPerformedId)

    suspend fun insertWorkPerformedMerged(workPerformedMerged: WorkPerformedMerged) =
        db.getWorkPerformedDao().insertWorkPerformedMerged(workPerformedMerged)

    suspend fun updateWorkPerformedMerged(oldWorkPerformedId: Long, newWorkPerformedId: Long) =
        db.getWorkPerformedDao().updateWorkPerformedMerged(oldWorkPerformedId, newWorkPerformedId)

    fun searchFromWorkPerformed(query: String) =
        db.getWorkPerformedDao().searchFromWorkPerformed(query)

    suspend fun getWorkPerformedSync(description: String) =
        db.getWorkPerformedDao().getWorkPerformedSync(description)

    fun getWorkPerformed(workPerformedId: Long) =
        db.getWorkPerformedDao().getWorkPerformed(workPerformedId)

    suspend fun updateWorkPerformed(workPerformed: WorkPerformed) =
        db.getWorkPerformedDao().updateWorkPerformed(workPerformed)

    suspend fun insertWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed) =
        db.getWorkPerformedDao().insertWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed)

    suspend fun updateWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed: WorkOrderHistoryWorkPerformed) =
        db.getWorkPerformedDao().updateWorkOrderHistoryWorkPerformed(workOrderHistoryWorkPerformed)

    suspend fun removeAllWorkPerformedFromWorkOrderHistory(historyId: Long, updateTime: String) =
        db.getWorkPerformedDao().removeAllWorkPerformedFromWorkOrderHistory(historyId, updateTime)

    suspend fun deleteWorkOrderHistoryWorkPerformed(
        historyWorkPerformedId: Long,
        updateTime: String
    ) = db.getWorkPerformedDao()
        .deleteWorkOrderHistoryWorkPerformed(historyWorkPerformedId, updateTime)

    fun getWorkPerformedCombinedByWorkOrderHistory(historyId: Long) =
        db.getWorkPerformedDao().getWorkPerformedByWorkOrderHistory(historyId)

    fun getWorkPerformedHistoryById(historyWorkPerformedId: Long) =
        db.getWorkPerformedDao().getWorkPerformedHistoryById(historyWorkPerformedId)
}