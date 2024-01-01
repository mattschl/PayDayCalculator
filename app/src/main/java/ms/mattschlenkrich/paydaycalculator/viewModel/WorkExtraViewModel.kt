package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.model.WorkExtrasDefinitions
import ms.mattschlenkrich.paydaycalculator.repository.WorkExtraRepository

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

    fun getWorkExtraDefinitions(employerId: Long) =
        workExtraRepository.getWorkExtraDefinitions(employerId)

    fun getActiveExtraDefinitionsFull(employerId: Long) =
        workExtraRepository.getActiveExtraDefinitionsFull(employerId)

}