package ms.mattschlenkrich.paycalculator.data

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

//    fun getWorkExtraDefinitions(employerId: Long) =
//        workExtraRepository.getWorkExtraDefinitions(employerId)
//
//    fun getWorkExtraDefinitions(employerId: Long, extraTypeId: Long) =
//        workExtraRepository.getWorkExtraDefinitions(employerId, extraTypeId)

    fun getActiveExtraDefinitionsFull(employerId: Long, extraTypeId: Long) =
        workExtraRepository.getActiveExtraDefinitionsFull(employerId, extraTypeId)

//    fun getExtraDefinitionsPerDay(employerId: Long) =
//        workExtraRepository.getExtraDefinitionsPerDay(employerId)

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

    fun deleteWorkExtraType(id: Long, updateTime: String) = viewModelScope.launch {
        workExtraRepository.deleteWorkExtraType(id, updateTime)
    }

    fun getWorkExtraTypeList(employerId: Long) =
        workExtraRepository.getWorkExtraTypeList(employerId)

    fun getExtraTypesAndDefByDaily(employerId: Long, cutoffDate: String) =
        workExtraRepository.getExtraTypesAndDefByDaily(employerId, cutoffDate)

    fun getExtraTypesByDaily(employerId: Long) =
        workExtraRepository.getExtraTypesByDaily(employerId)

    fun getExtraTypeAndDefByTypeId(typeId: Long, cutoffDate: String) =
        workExtraRepository.getExtraTypeAndDefByTypeId(typeId, cutoffDate)

    suspend fun getExtraTypeAndDefByTypeIdSync(typeId: Long, cutoffDate: String) =
        workExtraRepository.getExtraTypeAndDefByTypeIdSync(typeId, cutoffDate)

    fun getExtraTypesAndDef(employerId: Long, cutoffDate: String, appliesTo: Int) =
        workExtraRepository.getExtraTypesAndDef(employerId, cutoffDate, appliesTo)

    fun insertWorkDateExtra(extra: WorkDateExtras) =
        viewModelScope.launch {
            workExtraRepository.insertWorkDateExtra(extra)
        }

    fun deleteWorkDateExtra(id: Long, updateTime: String) = viewModelScope.launch {
        workExtraRepository.deleteWorkDateExtra(id, updateTime)
    }

    fun updateWorkDateExtra(extra: WorkDateExtras) =
        viewModelScope.launch {
            workExtraRepository.updateWorkDateExtra(extra)
        }
//
//    fun getWorkDateExtras(workDateId: Long) =
//        workExtraRepository.getWorkDateExtras(workDateId)

    fun getDefaultExtraTypesAndCurrentDef(employerId: Long, cutoffDate: String) =
        workExtraRepository.getDefaultExtraTypesAndCurrentDef(employerId, cutoffDate)
}