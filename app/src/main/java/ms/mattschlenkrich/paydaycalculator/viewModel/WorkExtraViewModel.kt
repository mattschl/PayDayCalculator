package ms.mattschlenkrich.paydaycalculator.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.paydaycalculator.model.WorkExtraFrequencies
import ms.mattschlenkrich.paydaycalculator.repository.WorkExtraRepository

class WorkExtraViewModel(
    app: Application,
    private val workExtraRepository: WorkExtraRepository
) : AndroidViewModel(app) {

    fun insertExtraFrequency(extraFrequency: WorkExtraFrequencies) =
        viewModelScope.launch {
            workExtraRepository.insertExtraFrequency(extraFrequency)
        }

    fun updateExtraFrequency(extraFrequency: WorkExtraFrequencies) =
        viewModelScope.launch {
            workExtraRepository.updateExtraFrequency(extraFrequency)
        }

    fun getWorkExtraFrequency() =
        workExtraRepository.getWorkExtraFrequency()
}