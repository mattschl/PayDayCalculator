package ms.mattschlenkrich.paycalculator.ui.extras

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.Screen
import ms.mattschlenkrich.paycalculator.common.DateFunctions
import ms.mattschlenkrich.paycalculator.data.EmployerViewModel
import ms.mattschlenkrich.paycalculator.data.ExtraDefTypeAndEmployer
import ms.mattschlenkrich.paycalculator.data.MainViewModel
import ms.mattschlenkrich.paycalculator.data.PayDayViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.WorkExtraViewModel
import ms.mattschlenkrich.paycalculator.data.WorkExtrasDefinitions

@Composable
fun PayPeriodExtraAddRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val payPeriod = mainViewModel.getPayPeriod() ?: return
    val employer = mainViewModel.getEmployer() ?: return

    val existingPayPeriodExtras by payDayViewModel.getPayPeriodExtras(payPeriod.payPeriodId)
        .observeAsState(emptyList())
    val existingWorkDateExtras by payDayViewModel.getWorkDateExtrasPerPay(
        employer.employerId, payPeriod.ppCutoffDate
    ).observeAsState(emptyList())
    val defaultExtras by workExtraViewModel.getExtraTypesAndDefByDaily(
        employer.employerId, payPeriod.ppCutoffDate
    ).observeAsState(emptyList())

    PayPeriodExtraScreen(
        curPayPeriod = payPeriod,
        employerName = employer.employerName,
        initialExtra = null,
        existingPayPeriodExtras = existingPayPeriodExtras,
        existingWorkDateExtras = existingWorkDateExtras,
        defaultExtras = defaultExtras,
        onUpdate = { extra ->
            payDayViewModel.insertPayPeriodExtra(extra)
            navController.popBackStack()
        },
        onDelete = {},
        onCancel = { navController.popBackStack() }
    )
}

@Composable
fun PayPeriodExtraUpdateRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val payPeriod = mainViewModel.getPayPeriod() ?: return
    val employer = mainViewModel.getEmployer() ?: return
    val initialExtra = mainViewModel.getPayPeriodExtra() ?: return

    val existingPayPeriodExtras by payDayViewModel.getPayPeriodExtras(payPeriod.payPeriodId)
        .observeAsState(emptyList())
    val existingWorkDateExtras by payDayViewModel.getWorkDateExtrasPerPay(
        employer.employerId, payPeriod.ppCutoffDate
    ).observeAsState(emptyList())
    val defaultExtras by workExtraViewModel.getExtraTypesAndDefByDaily(
        employer.employerId, payPeriod.ppCutoffDate
    ).observeAsState(emptyList())

    PayPeriodExtraScreen(
        curPayPeriod = payPeriod,
        employerName = employer.employerName,
        initialExtra = initialExtra,
        existingPayPeriodExtras = existingPayPeriodExtras,
        existingWorkDateExtras = existingWorkDateExtras,
        defaultExtras = defaultExtras,
        onUpdate = { extra ->
            payDayViewModel.updatePayPeriodExtra(extra)
            navController.popBackStack()
        },
        onDelete = { extra ->
            payDayViewModel.updatePayPeriodExtra(
                extra.copy(
                    ppeIsDeleted = true,
                    ppeUpdateTime = DateFunctions().getCurrentUTCTimeAsString()
                )
            )
            navController.popBackStack()
        },
        onCancel = { navController.popBackStack() }
    )
}

@Composable
fun ExtraRoute(
    mainViewModel: MainViewModel,
    employerViewModel: EmployerViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    EmployerExtraDefinitionsScreen(
        employerViewModel = employerViewModel,
        workExtraViewModel = workExtraViewModel,
        initialEmployer = mainViewModel.getEmployer(),
        initialExtraType = mainViewModel.getWorkExtraType(),
        onAddExtraDefinition = { employer, extraType ->
            mainViewModel.setEmployer(employer)
            mainViewModel.setWorkExtraType(extraType)
            navController.navigate(Screen.EmployerExtraDefinitionsAdd.route)
        },
        onUpdateExtraDefinition = { definition ->
            mainViewModel.setEmployer(definition.employer)
            mainViewModel.setExtraDefinitionFull(definition)
            navController.navigate(Screen.EmployerExtraDefinitionUpdate.route)
        },
        onUpdateExtraType = { employer, extraType ->
            mainViewModel.setEmployer(employer)
            mainViewModel.setWorkExtraType(extraType)
            navController.navigate(Screen.WorkExtraTypeUpdate.route)
        },
        onAddNewEmployer = {
            navController.navigate(Screen.EmployerAdd.route)
        },
        onAddNewExtraType = { employer ->
            mainViewModel.setEmployer(employer)
            navController.navigate(Screen.WorkExtraTypeAdd.route)
        },
        onDeleteExtraDefinition = { definition ->
            workExtraViewModel.deleteWorkExtraDefinition(
                definition.definition.workExtraDefId,
                DateFunctions()
                    .getCurrentUTCTimeAsString()
            )
        }
    )
}

@Composable
fun EmployerExtraDefinitionsAddRoute(
    mainViewModel: MainViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val curEmployer = mainViewModel.getEmployer()
    val curExtraType = mainViewModel.getWorkExtraType()

    EmployerExtraDefinitionScreen(
        initialDefinitionFull = if (curEmployer != null && curExtraType != null) {
            ExtraDefTypeAndEmployer(
                definition = WorkExtrasDefinitions(
                    workExtraDefId = 0L,
                    weEmployerId = curEmployer.employerId,
                    weExtraTypeId = curExtraType.workExtraTypeId,
                    weValue = 0.0,
                    weIsFixed = true,
                    weEffectiveDate = DateFunctions()
                        .getCurrentDateAsString(),
                    weIsDeleted = false,
                    weUpdateTime = DateFunctions()
                        .getCurrentUTCTimeAsString()
                ),
                employer = curEmployer,
                extraType = curExtraType
            )
        } else null,
        onUpdate = { definition ->
            workExtraViewModel.insertWorkExtraDefinition(definition)
            navController.popBackStack()
        },
        onDelete = { /* Not applicable for Add */ },
        onCancel = { navController.popBackStack() }
    )
}

@Composable
fun EmployerExtraDefinitionUpdateRoute(
    mainViewModel: MainViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val definitionFull = mainViewModel.getExtraDefinitionFull()

    EmployerExtraDefinitionScreen(
        initialDefinitionFull = definitionFull,
        onUpdate = { definition ->
            workExtraViewModel.updateWorkExtraDefinition(definition)
            navController.popBackStack()
        },
        onDelete = { definition ->
            workExtraViewModel.deleteWorkExtraDefinition(
                definition.workExtraDefId,
                DateFunctions()
                    .getCurrentUTCTimeAsString()
            )
            navController.popBackStack()
        },
        onCancel = { navController.popBackStack() }
    )
}

@Composable
fun WorkExtraTypeAddRoute(
    mainViewModel: MainViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val curEmployer = mainViewModel.getEmployer()
    if (curEmployer != null) {
        val extraTypeList by workExtraViewModel.getExtraDefTypes(curEmployer.employerId)
            .observeAsState(emptyList())

        WorkExtraTypeScreen(
            initialEmployer = curEmployer,
            initialExtraType = null,
            existingExtraTypes = extraTypeList,
            onUpdate = { newExtraType: WorkExtraTypes ->
                workExtraViewModel.insertWorkExtraType(newExtraType)
                mainViewModel.setWorkExtraType(newExtraType)
                navController.navigate(Screen.Extras.route) {
                    popUpTo(Screen.WorkExtraTypeAdd.route) { inclusive = true }
                }
            },
            onDelete = {}
        )
    } else {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }
}

@Composable
fun WorkExtraTypeUpdateRoute(
    mainViewModel: MainViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val curEmployer = mainViewModel.getEmployer()
    val curExtraType = mainViewModel.getWorkExtraType()
    if (curEmployer != null && curExtraType != null) {
        val extraTypeList by workExtraViewModel.getExtraDefTypes(curEmployer.employerId)
            .observeAsState(emptyList())

        WorkExtraTypeScreen(
            initialEmployer = curEmployer,
            initialExtraType = curExtraType,
            existingExtraTypes = extraTypeList,
            onUpdate = { updatedExtraType: WorkExtraTypes ->
                workExtraViewModel.updateWorkExtraType(updatedExtraType)
                mainViewModel.setWorkExtraType(updatedExtraType)
                navController.popBackStack()
            },
            onDelete = { extraTypeToDelete: WorkExtraTypes ->
                workExtraViewModel.updateWorkExtraType(
                    WorkExtraTypes(
                        extraTypeToDelete.workExtraTypeId,
                        extraTypeToDelete.wetName,
                        extraTypeToDelete.wetEmployerId,
                        extraTypeToDelete.wetAppliesTo,
                        extraTypeToDelete.wetAttachTo,
                        extraTypeToDelete.wetIsCredit,
                        extraTypeToDelete.wetIsDefault,
                        true,
                        DateFunctions().getCurrentUTCTimeAsString()
                    )
                )
                navController.popBackStack()
            }
        )
    } else {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }
}

@Composable
fun WorkDateExtraAddRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val workDate = mainViewModel.getWorkDateObject() ?: return
    val employer = mainViewModel.getEmployer() ?: return

    val existingExtras by payDayViewModel.getWorkDateExtras(workDate.workDateId)
        .observeAsState(emptyList())

    WorkDateExtraScreen(
        initialWorkDate = workDate,
        employerName = employer.employerName,
        initialExtra = null,
        existingExtras = existingExtras,
        onUpdate = { extra ->
            payDayViewModel.insertWorkDateExtra(extra)
            navController.popBackStack()
        },
        onDelete = {},
        onCancel = { navController.popBackStack() }
    )
}

@Composable
fun WorkDateExtraUpdateRoute(
    mainViewModel: MainViewModel,
    payDayViewModel: PayDayViewModel,
    workExtraViewModel: WorkExtraViewModel,
    navController: NavController
) {
    val workDate = mainViewModel.getWorkDateObject() ?: return
    val initialExtra = mainViewModel.getWorkDateExtra() ?: return
    val employer = mainViewModel.getEmployer() ?: return

    val existingExtras by payDayViewModel.getWorkDateExtras(workDate.workDateId)
        .observeAsState(emptyList())

    WorkDateExtraScreen(
        initialWorkDate = workDate,
        employerName = employer.employerName,
        initialExtra = initialExtra,
        existingExtras = existingExtras,
        onUpdate = { extra ->
            payDayViewModel.updateWorkDateExtra(extra)
            navController.popBackStack()
        },
        onDelete = { extra ->
            payDayViewModel.updateWorkDateExtra(
                extra.copy(
                    wdeIsDeleted = true,
                    wdeUpdateTime = DateFunctions().getCurrentUTCTimeAsString()
                )
            )
            navController.popBackStack()
        },
        onCancel = { navController.popBackStack() }
    )
}