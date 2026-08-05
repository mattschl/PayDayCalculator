package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import ms.mattschlenkrich.paycalculator.common.DEVICE_ID
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.common.PREFS_NAME
import ms.mattschlenkrich.paycalculator.common.settings.Settings
import ms.mattschlenkrich.paycalculator.common.settings.SettingsManager
import ms.mattschlenkrich.paycalculator.data.entity.EmployerPayRates
import ms.mattschlenkrich.paycalculator.data.entity.Employers
import ms.mattschlenkrich.paycalculator.data.entity.Material
import ms.mattschlenkrich.paycalculator.data.entity.PayPeriods
import ms.mattschlenkrich.paycalculator.data.entity.TaxTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkDateExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkDates
import ms.mattschlenkrich.paycalculator.data.entity.WorkExtraTypes
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrder
import ms.mattschlenkrich.paycalculator.data.entity.WorkOrderHistory
import ms.mattschlenkrich.paycalculator.data.entity.WorkPayPeriodExtras
import ms.mattschlenkrich.paycalculator.data.entity.WorkTaxRules
import ms.mattschlenkrich.paycalculator.data.model.ExtraDefTypeAndEmployer
import ms.mattschlenkrich.paycalculator.data.model.TempWorkOrderHistoryInfo
import ms.mattschlenkrich.paycalculator.data.model.WorkOrderHistoryTimeWorkedCombined

private const val SELECTED_EMPLOYER_ID = "selected_employer_id"

class MainViewModel(
    app: Application,
) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val settingsManager = SettingsManager(app)

    var selectedEmployer = mutableStateOf<Employers?>(null)
        private set
    var selectedCutOffDate = mutableStateOf("")
        private set
    var selectedEmployerId = settingsManager.loadSettings().defaultEmployerId
        ?: prefs.getLong(SELECTED_EMPLOYER_ID, -1L)
        private set

    var deviceId: Long = 0L
        private set

    init {
        deviceId = prefs.getLong(DEVICE_ID, 0L)
        if (deviceId == 0L) {
            deviceId = NumberFunctions().generateRandomIdAsLong()
            prefs.edit { putLong(DEVICE_ID, deviceId) }
        }
    }

    var selectedTopLevelIndex = mutableIntStateOf(0)
        private set

    var isAuthenticated = mutableStateOf(false)
        private set

    fun setAuthenticated(authenticated: Boolean) {
        isAuthenticated.value = authenticated
    }

    fun setSelectedTopLevelIndex(index: Int) {
        selectedTopLevelIndex.intValue = index
    }

    private var employer: Employers? = null
    fun getEmployer(): Employers? = employer ?: selectedEmployer.value
    fun setEmployer(newEmployer: Employers?) {
        employer = newEmployer
        selectedEmployer.value = newEmployer
        selectedEmployerId = newEmployer?.employerId ?: -1L
        prefs.edit { putLong(SELECTED_EMPLOYER_ID, selectedEmployerId) }
    }

    private var taxType: TaxTypes? = null
    fun getTaxType(): TaxTypes? = taxType
    fun setTaxType(newTaxType: TaxTypes?) {
        taxType = newTaxType
    }

    private var taxTypeString: String? = null
    fun getTaxTypeString(): String? = taxTypeString
    fun setTaxTypeString(newType: String?) {
        taxTypeString = newType
    }

    private var taxRule: WorkTaxRules? = null
    fun getTaxRule(): WorkTaxRules? = taxRule
    fun setTaxRule(newTaxRule: WorkTaxRules?) {
        taxRule = newTaxRule
    }

    private var effectiveDateString: String? = null
    fun getEffectiveDateString(): String? = effectiveDateString
    fun setEffectiveDateString(newDate: String?) {
        effectiveDateString = newDate
    }

    private var taxLevel: Int? = null
    fun getTaxLevel(): Int? = taxLevel
    fun setTaxLevel(newLevel: Int?) {
        taxLevel = newLevel
    }

    private var extraDefinitionFull: ExtraDefTypeAndEmployer? = null
    fun getExtraDefinitionFull(): ExtraDefTypeAndEmployer? = extraDefinitionFull
    fun setExtraDefinitionFull(newExtra: ExtraDefTypeAndEmployer?) {
        extraDefinitionFull = newExtra
    }

    private var extraType: WorkExtraTypes? = null
    fun getWorkExtraType(): WorkExtraTypes? = extraType
    fun setWorkExtraType(newExtra: WorkExtraTypes?) {
        extraType = newExtra
    }

    private var workDateExtraList = ArrayList<WorkDateExtras>()
    fun getWorkDateExtraList(): ArrayList<WorkDateExtras> = workDateExtraList
    fun setWorkDateExtraList(extraList: ArrayList<WorkDateExtras>) {
        workDateExtraList = extraList
    }

    private var workDateExtra: WorkDateExtras? = null
    fun getWorkDateExtra(): WorkDateExtras? = workDateExtra
    fun setWorkDateExtra(newExtra: WorkDateExtras?) {
        workDateExtra = newExtra
    }

    private var workDateObject: WorkDates? = null
    fun getWorkDateObject(): WorkDates? = workDateObject
    fun setWorkDateObject(newDate: WorkDates?) {
        workDateObject = newDate
    }

    private var cutOffDate: String? = null
    fun getCutOffDate(): String? = cutOffDate
    fun setCutOffDate(date: String?) {
        cutOffDate = date
        selectedCutOffDate.value = date ?: ""
    }

    private var payPeriod: PayPeriods? = null
    fun getPayPeriod(): PayPeriods? = payPeriod
    fun setPayPeriod(newPayPeriod: PayPeriods?) {
        payPeriod = newPayPeriod
    }

    private var payRate: EmployerPayRates? = null
    fun getPayRate(): EmployerPayRates? = payRate
    fun setPayRate(newRate: EmployerPayRates?) {
        payRate = newRate
    }

    private var payPeriodExtra: WorkPayPeriodExtras? = null
    fun getPayPeriodExtra(): WorkPayPeriodExtras? = payPeriodExtra
    fun setPayPeriodExtra(newExtra: WorkPayPeriodExtras?) {
        payPeriodExtra = newExtra
    }

    private var tempWorkOrderHistoryInfo: TempWorkOrderHistoryInfo? = null
    fun getTempWorkOrderHistoryInfo(): TempWorkOrderHistoryInfo? = tempWorkOrderHistoryInfo
    fun setTempWorkOrderHistoryInfo(newInfo: TempWorkOrderHistoryInfo?) {
        tempWorkOrderHistoryInfo = newInfo
    }

    private var workOrderHistory: WorkOrderHistory? = null
    fun getWorkOrderHistory(): WorkOrderHistory? = workOrderHistory
    fun setWorkOrderHistory(newWorkOrderHistory: WorkOrderHistory?) {
        workOrderHistory = newWorkOrderHistory
    }

    private var workOrderNumber: String? = null
    fun getWorkOrderNumber(): String? = workOrderNumber
    fun setWorkOrderNumber(newWorkOrderNumber: String?) {
        workOrderNumber = newWorkOrderNumber
    }

    private var workOrder: WorkOrder? = null
    fun getWorkOrder(): WorkOrder? = workOrder
    fun setWorkOrder(newWorkOrder: WorkOrder?) {
        workOrder = newWorkOrder
    }

    private var workOrderJobSpecId: Long? = null
    fun getWorkOrderJobSpecId(): Long? = workOrderJobSpecId
    fun setWorkOrderJobSpecId(newWorkOrderJobSpecId: Long?) {
        workOrderJobSpecId = newWorkOrderJobSpecId
    }

    private var jobSpecId: Long? = null
    fun getJobSpecId(): Long? = jobSpecId
    fun setJobSpecId(newJobSpecId: Long?) {
        jobSpecId = newJobSpecId
    }

    private var jobSpecIsMaster: Boolean = true
    fun getJobSpecIsMaster(): Boolean = jobSpecIsMaster
    fun setJobSpecIsMaster(isMaster: Boolean) {
        jobSpecIsMaster = isMaster
    }

    private var workPerformedId: Long? = null
    fun getWorkPerformedId(): Long? = workPerformedId
    fun setWorkPerformedId(newWorkPerformedId: Long?) {
        workPerformedId = newWorkPerformedId
    }

    private var workPerformedIsParent: Boolean = true
    fun getWorkPerformedIsMaster(): Boolean = workPerformedIsParent
    fun setWorkPerformedIsMaster(isMaster: Boolean) {
        workPerformedIsParent = isMaster
    }

    private var workPerformedHistoryId: Long? = null
    fun getWorkPerformedHistoryId(): Long? = workPerformedHistoryId
    fun setWorkPerformedHistoryId(newWorkPerformedHistoryId: Long?) {
        workPerformedHistoryId = newWorkPerformedHistoryId
    }

    private var material: Material? = null
    fun getMaterial(): Material? = material
    fun setMaterial(newMaterial: Material?) {
        material = newMaterial
    }

    private var materialId: Long? = null
    fun getMaterialId(): Long? = materialId
    fun setMaterialId(newMaterialId: Long?) {
        materialId = newMaterialId
    }

    private var materialIsParent: Boolean = true
    fun getMaterialIsParent(): Boolean = materialIsParent
    fun setMaterialIsParent(isParent: Boolean) {
        materialIsParent = isParent
    }

    private var areaId: Long? = null
    fun getAreaId(): Long? = areaId
    fun setAreaId(newAreaId: Long?) {
        areaId = newAreaId
    }

    private var workOrderHistoryTimeWorkedCombined: WorkOrderHistoryTimeWorkedCombined? = null
    fun getWorkOrderHistoryTimeWorkedCombined(): WorkOrderHistoryTimeWorkedCombined? =
        workOrderHistoryTimeWorkedCombined

    fun setWorkOrderHistoryTimeWorkedCombined(newWorkOrderHistoryTimeWorkedCombined: WorkOrderHistoryTimeWorkedCombined?) {
        workOrderHistoryTimeWorkedCombined = newWorkOrderHistoryTimeWorkedCombined
    }

    fun loadSettings(): Settings {
        return settingsManager.loadSettings()
    }
}