package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.repository.MaterialRepository
import org.junit.Before
import org.junit.Test

class MaterialViewModelTest {

    private lateinit var viewModel: MaterialViewModel
    private val repository: MaterialRepository = mockk()
    private val application: Application = mockk()

    @Before
    fun setup() {
        viewModel = MaterialViewModel(application, repository)
    }

    @Test
    fun testUpdateMaterialCostAndPrice_UpdatesRepository() = runBlocking {
        val materialId = 1L
        val originalMaterial = Material(materialId, "Test Material", 5.0, 10.0, false, "oldTime")
        val newCost = 6.0
        val newPrice = 12.0

        coEvery { repository.getMaterialSync(materialId) } returns originalMaterial
        coEvery { repository.updateMaterial(any()) } returns Unit

        viewModel.updateMaterialCostAndPrice(materialId, newCost, newPrice)

        coVerify {
            repository.updateMaterial(match {
                it.materialId == materialId &&
                        it.mCost == newCost &&
                        it.mPrice == newPrice &&
                        it.mUpdateTime != "oldTime"
            })
        }
    }
}