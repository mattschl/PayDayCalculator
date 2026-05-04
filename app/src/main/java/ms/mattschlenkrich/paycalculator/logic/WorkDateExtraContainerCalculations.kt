package ms.mattschlenkrich.paycalculator.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.MainActivity
import ms.mattschlenkrich.paycalculator.common.ExtraAppliesToFrequencies
import ms.mattschlenkrich.paycalculator.common.ExtraAttachToFrequencies
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.model.ExtraContainer
import ms.mattschlenkrich.paycalculator.data.model.ExtraDefinitionAndType

class WorkDateExtraContainerCalculations(
    private val mainActivity: MainActivity,
    private val workDate: WorkDates,
    private val wage: Double,
) {
    private lateinit var workDateExtraList: List<WorkDateExtras>
    private lateinit var extraDefinitionAndTypeList: List<ExtraDefinitionAndType>
    private var extraContainers = ArrayList<ExtraContainer>()

    private val defaultScope = CoroutineScope(Dispatchers.Default)

    init {
        defaultScope.launch {
            getCustomWorkDateExtrasFromDb()
            getDefaultExtraTypeAndDefFromDb()
            sortIntoSingleExtraContainerList()
        }
    }

    private fun sortIntoSingleExtraContainerList() {
        defaultScope.launch {
            if (workDateExtraList.isNotEmpty()) {
                for (extra in workDateExtraList) {
                    if (!extra.wdeIsDeleted) {
                        val value = if (!extra.wdeIsFixed && extra.wdeValue >= 1.0)
                            extra.wdeValue / 100.0
                        else extra.wdeValue
                        val amount: Double = if (extra.wdeIsFixed) {
                            when (extra.wdeAppliesTo) {
                                ExtraAppliesToFrequencies.HOURLY.value -> {
                                    value * (workDate.wdRegHours + workDate.wdOtHours + workDate.wdDblOtHours)
                                }

                                ExtraAppliesToFrequencies.DAILY.value -> {
                                    value
                                }

                                else -> {
                                    0.0
                                }
                            }
                        } else {
                            value * wage * (workDate.wdRegHours + workDate.wdOtHours * 1.5 + workDate.wdDblOtHours * 2.0)

                        }
                        if (amount > 0.0) {
                            extraContainers.add(
                                ExtraContainer(
                                    extra.wdeName, amount, null, extra, null
                                )
                            )
                        }
                    }
                }
            }
            if (extraDefinitionAndTypeList.isNotEmpty()) {
                for (extra in extraDefinitionAndTypeList) {

                    if (!extra.extraType.wetIsDeleted && !extra.definition.weIsDeleted && extra.extraType.wetIsDefault) {
                        val value =
                            if (!extra.definition.weIsFixed && extra.definition.weValue >= 1.0)
                                extra.definition.weValue / 100.0
                            else extra.definition.weValue
                        val amount: Double = if (extra.definition.weIsFixed) {
                            when (extra.extraType.wetAppliesTo) {
                                ExtraAppliesToFrequencies.HOURLY.value -> {
                                    value * (workDate.wdRegHours + workDate.wdOtHours + workDate.wdDblOtHours)
                                }

                                ExtraAppliesToFrequencies.DAILY.value -> {
                                    value
                                }

                                else -> {
                                    0.0
                                }
                            }
                        } else {
                            value * wage * (workDate.wdRegHours + workDate.wdOtHours * 1.5 + workDate.wdDblOtHours * 2.0)
                        }
                        if (amount > 0.0) {
                            extraContainers.add(
                                ExtraContainer(
                                    extra.extraType.wetName, amount, extra, null, null
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun getDefaultExtraTypeAndDefFromDb() {
        withContext(Dispatchers.Default) {
            val defaultExtraTypeAndDefDeferred = async { getExtraTypeAndDefByDay() }
            extraDefinitionAndTypeList = defaultExtraTypeAndDefDeferred.await()
        }
    }

    private fun getExtraTypeAndDefByDay(): List<ExtraDefinitionAndType> {
        return mainActivity.payDetailViewModel.getExtraTypeAndDefBy(
            workDate.wdEmployerId, workDate.wdCutoffDate, ExtraAttachToFrequencies.DAILY.value
        )
    }

    private suspend fun getCustomWorkDateExtrasFromDb() = withContext(Dispatchers.Default) {
        val customExtraListDeferred = async { getCustomWorkDateExtrasByDate() }
        workDateExtraList = customExtraListDeferred.await()
    }

    private fun getCustomWorkDateExtrasByDate(): List<WorkDateExtras> {
        return mainActivity.payDetailViewModel.getCustomWorkDateExtras(
            workDate.workDateId
        )
    }

    fun getExtraContainerList(): List<ExtraContainer> {
        return extraContainers
    }

}