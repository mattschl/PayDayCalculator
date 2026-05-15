package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtrasDefinitions
import ms.mattschlenkrich.paycalculator.data.repository.WorkExtraRepository

class WorkExtraViewModel(
    app: Application,
    private val workExtraRepository: WorkExtraRepository
) : AndroidViewModel(app) {

    fun insertWorkExtraDefinition(definition: WorkExtrasDefinitions) =
        viewModelScope.launch {
            workExtraRepository.insertWorkExtraDefinition(definition)
        }

    fun updateWorkExtraDefinition(definition: WorkExtrasDefinitions) =
        viewModelScope.launch {
            workExtraRepository.updateWorkExtraDefinition(definition)
        }

    fun deleteWorkExtraDefinition(id: Long, updateTime: String) =
        viewModelScope.launch {
            workExtraRepository.deleteWorkExtraDefinition(id, updateTime)
        }

    fun getActiveExtraDefinitionsFull(employerId: Long, extraTypeId: Long) =
        workExtraRepository.getActiveExtraDefinitionsFull(employerId, extraTypeId)

    fun getExtraDefTypes(employerId: Long) =
        workExtraRepository.getExtraDefTypes(employerId)

    fun insertWorkExtraType(workExtraType: WorkExtraTypes) =
        viewModelScope.launch {
            workExtraRepository.insertWorkExtraType(workExtraType)
        }

    fun updateWorkExtraType(extraType: WorkExtraTypes) =
        viewModelScope.launch {
            workExtraRepository.updateWorkExtraType(extraType)
        }

    fun getWorkExtraTypeList(employerId: Long) =
        workExtraRepository.getWorkExtraTypeList(employerId)

    fun getExtraTypesAndDefByDaily(employerId: Long, cutoffDate: String) =
        workExtraRepository.getExtraTypesAndDefByDaily(employerId, cutoffDate)

    fun getExtraTypesByDaily(employerId: Long) =
        workExtraRepository.getExtraTypesByDaily(employerId)

    suspend fun getExtraTypeAndDefByTypeIdSync(typeId: Long, cutoffDate: String) =
        workExtraRepository.getExtraTypeAndDefByTypeIdSync(typeId, cutoffDate)

    fun insertWorkDateExtra(extra: WorkDateExtras) =
        viewModelScope.launch {
            workExtraRepository.insertWorkDateExtra(extra)
        }

    fun updateWorkDateExtra(extra: WorkDateExtras) =
        viewModelScope.launch {
            workExtraRepository.updateWorkDateExtra(extra)
        }
}