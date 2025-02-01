package ms.mattschlenkrich.paycalculator.payfunctions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ms.mattschlenkrich.paycalculator.common.AppliesToFrequencies
import ms.mattschlenkrich.paycalculator.common.AttachToFrequencies
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraContainer
import ms.mattschlenkrich.paycalculator.database.model.extras.ExtraDefinitionAndType
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDateExtras
import ms.mattschlenkrich.paycalculator.database.model.payperiod.WorkDates
import ms.mattschlenkrich.paycalculator.ui.MainActivity

class WorkDateExtraContainerCalculations(
    private val mainActivity: MainActivity,
    private val workDate: WorkDates,
    private val wage: Double,
) {
    private lateinit var workDateExtraList: List<WorkDateExtras>
    private lateinit var extraDefinitionAndTypeList: List<ExtraDefinitionAndType>
    private var extraContainers = ArrayList<ExtraContainer>()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            getCustomWorkDateExtrasFromDb()
            getDefaultExtraTypeAndDefFromDb()
            sortIntoSingleExtraContainerList()
        }
    }

    private fun sortIntoSingleExtraContainerList() {
        CoroutineScope(Dispatchers.Default).launch {
            if (workDateExtraList.isNotEmpty()) {
                for (extra in workDateExtraList) {
                    if (!extra.wdeIsDeleted) {
                        val amount: Double =
                            if (extra.wdeIsFixed) {
                                when (extra.wdeAppliesTo) {
                                    AppliesToFrequencies.Hourly.value -> {
                                        extra.wdeValue *
                                                (workDate.wdRegHours +
                                                        workDate.wdOtHours +
                                                        workDate.wdDblOtHours)
                                    }

                                    AppliesToFrequencies.Daily.value -> {
                                        extra.wdeValue
                                    }

                                    else -> {
                                        0.0
                                    }
                                }
                            } else {
                                extra.wdeValue * wage *
                                        (workDate.wdRegHours +
                                                workDate.wdOtHours * 1.5
                                                + workDate.wdDblOtHours * 2)

                            }
                        if (amount > 0.0) {
                            extraContainers.add(
                                ExtraContainer(
                                    extra.wdeName,
                                    amount,
                                    null,
                                    extra,
                                    null
                                )
                            )
                        }
                    }
                }
            }
            if (extraDefinitionAndTypeList.isNotEmpty()) {
                for (extra in extraDefinitionAndTypeList) {

                    if (!extra.extraType.wetIsDeleted &&
                        !extra.definition.weIsDeleted &&
                        extra.extraType.wetIsDefault
                    ) {
                        val amount: Double =
                            if (extra.definition.weIsFixed) {
                                when (extra.extraType.wetAppliesTo) {
                                    AppliesToFrequencies.Hourly.value -> {
                                        extra.definition.weValue *
                                                (workDate.wdRegHours +
                                                        workDate.wdOtHours +
                                                        workDate.wdDblOtHours)
                                    }

                                    AppliesToFrequencies.Daily.value -> {
                                        extra.definition.weValue
                                    }

                                    else -> {
                                        0.0
                                    }
                                }
                            } else {
                                extra.definition.weValue * wage *
                                        (workDate.wdRegHours + workDate.wdOtHours + workDate.wdDblOtHours)
                            }
                        if (amount > 0.0) {
                            extraContainers.add(
                                ExtraContainer(
                                    extra.extraType.wetName,
                                    amount,
                                    extra,
                                    null,
                                    null
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
            workDate.wdEmployerId, workDate.wdCutoffDate, AttachToFrequencies.Daily.value
        )
    }

    private suspend fun getCustomWorkDateExtrasFromDb() =
        withContext(Dispatchers.Default) {
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