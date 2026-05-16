package ms.mattschlenkrich.paycalculator.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

class MainViewModel(
    app: Application,
) : AndroidViewModel(app) {
    private var employer: Employers? = null
    private var taxType: TaxTypes? = null
    private var taxTypeString: String? = null
    private var taxRule: WorkTaxRules? = null
    private var effectiveDateString: String? = null
    private var taxLevel: Int? = null
    private var extraDefinitionFull: ExtraDefTypeAndEmployer? = null
    private var extraType: WorkExtraTypes? = null
    private var workDateExtraList = ArrayList<WorkDateExtras>()
    private var workDateExtra: WorkDateExtras? = null
    private var workDateObject: WorkDates? = null
    private var cutOffDate: String? = null
    private var payPeriod: PayPeriods? = null
    private var payRate: EmployerPayRates? = null
    private var payPeriodExtra: WorkPayPeriodExtras? = null
    private var tempWorkOrderHistoryInfo: TempWorkOrderHistoryInfo? = null
    private var workOrderHistory: WorkOrderHistory? = null
    private var workOrderNumber: String? = null
    private var workOrder: WorkOrder? = null
    private var workOrderJobSpecId: Long? = null
    private var jobSpecId: Long? = null
    private var jobSpecIsMaster: Boolean = true
    private var workPerformedId: Long? = null
    private var workPerformedIsParent: Boolean = true
    private var workPerformedHistoryId: Long? = null
    private var material: Material? = null
    private var materialId: Long? = null
    private var materialIsParent: Boolean = true
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

    fun setPayPeriodExtra(newExtra: WorkPayPeriodExtras?) {
        payPeriodExtra = newExtra
    }

    fun getPayPeriodExtra(): WorkPayPeriodExtras? {
        return payPeriodExtra
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

    fun setExtraDefinitionFull(newExtra: ExtraDefTypeAndEmployer?) {
        extraDefinitionFull = newExtra
    }

    fun getExtraDefinitionFull(): ExtraDefTypeAndEmployer? {
        return extraDefinitionFull
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
}