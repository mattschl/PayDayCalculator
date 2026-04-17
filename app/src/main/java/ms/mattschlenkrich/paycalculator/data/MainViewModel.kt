package ms.mattschlenkrich.paycalculator.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class MainViewModel(
    app: Application,
) : AndroidViewModel(app) {
    private var employer: Employers? = null
    private var employerString: String? = null
    private var taxType: TaxTypes? = null
    private var taxTypeString: String? = null
    private var taxRule: WorkTaxRules? = null
    private var effectiveDateString: String? = null
    private var taxLevel: Int? = null
    private var callingFragment: String? = null
    private var extraDefinitionFull: ExtraDefTypeAndEmployer? = null
    private var extraType: WorkExtraTypes? = null
    private var workDateExtraList = ArrayList<WorkDateExtras>()
    private var workDateExtra: WorkDateExtras? = null
    private var workDate: String? = null
    private var workDateObject: WorkDates? = null
    private var cutOffDate: String? = null
    private var payPeriod: PayPeriods? = null
    private var payRate: EmployerPayRates? = null
    private var isCredit = false
    private var payPeriodExtra: WorkPayPeriodExtras? = null
    private var payPeriodExtraList = ArrayList<WorkPayPeriodExtras>()
    private var tempWorkOrderHistoryInfo: TempWorkOrderHistoryInfo? = null
    private var workOrderHistory: WorkOrderHistory? = null
    private var workOrderNumber: String? = null
    private var workOrder: WorkOrder? = null
    private var jobSpec: JobSpec? = null
    private var workOrderJobSpecId: Long? = null
    private var jobSpecId: Long? = null
    private var jobSpecIsMaster: Boolean = true
    private var workPerformedId: Long? = null
    private var workPerformedIsParent: Boolean = true
    private var workPerformedHistoryId: Long? = null
    private var material: Material? = null
    private var materialId: Long? = null
    private var materialIsParent: Boolean = true
    private var materialInSequence: MaterialInSequence? = null
    private var extraContainer: ExtraContainer? = null
    private var areaId: Long? = null
    private var workOrderHistoryTimeWorkedCombined: WorkOrderHistoryTimeWorkedCombined? = null

    fun getMaterialIsParent(): Boolean {
        return materialIsParent
    }

    fun setMaterialIsParent(isParent: Boolean) {
        materialIsParent = isParent
    }


    fun setMaterialId(newMaterialId: Long?) {
        materialId = newMaterialId
    }

    fun getMaterialId(): Long? {
        return materialId
    }

    fun setWorkOrderHistoryTimeWorkedCombined(newWorkOrderHistoryTimeWorkedCombined: WorkOrderHistoryTimeWorkedCombined?) {
        workOrderHistoryTimeWorkedCombined = newWorkOrderHistoryTimeWorkedCombined
    }

    fun getWorkOrderHistoryTimeWorkedCombined(): WorkOrderHistoryTimeWorkedCombined? {
        return workOrderHistoryTimeWorkedCombined

    }

    fun getWorkOrderJobSpecId(): Long? {
        return workOrderJobSpecId
    }

    fun setWorkOrderJobSpecId(newWorkOrderJobSpecId: Long?) {
        workOrderJobSpecId = newWorkOrderJobSpecId
    }

    fun getJobSpecId(): Long? {
        return jobSpecId
    }

    fun setJobSpecId(newJobSpecId: Long?) {
        jobSpecId = newJobSpecId
    }

    fun setJobSpecIsMaster(isMaster: Boolean) {
        jobSpecIsMaster = isMaster
    }

    fun getJobSpecIsMaster(): Boolean {
        return jobSpecIsMaster
    }

    fun setWorkPerformedHistoryId(newWorkPerformedHistoryId: Long?) {
        workPerformedHistoryId = newWorkPerformedHistoryId
    }

    fun getWorkPerformedHistoryId(): Long? {
        return workPerformedHistoryId
    }

    fun setWorkPerformedIsMaster(isMaster: Boolean) {
        workPerformedIsParent = isMaster
    }

    fun getWorkPerformedIsMaster(): Boolean {
        return workPerformedIsParent
    }

    fun setAreaId(newAreaId: Long?) {
        areaId = newAreaId
    }

    fun getAreaId(): Long? {
        return areaId
    }

    fun setExtraContainer(newExtraContainer: ExtraContainer?) {
        extraContainer = newExtraContainer
    }

    fun getExtraContainer(): ExtraContainer? {
        return extraContainer
    }

    fun setMaterialInSequence(newMaterial: MaterialInSequence?) {
        materialInSequence = newMaterial
    }

    fun getMaterialInSequence(): MaterialInSequence? {
        return materialInSequence
    }

    fun setMaterial(newMaterial: Material?) {
        material = newMaterial
    }

    fun getMaterial(): Material? {
        return material
    }

    fun setWorkPerformedId(newWorkPerformedId: Long?) {
        workPerformedId = newWorkPerformedId
    }

    fun getWorkPerformedId(): Long? {
        return workPerformedId
    }

    fun getJobSpec(): JobSpec? {
        return jobSpec
    }

    fun setJobSpec(newJobSpec: JobSpec?) {
        jobSpec = newJobSpec
    }

    fun getWorkOrder(): WorkOrder? {
        return workOrder
    }

    fun setWorkOrder(newWorkOrder: WorkOrder?) {
        workOrder = newWorkOrder
    }

    fun setWorkOrderNumber(newWorkOrderNumber: String?) {
        workOrderNumber = newWorkOrderNumber
    }

    fun getWorkOrderNumber(): String? {
        return workOrderNumber
    }

    fun setWorkOrderHistory(newWorkOrderHistory: WorkOrderHistory?) {
        workOrderHistory = newWorkOrderHistory
    }

    fun getWorkOrderHistory(): WorkOrderHistory? {
        return workOrderHistory
    }

    fun setTempWorkOrderHistoryInfo(newInfo: TempWorkOrderHistoryInfo?) {
        tempWorkOrderHistoryInfo = newInfo
    }

    fun getTempWorkOrderHistoryInfo(): TempWorkOrderHistoryInfo? {
        return tempWorkOrderHistoryInfo
    }

    fun setPayPeriodExtraList(newList: ArrayList<WorkPayPeriodExtras>) {
        payPeriodExtraList = newList
    }

    fun getPayPeriodExtraList(): ArrayList<WorkPayPeriodExtras> {
        return payPeriodExtraList
    }

    fun clearPayPeriodExtraList() {
        payPeriodExtraList.clear()
    }

    fun setPayPeriodExtra(newExtra: WorkPayPeriodExtras?) {
        payPeriodExtra = newExtra
    }

    fun getPayPeriodExtra(): WorkPayPeriodExtras? {
        return payPeriodExtra
    }

    fun getIsCredit(): Boolean {
        return isCredit
    }

    fun setIsCredit(b: Boolean) {
        isCredit = b
    }

    fun getWorkDateExtra(): WorkDateExtras? {
        return workDateExtra
    }

    fun setWorkDateExtra(newExtra: WorkDateExtras?) {
        workDateExtra = newExtra
    }

    fun setWorkDateExtraList(extraList: ArrayList<WorkDateExtras>) {
        workDateExtraList = extraList
    }

    fun getWorkDateExtraList(): ArrayList<WorkDateExtras> {
        return workDateExtraList
    }

    fun setPayRate(newRate: EmployerPayRates?) {
        payRate = newRate
    }

    fun getPayRate(): EmployerPayRates? {
        return payRate
    }

    fun getWorkExtraType(): WorkExtraTypes? {
        return extraType
    }

    fun setWorkExtraType(newExtra: WorkExtraTypes?) {
        extraType = newExtra
    }

    fun setWorkDateObject(newDate: WorkDates?) {
        workDateObject = newDate
    }

    fun getWorkDateObject(): WorkDates? {
        return workDateObject
    }

    fun setPayPeriod(newPayPeriod: PayPeriods?) {
        payPeriod = newPayPeriod
    }

    fun getPayPeriod(): PayPeriods? {
        return payPeriod
    }

    fun setCutOffDate(date: String?) {
        cutOffDate = date
    }

    fun getCutOffDate(): String? {
        return cutOffDate
    }

    fun setWorkDateString(date: String?) {
        workDate = date
    }

    fun getWorkDateString(): String? {
        return workDate
    }

    fun setEmployerString(employer: String?) {
        employerString = employer
    }

    fun getEmployerString(): String? {
        return employerString
    }

    fun setExtraDefinitionFull(newExtra: ExtraDefTypeAndEmployer?) {
        extraDefinitionFull = newExtra
    }

    fun getExtraDefinitionFull(): ExtraDefTypeAndEmployer? {
        return extraDefinitionFull
    }

    fun setCallingFragment(newFragment: String?) {
        callingFragment = newFragment
    }

    fun addCallingFragment(newFragment: String?) {
        if (callingFragment != null) {
            callingFragment += ", $newFragment"
        } else {
            callingFragment = newFragment
        }
    }

    fun removeCallingFragment(oldFragment: String) {
        callingFragment = if (callingFragment != null) {
            callingFragment!!.replace(", $oldFragment", "")
            callingFragment!!.replace(oldFragment, "")
        } else {
            null
        }
    }

    fun getCallingFragment(): String? {
        return callingFragment
    }

    fun setEffectiveDateString(newDate: String?) {
        effectiveDateString = newDate
    }

    fun getEffectiveDateString(): String? {
        return effectiveDateString
    }

    fun setTaxTypeString(newType: String?) {
        taxTypeString = newType
    }

    fun setTaxLevel(newLevel: Int?) {
        taxLevel = newLevel
    }

    fun setEmployer(newEmployer: Employers?) {
        employer = newEmployer
    }

    fun setTaxType(newTaxType: TaxTypes?) {
        taxType = newTaxType
    }

    fun setTaxRule(newTaxRule: WorkTaxRules?) {
        taxRule = newTaxRule
    }

    fun getTaxTypeString(): String? {
        return taxTypeString
    }

    fun getTaxLevel(): Int? {
        return taxLevel
    }

    fun getEmployer(): Employers? {
        return employer
    }

    fun getTaxType(): TaxTypes? {
        return taxType
    }

    fun getTaxRule(): WorkTaxRules? {
        return taxRule
    }

    fun clearAllData() {
        employer = null
        employerString = null
        taxType = null
        taxTypeString = null
        taxRule = null
        effectiveDateString = null
        taxLevel = null
        callingFragment = null
        extraDefinitionFull = null
        extraType = null
        workDateExtraList.clear()
        workDateExtra = null
        workDate = null
        workDateObject = null
        cutOffDate = null
        payPeriod = null
        payRate = null
        isCredit = false
        payPeriodExtra = null
        payPeriodExtraList.clear()
        tempWorkOrderHistoryInfo = null
        workOrderHistory = null
        workOrderNumber = null
        workOrder = null
        jobSpec = null
        jobSpecId = null
        jobSpecIsMaster = true
        workOrderJobSpecId = null
        workPerformedId = null
        workPerformedIsParent = true
        workPerformedHistoryId = null
        material = null
        materialId = null
        materialIsParent = true
        materialInSequence = null
        extraContainer = null
        areaId = null
        workOrderHistoryTimeWorkedCombined = null
    }
}