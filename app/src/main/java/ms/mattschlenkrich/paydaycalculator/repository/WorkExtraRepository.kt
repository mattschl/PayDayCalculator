package ms.mattschlenkrich.paydaycalculator.repository

import ms.mattschlenkrich.paydaycalculator.database.PayDatabase
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraFrequencies

class WorkExtraRepository(private val db: PayDatabase) {
    suspend fun insertExtraFrequency(extraFrequency: WorkExtraFrequencies) =
        db.getWorkExtraDao().insertExtraFrequency(extraFrequency)

    suspend fun updateExtraFrequency(extraFrequency: WorkExtraFrequencies) =
        db.getWorkExtraDao().updateExtraFrequency(extraFrequency)

    fun getWorkExtraFrequency() =
        db.getWorkExtraDao().getWorkExtraFrequency()
}